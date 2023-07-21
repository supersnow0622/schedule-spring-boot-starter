package com.wlx.middleware.schedule.config;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.schedule.annotation.DcsScheduled;
import com.wlx.middleware.schedule.common.Constants;
import com.wlx.middleware.schedule.domain.ExecOrder;
import com.wlx.middleware.schedule.service.HeartbeatService;
import com.wlx.middleware.schedule.service.ZkCuratorServer;
import com.wlx.middleware.schedule.task.CronTaskRegister;
import com.wlx.middleware.schedule.task.SchedulingRunnable;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wlx.middleware.schedule.common.Constants.*;

public class DcsSchedulingConfiguration implements BeanPostProcessor, ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent> {

    private final List<Class<?>> nonAnnotatedClasses = new ArrayList<>();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Constants.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (nonAnnotatedClasses.contains(targetClass)) {
            return bean;
        }
        Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : allDeclaredMethods) {
            DcsScheduled annotation = method.getAnnotation(DcsScheduled.class);
            if (annotation == null) {
                continue;
            }
            ExecOrder execOrder = new ExecOrder();
            execOrder.setBean(bean);
            execOrder.setBeanName(beanName);
            execOrder.setMethodName(method.getName());
            execOrder.setDesc(annotation.desc());
            execOrder.setCron(annotation.cron());
            execOrder.setAutoRun(annotation.autoRun());
            execOrderMap.computeIfAbsent(beanName, x -> new ArrayList<>()).add(execOrder);
        }
        nonAnnotatedClasses.add(targetClass);
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        // 1.初始化配置
        initConfig(applicationContext);

        // 2.初始化server
        initServer(applicationContext);

        // 3.启动任务，挂载节点
        initTaskAndNode(applicationContext);

        // 4.心跳监听
        HeartbeatService.startFlushScheduleStatus();
    }

    private void initConfig(ApplicationContext applicationContext) {
        try {
            StarterServiceProperties serviceProperties = ((StarterAutoConfig)applicationContext
                    .getBean("starterAutoConfig")).getServiceProperties();
            zkAddress = serviceProperties.getZkAddress();
            schedulerServerId = serviceProperties.getSchedulerServerId();
            schedulerServerName = serviceProperties.getSchedulerServerName();
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initServer(ApplicationContext applicationContext) {
        try {
            zkClient = ZkCuratorServer.getClient(zkAddress, BASE_PATH);
            serverPath = BASE_PATH + LINE + SERVER + LINE + schedulerServerId;
            classPath = serverPath + LINE + IP + LINE + localIP + LINE + CLASS;
            // 创建项目节点
            ZkCuratorServer.createNode(zkClient, classPath);
            ZkCuratorServer.setData(zkClient, serverPath, schedulerServerName);
            // 创建执行节点并监听该节点
            ZkCuratorServer.createNode(zkClient, EXE_PATH);
            ZkCuratorServer.addTreeCacheListener(zkClient, EXE_PATH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initTaskAndNode(ApplicationContext applicationContext) {
        if (execOrderMap.size() == 0) {
            return;
        }
        CronTaskRegister cronTaskRegister = (CronTaskRegister) applicationContext.getBean("cronTaskRegister");
        for (Map.Entry<String, List<ExecOrder>> entry : execOrderMap.entrySet()) {
            String key = entry.getKey();
            List<ExecOrder> execOrders = entry.getValue();
            for (ExecOrder execOrder : execOrders) {
                if (execOrder.isAutoRun()) {
                    cronTaskRegister.addCronTask(new SchedulingRunnable(execOrder.getBean(), execOrder.getBeanName(),
                            execOrder.getMethodName()), execOrder.getCron());
                }

                String methodPath = classPath + LINE + execOrder.getBeanName() + LINE + METHOD + LINE + execOrder.getMethodName();
                String execOrderPath = methodPath + LINE + VALUE;
                String statusPath = methodPath + LINE + STATUS;
                ZkCuratorServer.createNode(zkClient, execOrderPath);
                ZkCuratorServer.createNode(zkClient, statusPath);
                ZkCuratorServer.setData(zkClient, execOrderPath, JSON.toJSONString(execOrder));
                ZkCuratorServer.setData(zkClient, statusPath, execOrder.isAutoRun() ? "1" : "0");
            }
        }
    }

}

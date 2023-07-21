package com.wlx.middleware.schedule.export;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.schedule.domain.*;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import static com.wlx.middleware.schedule.common.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DcsScheduleResource {

    private String zkAddress;

    private CuratorFramework client;

    public DcsScheduleResource(String zkAddress) {
        this.zkAddress = zkAddress;
        client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))
                .build();
        client.start();
    }

    public List<String> queryPathRootServerList() throws Exception {
        return getChildren(BASE_PATH + LINE + SERVER);
    }

    public List<DcsScheduleInfo> queryDcsScheduleInfoList(String schedulerServerId) throws Exception {
        List<DcsScheduleInfo> results = new ArrayList<>();
        String serverPath = BASE_PATH + LINE + SERVER + LINE + schedulerServerId;
        String serverName = getData(serverPath);
        String ipPath = serverPath + LINE + IP;
        List<String> ipList = getChildren(ipPath);
        for (String ip : ipList) {
            String classPath = ipPath + LINE + ip + LINE + CLASS;
            List<String> beanNameList = getChildren(classPath);
            for (String beanName : beanNameList) {
                String methodPath = classPath + LINE + beanName + LINE + METHOD;
                List<String> methodNameList = getChildren(methodPath);
                for (String methodName : methodNameList) {
                    String valuePath = methodPath + LINE + methodName + LINE + VALUE;
                    String data = getData(valuePath);
                    ExecOrder execOrder = JSON.parseObject(data, ExecOrder.class);
                    if (execOrder == null) {
                        continue;
                    }
                    DcsScheduleInfo dcsScheduleInfo = new DcsScheduleInfo();
                    dcsScheduleInfo.setIp(ip);
                    dcsScheduleInfo.setSchedulerServerId(schedulerServerId);
                    dcsScheduleInfo.setSchedulerServerName(serverName);
                    dcsScheduleInfo.setBeanName(beanName);
                    dcsScheduleInfo.setMethodName(methodName);
                    dcsScheduleInfo.setDesc(execOrder.getDesc());
                    dcsScheduleInfo.setCron(execOrder.getCron());
                    String statusPath = methodPath + LINE + methodName + LINE + STATUS;
                    String status = getData(statusPath);
                    dcsScheduleInfo.setStatus(StringUtils.isNotEmpty(status) ? Integer.parseInt(status) : 0);
                    results.add(dcsScheduleInfo);
                }
            }
        }
        return results;
    }

    public DataCollect queryDataCollect() throws Exception {
        AtomicInteger serverCount = new AtomicInteger(0);
        AtomicInteger ipCount = new AtomicInteger(0);
        AtomicInteger beanCount = new AtomicInteger(0);
        AtomicInteger methodCount = new AtomicInteger(0);

        String serverPath = BASE_PATH + LINE + SERVER;
        List<String> serverList = getChildren(serverPath);
        serverCount.getAndAdd(serverList.size());
        for (String server : serverList) {
            String ipPath = serverPath + LINE + server + LINE + IP;
            List<String> ipList = getChildren(ipPath);
            ipCount.getAndAdd(ipList.size());
            for (String ip : ipList) {
                String classPath = ipPath + LINE + ip + LINE + CLASS;
                List<String> classList = getChildren(classPath);
                beanCount.getAndAdd(classList.size());
                for (String clazz : classList) {
                    String methodPath = classPath + LINE + clazz + LINE + METHOD;
                    List<String> methodList = getChildren(methodPath);
                    methodCount.getAndAdd(methodList.size());
                }
            }
        }
        return new DataCollect(serverCount.get(), ipCount.get(), beanCount.get(), methodCount.get());
    }

    public List<DcsServerNode> queryDcsServerNodeList() throws Exception {
        List<DcsServerNode> results = new ArrayList<>();
        String serverPath = BASE_PATH + LINE + SERVER;
        List<String> serverList = getChildren(serverPath);
        for (String server : serverList) {
            String data = getData(serverPath + LINE + server);
            DcsServerNode dcsServerNode = new DcsServerNode();
            dcsServerNode.setSchedulerServerId(server);
            dcsServerNode.setSchedulerServerName(data);
            results.add(dcsServerNode);
        }
        return results;
    }

    public void pushInstruct(Instruct instruct) throws Exception {
        if (client.checkExists().forPath(EXE_PATH) == null) {
            return;
        }
        client.setData().forPath(EXE_PATH, JSON.toJSONString(instruct).getBytes(CHARSET));
    }

    private List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    private int getChildrenCount(String path) throws Exception {
        return client.getChildren().forPath(path).size();
    }

    private String getData(String path) throws Exception {
        byte[] bytes = client.getData().forPath(path);
        if (bytes == null || bytes.length < 1) {
            return null;
        }
        return new String(bytes, CHARSET);
    }
}

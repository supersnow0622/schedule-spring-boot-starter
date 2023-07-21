package com.wlx.middleware.schedule.service;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.schedule.common.Constants;
import com.wlx.middleware.schedule.domain.Instruct;
import com.wlx.middleware.schedule.task.CronTaskRegister;
import com.wlx.middleware.schedule.task.SchedulingRunnable;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import static com.wlx.middleware.schedule.common.Constants.*;

public class ZkCuratorServer {


    public static CuratorFramework getClient(String zkAddress, String namespace) {
        if (null != Constants.zkClient) {
            return Constants.zkClient;
        }
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))
                .build();
        client.start();
        return client;
    }

    public static void createNode(CuratorFramework zkClient, String path) {
        try {
            if (zkClient.checkExists().forPath(path) == null) {
                zkClient.create().creatingParentsIfNeeded().forPath(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteNode(CuratorFramework zkClient, String path) {
        try {
            if (zkClient.checkExists().forPath(path) != null) {
                zkClient.delete().deletingChildrenIfNeeded().forPath(path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setData(CuratorFramework zkClient, String path, String value) {
        try {
            if (zkClient.checkExists().forPath(path) != null) {
                zkClient.setData().forPath(path, value.getBytes(CHARSET));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addTreeCacheListener(CuratorFramework zkClient, String path) {
        try {
            TreeCache treeCache = new TreeCache(zkClient, path);
            treeCache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
                if (treeCacheEvent.getData() == null) {
                    return;
                }
                byte[] data = treeCacheEvent.getData().getData();
                if (data == null || data.length < 1) {
                    return;
                }
                String json = new String(data, CHARSET);
                if (StringUtils.isEmpty(json) || json.indexOf("{") != 0 || json.lastIndexOf("}") + 1 != json.length()) {
                    return;
                }
                Instruct instruct = JSON.parseObject(json, Instruct.class);
                if (instruct == null) {
                    return;
                }
                switch (treeCacheEvent.getType()) {
                    case NODE_ADDED:
                    case NODE_UPDATED:
                        if (!schedulerServerId.equals(instruct.getSchedulerServerId()) ||
                                !localIP.equals(instruct.getIp())) {
                            return;
                        }
                        CronTaskRegister cronTaskRegister = (CronTaskRegister) Constants.applicationContext.getBean("cronTaskRegister");
                        int status = instruct.getStatus();
                        String statusPath = classPath + LINE + instruct.getBeanName() + LINE + METHOD +
                                LINE + instruct.getMethodName() + LINE + STATUS;
                        if (!applicationContext.containsBean(instruct.getBeanName())) {
                            return;
                        }
                        Object bean = applicationContext.getBean(instruct.getBeanName());
                        switch (status) {
                            case 0:
                                cronTaskRegister.removeCronTask(instruct.getTaskId());
                                setData(zkClient, statusPath, "0");
                                break;
                            case 1:
                                cronTaskRegister.addCronTask(new SchedulingRunnable(bean, instruct.getBeanName(), instruct.getMethodName()),
                                        instruct.getCron());
                                setData(zkClient, statusPath, "1");
                                break;
                            case 2:
                                cronTaskRegister.removeCronTask(instruct.getTaskId());
                                cronTaskRegister.addCronTask(new SchedulingRunnable(bean, instruct.getBeanName(), instruct.getMethodName()),
                                        instruct.getCron());
                                setData(zkClient, statusPath, "1");
                                break;
                        }
                        break;
                    default: break;
                }
            });
            treeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



}

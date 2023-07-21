package com.wlx.middleware.schedule.service;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.schedule.domain.ExecOrder;
import com.wlx.middleware.schedule.task.ScheduledTaskFuture;
import org.apache.commons.beanutils.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.wlx.middleware.schedule.common.Constants.*;

public class HeartbeatService {

    private static class Singleton {
        private static final HeartbeatService INSTANCE = new HeartbeatService();
    }

    private HeartbeatService() {
    }

    public static HeartbeatService getInstance() {
        return Singleton.INSTANCE;
    }

    public static void startFlushScheduleStatus() {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(() -> {
            try {
                for (Map.Entry<String, List<ExecOrder>> entry : execOrderMap.entrySet()) {
                    for (ExecOrder execOrder : entry.getValue()) {
                        ScheduledTaskFuture future = scheduledTasks.get(execOrder.getTaskId());
                        if (future == null) {
                            continue;
                        }
                        String methodPath = BASE_PATH + LINE + SERVER + LINE + schedulerServerId + LINE + IP + LINE + localIP +
                                LINE + CLASS + LINE + execOrder.getBeanName() + LINE + execOrder.getMethodName();
                        String valuePath = methodPath + LINE + VALUE;
                        String statusPath = methodPath + LINE + STATUS;
                        if (zkClient.checkExists().forPath(valuePath) == null) {
                            continue;
                        }
                        ExecOrder oldExecOrder;
                        byte[] bytes = zkClient.getData().forPath(valuePath);
                        if (bytes != null) {
                            oldExecOrder = JSON.parseObject(new String(bytes, CHARSET), ExecOrder.class);
                        } else {
                            oldExecOrder = new ExecOrder();
                            BeanUtils.copyProperties(oldExecOrder, execOrder);
                        }
                        oldExecOrder.setAutoRun(!future.getScheduledFuture().isCancelled());
                        zkClient.setData().forPath(valuePath, JSON.toJSONString(oldExecOrder).getBytes(CHARSET));
                        if (zkClient.checkExists().forPath(statusPath) == null) {
                            continue;
                        }
                        zkClient.setData().forPath(statusPath, (execOrder.isAutoRun() ? "1" : "0").getBytes(CHARSET));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 300, 60, TimeUnit.SECONDS);
    }

}

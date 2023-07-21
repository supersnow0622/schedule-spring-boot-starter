package com.wlx.middleware.schedule.common;

import com.wlx.middleware.schedule.domain.ExecOrder;
import com.wlx.middleware.schedule.task.ScheduledTaskFuture;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {

    // key:beanName value:一个bean中定义的多个任务列表
    public static final Map<String, List<ExecOrder>> execOrderMap = new ConcurrentHashMap<>();

    // key:beanName+methodName value:任务
    public static final Map<String, ScheduledTaskFuture> scheduledTasks = new ConcurrentHashMap<>();

    public static CuratorFramework zkClient;

    public static ApplicationContext applicationContext;

    public static String zkAddress;

    public static String schedulerServerId;

    public static String schedulerServerName;

    public static String localIP;

    public static String CHARSET = "utf-8";

    public static String BASE_PATH = "/com/wlx/middleware/schedule";

    public static String serverPath;

    public static String classPath;

    public static String LINE = "/";

    public static String EXE_PATH = BASE_PATH + LINE + "exec";

    public static String SERVER = "server";

    public static String IP = "ip";

    public static String CLASS = "class";

    public static String METHOD = "method";

    public static String VALUE = "value";

    public static String STATUS = "status";
}

package com.wlx.middleware.schedule.domain;

import com.alibaba.fastjson.annotation.JSONField;

public class ExecOrder {

    @JSONField(serialize = false)
    private Object bean;

    // 实例名称
    private String beanName;

    // 方法名
    private String methodName;

    // 任务描述
    private String desc;

    // cron表达式
    private String cron;

    // 是否自动执行
    private boolean autoRun;

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isAutoRun() {
        return autoRun;
    }

    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }

    public String getTaskId() {
        return beanName + "-" + methodName;
    }
}

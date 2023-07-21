package com.wlx.middleware.schedule.task;

import java.lang.reflect.Method;

public class SchedulingRunnable implements Runnable {

    private Object bean;

    private String beanName;

    private String methodName;

    public SchedulingRunnable(Object bean, String beanName, String methodName) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    @Override
    public void run() {
        try {
            Method declaredMethod = bean.getClass().getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTaskId() {
        return beanName + "-" + methodName;
    }

}

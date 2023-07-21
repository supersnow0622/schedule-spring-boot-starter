package com.wlx.middleware.schedule.domain;

public class DataCollect {

    private int serverCount;
    private int ipCount;
    private int beanCount;
    private int methodCount;

    public DataCollect(int serverCount, int ipCount, int beanCount, int methodCount) {
        this.serverCount = serverCount;
        this.ipCount = ipCount;
        this.beanCount = beanCount;
        this.methodCount = methodCount;
    }

    public int getServerCount() {
        return serverCount;
    }

    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    public int getIpCount() {
        return ipCount;
    }

    public void setIpCount(int ipCount) {
        this.ipCount = ipCount;
    }

    public int getBeanCount() {
        return beanCount;
    }

    public void setBeanCount(int beanCount) {
        this.beanCount = beanCount;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }
}

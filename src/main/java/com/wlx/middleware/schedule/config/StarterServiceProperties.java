package com.wlx.middleware.schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.wlx.middleware.zookeeper")
public class StarterServiceProperties {

    private String zkAddress;

    private String schedulerServerId;

    private String schedulerServerName;

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String getSchedulerServerId() {
        return schedulerServerId;
    }

    public void setSchedulerServerId(String schedulerServerId) {
        this.schedulerServerId = schedulerServerId;
    }

    public String getSchedulerServerName() {
        return schedulerServerName;
    }

    public void setSchedulerServerName(String schedulerServerName) {
        this.schedulerServerName = schedulerServerName;
    }
}

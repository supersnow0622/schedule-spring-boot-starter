package com.wlx.middleware.schedule.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("starterAutoConfig")
@EnableConfigurationProperties(StarterServiceProperties.class)
public class StarterAutoConfig {

    @Autowired
    private StarterServiceProperties serviceProperties;

    public StarterServiceProperties getServiceProperties() {
        return serviceProperties;
    }

    public void setServiceProperties(StarterServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }
}

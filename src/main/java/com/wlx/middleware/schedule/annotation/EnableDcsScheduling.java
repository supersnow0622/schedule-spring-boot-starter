package com.wlx.middleware.schedule.annotation;

import com.wlx.middleware.schedule.DoJoinPoint;
import com.wlx.middleware.schedule.config.DcsSchedulingConfiguration;
import com.wlx.middleware.schedule.task.CronTaskRegister;
import com.wlx.middleware.schedule.task.SchedulingConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({DcsSchedulingConfiguration.class})
@ImportAutoConfiguration({SchedulingConfig.class, CronTaskRegister.class, DoJoinPoint.class})
@ComponentScan("com.wlx.middleware.*")
public @interface EnableDcsScheduling {
}

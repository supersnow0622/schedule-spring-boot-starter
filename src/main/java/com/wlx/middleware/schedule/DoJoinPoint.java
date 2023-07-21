package com.wlx.middleware.schedule;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;



@Component("doJoinPoint")
@Aspect
public class DoJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DoJoinPoint.class);

    @Pointcut("@annotation(com.wlx.middleware.schedule.annotation.DcsScheduled)")
    public void aopPoint() {
    }

    @Around("aopPoint()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());
        long begin = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            logger.info("class={} method={} take time {}ms", joinPoint.getTarget().getClass().getSimpleName(),
                    method.getName(), end - begin);
        }
    }

}

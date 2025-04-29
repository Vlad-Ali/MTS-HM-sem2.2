package com.example.kafkaproducerhomework.metrics.outbox;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class OutboxMetricsAspect {
    private final OutboxMetrics metrics;
    private static final Logger LOG = LoggerFactory.getLogger(OutboxMetricsAspect.class);

    public OutboxMetricsAspect(OutboxMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("execution(* com.example.kafkaproducerhomework.scheduler.OutboxScheduler.processOutbox(..))")
    public Object recordProcessMessages(ProceedingJoinPoint joinPoint) throws Throwable {
        LOG.debug("Method recordProcessMessages called");
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordProcessDuration(duration);
            return result;
        } catch (Exception e) {
            LOG.error("Error processing messages", e);
            throw e;
        }
    }

}

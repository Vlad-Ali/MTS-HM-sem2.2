package com.example.kafkaproducerhomework.scheduler;

import com.example.kafkaproducerhomework.entity.OutboxEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Aspect
public class OutboxMetricsAspect {
    private final OutboxMetrics metrics;
    private static final Logger LOG = LoggerFactory.getLogger(OutboxMetricsAspect.class);

    public OutboxMetricsAspect(OutboxMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("execution(* com.example.kafkaproducerhomework.scheduler.OutboxScheduler.processOutbox(..))")
    public Object recordProcessOutbox(ProceedingJoinPoint joinPoint) throws Throwable {
        LOG.debug("Method recordProcessOutbox called");
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordProcessDuration(duration);
        }
    }

    @AfterReturning(
            pointcut = "execution(public void com.example.kafkaproducerhomework.scheduler.OutboxScheduler.processMessages(java.util.List<com.example.kafkaproducerhomework.entity.OutboxEntity>)) && args(messages)",
            argNames = "messages"
    )
    public void recordCountMessages(List<OutboxEntity> messages) {
        LOG.debug("Method recordCountMessages called with {} messages", messages.size());
        metrics.incrementRequests(messages.size());
    }

    @Around("execution(* com.example.kafkaproducerhomework.scheduler.OutboxScheduler.handleKafkaResult(..))")
    public Object recordKafkaSend(ProceedingJoinPoint joinPoint) throws Throwable {
        LOG.debug("Method recordKafkaSend called");

        CompletableFuture<?> future = (CompletableFuture<?>) joinPoint.getArgs()[0];
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        future.whenComplete((res, ex) -> {
            long duration = System.currentTimeMillis() - startTime;
            if (ex == null) {
                metrics.recordSuccess(duration);
                LOG.debug("Successfully recorded Kafka send duration: {} ms", duration);
            } else {
                LOG.error("Failed to send message to Kafka", ex);
            }
        });
        LOG.debug("Add success record");

        return result;
    }


}

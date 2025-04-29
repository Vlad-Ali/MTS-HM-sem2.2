package com.example.kafkaproducerhomework.metrics.websitescontroller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class WebsitesControllerMetricsAspect {
    private final WebsitesControllerMetrics websitesControllerMetrics;
    private static final Logger LOG = LoggerFactory.getLogger(WebsitesControllerMetricsAspect.class);

    public WebsitesControllerMetricsAspect(WebsitesControllerMetrics websitesControllerMetrics) {
        this.websitesControllerMetrics = websitesControllerMetrics;
    }

    @Around("execution(* com.example.kafkaproducerhomework.controller.website.WebsitesController.*(..))")
    public Object recordSuccessfulRequests(ProceedingJoinPoint joinPoint) throws Throwable {
        LOG.debug("Method recordSuccessfulRequests called");
        Object result = joinPoint.proceed();
        if (result instanceof ResponseEntity<?>){
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (response.getStatusCode().is2xxSuccessful()){
                websitesControllerMetrics.incrementRequests();
            }
        }
        return result;
    }

}

package com.example.kafkaproducerhomework.metrics.websitescontroller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class WebsitesControllerMetrics {
    private final Counter rpsCounter;

    public WebsitesControllerMetrics(MeterRegistry registry) {
        this.rpsCounter = Counter.builder("websites.requests.rps")
                .description("Requests per second for GET websites")
                .register(registry);
    }

    public void incrementRequests(){
        rpsCounter.increment(1);
    }
}

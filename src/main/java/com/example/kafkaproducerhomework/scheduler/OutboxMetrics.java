package com.example.kafkaproducerhomework.scheduler;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OutboxMetrics {
    private final Counter rpsCounter;
    private final Timer responseTimer;
    private final Timer sliTimer;
    private final DistributionSummary heatmapDistribution;

    public OutboxMetrics(MeterRegistry registry) {
        this.rpsCounter = Counter.builder("outbox.requests.rps")
                .description("Requests per second for outbox processing")
                .register(registry);

        this.responseTimer = Timer.builder("outbox.response.avg")
                .description("Average response time for outbox processing")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(registry);

        this.sliTimer = Timer.builder("outbox.sli")
                .description("Service Level Indicator for outbox processing")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(registry);

        this.heatmapDistribution = DistributionSummary.builder("outbox.heatmap")
                .description("Heatmap of processing times")
                .baseUnit("milliseconds")
                .serviceLevelObjectives(10, 50, 100, 200, 300, 400, 500, 1000, 10000)
                .register(registry);
    }

    public void recordSuccess(long duration) {
        sliTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    public void incrementRequests(int count) {
        rpsCounter.increment(count);
    }

    public void recordProcessDuration(long duration) {
        responseTimer.record(duration, TimeUnit.MILLISECONDS);
        heatmapDistribution.record(duration);
    }
}

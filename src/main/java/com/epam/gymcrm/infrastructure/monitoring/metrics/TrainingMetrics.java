package com.epam.gymcrm.infrastructure.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TrainingMetrics {

    private final Counter createdTrainings;

    public TrainingMetrics(MeterRegistry registry) {
        this.createdTrainings = registry.counter("training_created_total");
    }

    public void incrementCreated() {
        createdTrainings.increment();
    }
}

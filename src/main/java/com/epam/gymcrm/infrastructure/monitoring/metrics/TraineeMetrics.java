package com.epam.gymcrm.infrastructure.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TraineeMetrics {

    private final Counter createdCounter;
    private final Counter updatedCounter;
    private final Counter trainerUpdateCounter;
    private final Counter activatedCounter;
    private final Counter deactivatedCounter;

    public TraineeMetrics(MeterRegistry registry) {
        this.createdCounter = registry.counter("trainee_registered_total");
        this.updatedCounter = registry.counter("trainee_updated_total");
        this.trainerUpdateCounter = registry.counter("trainee_trainers_updated_total");
        this.activatedCounter = registry.counter("trainee_activated_total");
        this.deactivatedCounter = registry.counter("trainee_deactivated_total");
    }

    public void incrementRegistered() {
        createdCounter.increment();
    }

    public void incrementUpdated() {
        updatedCounter.increment();
    }

    public void incrementTrainerUpdated() {
        trainerUpdateCounter.increment();
    }

    public void incrementActivated() {
        activatedCounter.increment();
    }

    public void incrementDeactivated() {
        deactivatedCounter.increment();
    }
}

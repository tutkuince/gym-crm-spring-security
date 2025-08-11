package com.epam.gymcrm.infrastructure.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TrainerMetrics {

    private final Counter registeredTrainers;
    private final Counter updatedTrainers;
    private final Counter activatedTrainers;
    private final Counter deactivatedTrainers;

    public TrainerMetrics(MeterRegistry registry) {
        this.registeredTrainers = registry.counter("trainer_registered_total");
        this.updatedTrainers = registry.counter("trainer_updated_total");
        this.activatedTrainers = registry.counter("trainer_activated_total");
        this.deactivatedTrainers = registry.counter("trainer_deactivated_total");
    }

    public void incrementRegistered() {
        registeredTrainers.increment();
    }

    public void incrementUpdated() {
        updatedTrainers.increment();
    }

    public void incrementActivated() {
        activatedTrainers.increment();
    }

    public void incrementDeactivated() {
        deactivatedTrainers.increment();
    }
}

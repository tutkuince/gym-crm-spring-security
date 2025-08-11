package com.epam.gymcrm.infrastructure.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerMetricsTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter registeredCounter;

    @Mock
    private Counter updatedCounter;

    @Mock
    private Counter activatedCounter;

    @Mock
    private Counter deactivatedCounter;

    private TrainerMetrics trainerMetrics;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("trainer_registered_total")).thenReturn(registeredCounter);
        when(meterRegistry.counter("trainer_updated_total")).thenReturn(updatedCounter);
        when(meterRegistry.counter("trainer_activated_total")).thenReturn(activatedCounter);
        when(meterRegistry.counter("trainer_deactivated_total")).thenReturn(deactivatedCounter);

        trainerMetrics = new TrainerMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementRegisteredCounter() {
        trainerMetrics.incrementRegistered();
        verify(registeredCounter).increment();
    }

    @Test
    void shouldIncrementUpdatedCounter() {
        trainerMetrics.incrementUpdated();
        verify(updatedCounter).increment();
    }

    @Test
    void shouldIncrementActivatedCounter() {
        trainerMetrics.incrementActivated();
        verify(activatedCounter).increment();
    }

    @Test
    void shouldIncrementDeactivatedCounter() {
        trainerMetrics.incrementDeactivated();
        verify(deactivatedCounter).increment();
    }
}
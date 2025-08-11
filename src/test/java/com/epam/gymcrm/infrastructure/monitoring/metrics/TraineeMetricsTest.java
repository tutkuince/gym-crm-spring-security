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
class TraineeMetricsTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter registeredCounter;

    @Mock
    private Counter updatedCounter;

    @Mock
    private Counter trainerUpdatedCounter;

    @Mock
    private Counter activatedCounter;

    @Mock
    private Counter deactivatedCounter;

    private TraineeMetrics traineeMetrics;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("trainee_registered_total")).thenReturn(registeredCounter);
        when(meterRegistry.counter("trainee_updated_total")).thenReturn(updatedCounter);
        when(meterRegistry.counter("trainee_trainers_updated_total")).thenReturn(trainerUpdatedCounter);
        when(meterRegistry.counter("trainee_activated_total")).thenReturn(activatedCounter);
        when(meterRegistry.counter("trainee_deactivated_total")).thenReturn(deactivatedCounter);

        traineeMetrics = new TraineeMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementRegisteredCounter() {
        traineeMetrics.incrementRegistered();
        verify(registeredCounter).increment();
    }

    @Test
    void shouldIncrementUpdatedCounter() {
        traineeMetrics.incrementUpdated();
        verify(updatedCounter).increment();
    }

    @Test
    void shouldIncrementTrainerUpdatedCounter() {
        traineeMetrics.incrementTrainerUpdated();
        verify(trainerUpdatedCounter).increment();
    }

    @Test
    void shouldIncrementActivatedCounter() {
        traineeMetrics.incrementActivated();
        verify(activatedCounter).increment();
    }

    @Test
    void shouldIncrementDeactivatedCounter() {
        traineeMetrics.incrementDeactivated();
        verify(deactivatedCounter).increment();
    }
}
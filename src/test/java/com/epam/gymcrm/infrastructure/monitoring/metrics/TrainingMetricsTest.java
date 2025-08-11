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
class TrainingMetricsTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter createdCounter;

    private TrainingMetrics trainingMetrics;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("training_created_total")).thenReturn(createdCounter);
        trainingMetrics = new TrainingMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementCreatedCounter() {
        trainingMetrics.incrementCreated();
        verify(createdCounter).increment();
    }
}
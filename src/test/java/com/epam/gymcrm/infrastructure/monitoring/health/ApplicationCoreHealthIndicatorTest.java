package com.epam.gymcrm.infrastructure.monitoring.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ApplicationCoreHealthIndicatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ApplicationCoreHealthIndicator healthIndicator;


    @Test
    void shouldReturnHealthUp_whenDatabaseIsReachable() {
        // given
        doNothing().when(jdbcTemplate).execute("SELECT 1");

        // when
        Health result = healthIndicator.health();

        // then
        assertEquals(Health.up().build().getStatus(), result.getStatus());
        assertEquals("Reachable", result.getDetails().get("database"));
        assertEquals("Optional", result.getDetails().get("core-services"));
        assertEquals("1.0.0", result.getDetails().get("version"));
    }

    @Test
    void shouldReturnHealthDown_whenDatabaseIsUnreachable() {
        // given
        doThrow(new RuntimeException("DB error")).when(jdbcTemplate).execute("SELECT 1");

        // when
        Health result = healthIndicator.health();

        // then
        assertEquals(Health.down().build().getStatus(), result.getStatus());
        assertEquals("Unreachable", result.getDetails().get("database"));
        assertTrue(((String) result.getDetails().get("error")).contains("DB error"));
    }
}
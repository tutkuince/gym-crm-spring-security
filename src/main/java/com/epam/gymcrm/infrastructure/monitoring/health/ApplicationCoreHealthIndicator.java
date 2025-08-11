package com.epam.gymcrm.infrastructure.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("applicationCore")
public class ApplicationCoreHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationCoreHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        // Try to execute a lightweight query to verify DB connectivity
        try {
            jdbcTemplate.execute("SELECT 1");

            return Health.up()
                    .withDetail("database", "Reachable")
                    .withDetail("core-services", "Optional")
                    .withDetail("version", "1.0.0")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

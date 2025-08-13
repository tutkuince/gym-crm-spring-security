package com.epam.gymcrm.infrastructure.security;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

@Service
public class TokenBlacklist {

    private final Map<String, Long> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sweeper;

    public TokenBlacklist() {
        this.sweeper = newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "blacklist-sweeper");
            t.setDaemon(true);
            return t;
        });
        sweeper.scheduleAtFixedRate(this::sweep, 1, 60, TimeUnit.SECONDS);
    }

    public void invalidate(String jti, Instant expiresAt) {
        store.put(jti, expiresAt.toEpochMilli());
    }

    public boolean isInvalid(String jti) {
        Long exp = store.get(jti);
        if (Objects.isNull(exp)) return false;
        if (exp > System.currentTimeMillis()) return true;
        store.remove(jti, exp);
        return false;
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue() < now);
    }

    @PreDestroy
    public void shutdown() {
        sweeper.shutdown();
    }
}
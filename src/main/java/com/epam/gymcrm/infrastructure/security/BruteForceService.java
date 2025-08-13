package com.epam.gymcrm.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceService {
    public record State(int fails, long blockedUntilMs) {}
    private final ConcurrentHashMap<String, State> map = new ConcurrentHashMap<>();
    private final int max;
    private final long blockMs;

    public BruteForceService(
            @Value("${security.bruteforce.max-attempts}") int maxAttempts,
            @Value("${security.bruteforce.block-minutes}") double blockMinutes) {
        this.max = maxAttempts;
        this.blockMs = (long)(blockMinutes * 60_000);
    }

    public boolean isBlocked(String u) {
        State s = map.get(u);
        return Objects.nonNull(s) && s.blockedUntilMs() > System.currentTimeMillis();
    }

    public long retryAfterSeconds(String u) {
        State s = map.get(u);
        if (Objects.isNull(s)) return 0;
        long diff = s.blockedUntilMs() - System.currentTimeMillis();
        return diff > 0 ? Math.max(1, diff / 1000) : 0;
    }

    public void registerFailure(String u) {
        map.compute(u, (k, p) -> {
            int f = (Objects.isNull(p) ? 1 : p.fails() + 1);
            long until = (f >= max) ? System.currentTimeMillis() + blockMs : 0L;
            return new State(f, until);
        });
    }

    public void registerSuccess(String u) { map.remove(u); }
}

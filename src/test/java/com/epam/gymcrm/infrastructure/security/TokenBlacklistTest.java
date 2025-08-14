package com.epam.gymcrm.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistTest {

    private final TokenBlacklist blacklist = new TokenBlacklist();

    @AfterEach
    void tearDown() {
        blacklist.shutdown();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getStore() throws Exception {
        Field f = TokenBlacklist.class.getDeclaredField("store");
        f.setAccessible(true);
        return (Map<String, Long>) f.get(blacklist);
    }

    private ScheduledExecutorService getSweeper() throws Exception {
        Field f = TokenBlacklist.class.getDeclaredField("sweeper");
        f.setAccessible(true);
        return (ScheduledExecutorService) f.get(blacklist);
    }

    private void callSweep() throws Exception {
        Method m = TokenBlacklist.class.getDeclaredMethod("sweep");
        m.setAccessible(true);
        m.invoke(blacklist);
    }

    @Test
    void invalidate_thenIsInvalidTrue_beforeExpiry() {
        String jti = "JTI-FUTURE";
        Instant exp = Instant.now().plusSeconds(3600);

        blacklist.invalidate(jti, exp);

        assertTrue(blacklist.isInvalid(jti), "Future expiry should be considered invalid (blacklisted)");
        assertTrue(blacklist.isInvalid(jti));
    }

    @Test
    void isInvalidReturnsFalse_andRemoves_whenExpiredEntry() throws Exception {
        String jti = "JTI-EXPIRED";
        Instant past = Instant.now().minusSeconds(1);

        blacklist.invalidate(jti, past);

        assertFalse(blacklist.isInvalid(jti));

        Map<String, Long> store = getStore();
        assertFalse(store.containsKey(jti), "Expired entry should be removed from store");
    }

    @Test
    void sweepRemovesOnlyExpiredEntries() throws Exception {
        String expiredJti = "EXPIRED";
        String futureJti = "FUTURE";
        blacklist.invalidate(expiredJti, Instant.now().minusSeconds(5));
        blacklist.invalidate(futureJti, Instant.now().plusSeconds(120));

        callSweep();

        Map<String, Long> store = getStore();
        assertFalse(store.containsKey(expiredJti), "Expired entry must be swept");
        assertTrue(store.containsKey(futureJti), "Non-expired entry must remain");
    }

    @Test
    void shutdownStopsSweeper() throws Exception {
        ScheduledExecutorService sweeper = getSweeper();
        assertFalse(sweeper.isShutdown(), "Sweeper should be running before shutdown");

        blacklist.shutdown();

        assertTrue(sweeper.isShutdown(), "Sweeper should be shutdown after shutdown()");
    }
}
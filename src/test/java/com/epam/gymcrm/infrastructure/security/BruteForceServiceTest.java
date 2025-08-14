package com.epam.gymcrm.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceServiceTest {

    @Test
    void initially_notBlocked_and_retryAfterZero() {
        BruteForceService svc = new BruteForceService(3, 1.0);
        assertFalse(svc.isBlocked("ali"));
        assertEquals(0, svc.retryAfterSeconds("ali"));
    }

    @Test
    void belowThreshold_registerFailure_doesNotBlock() {
        BruteForceService svc = new BruteForceService(3, 1.0);

        svc.registerFailure("ali");
        assertFalse(svc.isBlocked("ali"));
        assertEquals(0, svc.retryAfterSeconds("ali"));

        svc.registerFailure("ali");
        assertFalse(svc.isBlocked("ali"));
        assertEquals(0, svc.retryAfterSeconds("ali"));
    }

    @Test
    void atThreshold_blocks_and_retryAfterPositive() {
        BruteForceService svc = new BruteForceService(2, 0.001);

        svc.registerFailure("ali");
        assertFalse(svc.isBlocked("ali"));

        svc.registerFailure("ali");
        assertTrue(svc.isBlocked("ali"));

        assertTrue(svc.retryAfterSeconds("ali") >= 1);
    }

    @Test
    void afterBlockExpires_notBlocked_and_retryAfterZero() throws InterruptedException {
        BruteForceService svc = new BruteForceService(1, 0.001);

        svc.registerFailure("ali");
        assertTrue(svc.isBlocked("ali"));

        Thread.sleep(80);

        assertFalse(svc.isBlocked("ali"));
        assertEquals(0, svc.retryAfterSeconds("ali"));
    }

    @Test
    void registerSuccess_clearsState() {
        BruteForceService svc = new BruteForceService(1, 1.0);

        svc.registerFailure("ali");
        assertTrue(svc.isBlocked("ali"));

        svc.registerSuccess("ali");
        assertFalse(svc.isBlocked("ali"));
        assertEquals(0, svc.retryAfterSeconds("ali"));
    }

    @Test
    void retryAfterSeconds_forUnknownUser_isZero() {
        BruteForceService svc = new BruteForceService(3, 1.0);
        assertEquals(0, svc.retryAfterSeconds("unknown"));
    }
}
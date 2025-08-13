package com.epam.gymcrm.domain.exception;

import java.io.Serial;

public class AccountLockedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long retryAfterSeconds;

    public AccountLockedException(long retryAfterSeconds) {
        super("ACCOUNT_LOCKED");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
}

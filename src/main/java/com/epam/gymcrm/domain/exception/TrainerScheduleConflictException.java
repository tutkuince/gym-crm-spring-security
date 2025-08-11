package com.epam.gymcrm.domain.exception;

import java.io.Serial;

public class TrainerScheduleConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    public TrainerScheduleConflictException(String message) {
        super(message);
    }
}

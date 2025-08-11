package com.epam.gymcrm.util;


import java.time.format.DateTimeFormatter;

public class DateConstants {
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    private DateConstants() {}
}

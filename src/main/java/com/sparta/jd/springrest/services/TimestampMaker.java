package com.sparta.jd.springrest.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TimestampMaker {
    public static Timestamp getCurrentDate() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    public static Timestamp getCurrentDatePlusDays(int days) {
        return Timestamp.valueOf(LocalDateTime.now().plusDays(days));
    }
}

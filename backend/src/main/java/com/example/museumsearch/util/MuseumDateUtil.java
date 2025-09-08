package com.example.museumsearch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MuseumDateUtil {
    
    public static LocalDate extractStartDate(String schedule) {
        return parseDateFromSchedule(schedule, true);
    }

    public static LocalDate extractEndDate(String schedule) {
        return parseDateFromSchedule(schedule, false);
    }

    private static LocalDate parseDateFromSchedule(String schedule, boolean start) {
        if (schedule == null || schedule.isEmpty()) return null;

        String[] parts = schedule.split("[〜~]");
        if (parts.length == 0) return null;

        String target = start ? parts[0].trim() : (parts.length > 1 ? parts[1].trim() : null);
        if (target == null) return null;

        // "2025年10月5日(日)" → "2025-10-05"
        String cleaned = target.replaceAll("年|月", "-").replaceAll("日.*", "");
        try {
            return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("yyyy-M-d"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

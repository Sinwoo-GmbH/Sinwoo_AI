package com.sinwoo.attendance.support;

import java.time.OffsetDateTime;
import java.util.Locale;

public final class AttendanceStatusCd {

    public static final String CHECKED_IN = "CHECKED_IN";
    public static final String CHECKED_OUT = "CHECKED_OUT";
    public static final String LEAVE = "LEAVE";
    public static final String BUSINESS_TRIP = "BUSINESS_TRIP";
    public static final String NONE = "NONE";

    private AttendanceStatusCd() {
    }

    public static boolean isSupported(String value) {
        return CHECKED_IN.equals(value)
                || CHECKED_OUT.equals(value)
                || LEAVE.equals(value)
                || BUSINESS_TRIP.equals(value);
    }

    public static boolean isNoTimeStatus(String value) {
        return LEAVE.equals(value) || BUSINESS_TRIP.equals(value);
    }

    public static boolean isLeave(String value) {
        return LEAVE.equalsIgnoreCase(value);
    }

    public static boolean isBusinessTrip(String value) {
        return BUSINESS_TRIP.equalsIgnoreCase(value);
    }

    public static String normalizeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static String resolveManualStatus(OffsetDateTime chkinDtm, OffsetDateTime chkoutDtm, String attndStsCd) {
        String normalized = normalizeOrNull(attndStsCd);
        if (normalized != null) {
            return normalized;
        }
        return chkoutDtm != null ? CHECKED_OUT : CHECKED_IN;
    }

    public static String fallbackDisplayValue(String attndStsCd) {
        String normalized = normalizeOrNull(attndStsCd);
        return normalized == null ? NONE : normalized;
    }
}

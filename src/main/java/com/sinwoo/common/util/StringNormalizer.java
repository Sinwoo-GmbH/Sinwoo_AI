package com.sinwoo.common.util;

/**
 * Common string normalization helpers shared by service implementations.
 *
 * <p>Methods are intentionally null-safe: null or blank inputs are treated as absent.
 */
public final class StringNormalizer {

    public static final String YN_Y = "Y";
    public static final String YN_N = "N";

    private StringNormalizer() {
    }

    /** Trim. Returns {@code null} when input is null or blank. */
    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Trim + upper-case. Returns {@code null} when input is null or blank. */
    public static String blankToNullUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    /** Trim + upper-case. Throws {@link NullPointerException} when input is null. */
    public static String trimAndUpper(String value) {
        return value.trim().toUpperCase();
    }

    /** Trim + upper-case. Returns {@code defaultValue} when input is null or blank. */
    public static String defaultIfBlankUpper(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim().toUpperCase();
    }

    /**
     * Normalize a Y/N flag. Returns "Y" when input is "y"/"Y" (case-insensitive),
     * otherwise "N". Null or blank input returns {@code defaultValue}.
     */
    public static String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return YN_Y.equalsIgnoreCase(value.trim()) ? YN_Y : YN_N;
    }
}

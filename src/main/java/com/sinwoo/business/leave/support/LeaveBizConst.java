package com.sinwoo.business.leave.support;

import java.util.Locale;
import java.util.Map;

public final class LeaveBizConst {

    public static final String STS_ACTIVE = "ACTIVE";
    public static final String STS_CANCELLED = "CANCELLED";

    public static final String APRV_SAVED = "SV";
    public static final String APRV_REQUESTED = "RQ";
    public static final String APRV_APPROVED = "AP";
    public static final String APRV_REJECTED = "RJ";
    public static final String APRV_WAITING = "WT";
    public static final String APRV_REFERENCE = "RF";

    public static final String APRV_TYPE_APPROVER = "APP";
    public static final String APRV_TYPE_REFERENCE = "REF";

    public static final String ROLE_REQUESTER = "RQT";
    public static final String ROLE_APPROVER = "APR";
    public static final String ROLE_REFERRER = "RFR";

    public static final String VAC_ANNUAL = "AN";
    public static final String VAC_SPECIAL = "SP";

    public static final String DAY_FULL = "AD";
    public static final String DAY_HALF_AM = "HAM";
    public static final String DAY_HALF_PM = "HPM";
    public static final String DAY_HALF_LEGACY = "HD";

    public static final String DEDUCT_Y = "Y";
    public static final String DEDUCT_N = "N";

    private static final Map<String, String> LEAVE_TYPE_TO_CD = Map.of(
            "annual leave", "AN",
            "sick leave", "SICK",
            "marriage leave", "MARR",
            "bereavement leave", "BERE",
            "unpaid leave", "UNPD",
            "special leave", "SP"
    );

    private static final Map<String, String> LEAVE_TYPE_TO_LABEL = Map.of(
            "AN", "Annual Leave",
            "SICK", "Sick Leave",
            "MARR", "Marriage Leave",
            "BERE", "Bereavement Leave",
            "UNPD", "Unpaid Leave",
            "SP", "Special Leave"
    );

    private static final Map<String, String> UNIT_TO_CD = Map.of(
            "full day", DAY_FULL,
            "half day am", DAY_HALF_AM,
            "half day pm", DAY_HALF_PM,
            "half day", DAY_HALF_LEGACY
    );

    private static final Map<String, String> UNIT_TO_LABEL = Map.of(
            DAY_FULL, "Full Day",
            DAY_HALF_AM, "Half Day AM",
            DAY_HALF_PM, "Half Day PM",
            DAY_HALF_LEGACY, "Half Day AM"
    );

    private LeaveBizConst() {
    }

    public static String normalizeLeaveType(String value) {
        if (value == null || value.isBlank()) {
            return VAC_ANNUAL;
        }
        String trimmed = value.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (LEAVE_TYPE_TO_LABEL.containsKey(upper)) {
            return upper;
        }
        return LEAVE_TYPE_TO_CD.getOrDefault(trimmed.toLowerCase(Locale.ROOT), VAC_SPECIAL);
    }

    public static String leaveTypeLabel(String code) {
        return LEAVE_TYPE_TO_LABEL.getOrDefault(normalizeCd(code), "Special Leave");
    }

    public static String normalizeDayType(String value) {
        if (value == null || value.isBlank()) {
            return DAY_FULL;
        }
        String trimmed = value.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (UNIT_TO_LABEL.containsKey(upper)) {
            return upper;
        }
        return UNIT_TO_CD.getOrDefault(trimmed.toLowerCase(Locale.ROOT), DAY_FULL);
    }

    public static String dayTypeLabel(String code) {
        return UNIT_TO_LABEL.getOrDefault(normalizeCd(code), "Full Day");
    }

    public static String normalizeDeductYn(String deductionType) {
        if (deductionType == null || deductionType.isBlank()) {
            return DEDUCT_Y;
        }
        String normalized = deductionType.trim().toLowerCase(Locale.ROOT);
        if ("n".equals(normalized)
                || "nded".equals(normalized)
                || "non-deducted leave".equals(normalized)
                || "non deducted leave".equals(normalized)) {
            return DEDUCT_N;
        }
        return DEDUCT_Y;
    }

    public static String deductionTypeLabel(String deductYn) {
        return DEDUCT_Y.equalsIgnoreCase(deductYn) ? "Deducted Leave" : "Non-deducted Leave";
    }

    public static String normalizeSaveStatus(String value) {
        if (value == null || value.isBlank()) {
            return APRV_SAVED;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("requested".equals(normalized) || APRV_REQUESTED.equalsIgnoreCase(value)) {
            return APRV_REQUESTED;
        }
        return APRV_SAVED;
    }

    public static String statusLabel(String stsCd, String apprStsCd) {
        if (STS_CANCELLED.equalsIgnoreCase(stsCd)) {
            return "Cancelled";
        }
        return switch (normalizeCd(apprStsCd)) {
            case APRV_SAVED -> "Draft";
            case APRV_REQUESTED -> "Requested";
            case APRV_APPROVED -> "Approved";
            case APRV_REJECTED -> "Rejected";
            default -> "Draft";
        };
    }

    public static String statusCdFromLabel(String status) {
        if (status == null || status.isBlank() || "All".equalsIgnoreCase(status)) {
            return null;
        }
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "draft", "saved", "sv" -> APRV_SAVED;
            case "requested", "request", "rq" -> APRV_REQUESTED;
            case "approved", "ap" -> APRV_APPROVED;
            case "rejected", "reject", "rj" -> APRV_REJECTED;
            case "cancelled", "canceled" -> STS_CANCELLED;
            default -> null;
        };
    }

    public static boolean isReservedStatus(String stsCd, String apprStsCd) {
        if (!STS_ACTIVE.equalsIgnoreCase(stsCd)) {
            return false;
        }
        return APRV_REQUESTED.equalsIgnoreCase(apprStsCd) || APRV_APPROVED.equalsIgnoreCase(apprStsCd);
    }

    public static boolean isHalfDay(String dayTpCd) {
        String normalized = normalizeDayType(dayTpCd);
        return DAY_HALF_AM.equals(normalized) || DAY_HALF_PM.equals(normalized) || DAY_HALF_LEGACY.equals(normalized);
    }

    private static String normalizeCd(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}

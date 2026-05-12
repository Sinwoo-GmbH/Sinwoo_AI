package com.sinwoo.common.security;

import java.util.List;

public record AuthenticatedUsr(
        Long usrId,
        Long tenantId,
        String tenantCd,
        Long coId,
        String tenantTpCd,
        String lgnId,
        String eml,
        String dspNm,
        String authGrpCd,
        String authLvlCd,
        List<String> roleCds
) {

    /**
     * Resolve a human-readable identifier for logging/audit:
     * {@code eml} → {@code lgnId} → {@code USR####} → {@code null}.
     * Email is preferred so audit columns (CRT_BY/UPD_BY, USR_KEY) carry
     * the user's recognizable login address.
     */
    public String resolveKey() {
        if (eml != null && !eml.isBlank()) {
            return eml;
        }
        if (lgnId != null && !lgnId.isBlank()) {
            return lgnId;
        }
        if (usrId != null) {
            return "USR%04d".formatted(usrId);
        }
        return null;
    }
}

package com.sinwoo.platform.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreatePayTxnRequest(
        @NotNull
        Long tenantId,

        @NotNull
        Long subsId,

        @NotBlank
        @Size(max = 20)
        String payTpCd,

        @Size(max = 20)
        String payStsCd,

        @DecimalMin("0.00")
        BigDecimal payAmt,

        @NotBlank
        @Size(max = 10)
        String currCd,

        @Size(max = 30)
        String pgCd,

        @Size(max = 100)
        String pgTxnNo,

        OffsetDateTime aprvDtm,

        @Size(max = 1000)
        String failMsg
) {
}

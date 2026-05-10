package com.sinwoo.platform.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateSubscrPlanRequest(
        @NotBlank
        @Size(max = 100)
        String planCd,

        @NotBlank
        @Size(max = 255)
        String planNm,

        @NotBlank
        @Size(max = 20)
        String tenantTpCd,

        @NotBlank
        @Size(max = 20)
        String billCyclCd,

        @NotBlank
        @Size(max = 10)
        String currCd,

        @DecimalMin("0.00")
        BigDecimal baseAmt,

        Integer usrLmtCnt,

        @Size(max = 1)
        String useYn
) {
}

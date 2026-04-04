package com.sinwoo.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotNull
        Long tenantId,

        @NotBlank
        @Size(max = 100)
        String planCd,

        @Size(max = 20)
        String subsStsCd,

        @Size(max = 1)
        String billFreeYn,

        @Size(max = 1)
        String autoPayYn,

        LocalDate strDt,

        LocalDate endDt,

        LocalDate nextBillDt
) {
}

package com.sinwoo.company.dto;

import java.util.List;

public record CompanyListResponse(
        long totCnt,
        List<CompanyResponse> itemList
) {
}

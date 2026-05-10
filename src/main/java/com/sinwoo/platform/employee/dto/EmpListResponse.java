package com.sinwoo.platform.emp.dto;

import java.util.List;

public record EmpListResponse(
        int totCnt,
        List<EmpResponse> itemList
) {
}

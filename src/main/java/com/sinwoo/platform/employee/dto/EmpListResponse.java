package com.sinwoo.platform.employee.dto;

import java.util.List;

public record EmpListResponse(
        int totCnt,
        List<EmpResponse> itemList
) {
}

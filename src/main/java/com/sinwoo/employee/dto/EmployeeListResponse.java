package com.sinwoo.employee.dto;

import java.util.List;

public record EmployeeListResponse(
        int totCnt,
        List<EmployeeResponse> itemList
) {
}

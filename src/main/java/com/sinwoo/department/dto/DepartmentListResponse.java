package com.sinwoo.department.dto;

import java.util.List;

public record DepartmentListResponse(
        int totCnt,
        List<DepartmentResponse> itemList
) {
}

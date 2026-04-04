package com.sinwoo.department.dto;

import java.util.List;

public record DepartmentTreeResponse(
        int totCnt,
        List<DepartmentNodeResponse> itemList
) {
}

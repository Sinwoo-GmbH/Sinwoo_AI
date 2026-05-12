package com.sinwoo.platform.department.dto;

import java.util.List;

public record DeptListResponse(
        int totCnt,
        List<DeptResponse> itemList
) {
}

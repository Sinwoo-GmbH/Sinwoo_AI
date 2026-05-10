package com.sinwoo.platform.dept.dto;

import java.util.List;

public record DeptListResponse(
        int totCnt,
        List<DeptResponse> itemList
) {
}

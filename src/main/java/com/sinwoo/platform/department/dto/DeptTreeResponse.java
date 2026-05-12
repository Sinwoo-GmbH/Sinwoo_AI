package com.sinwoo.platform.department.dto;

import java.util.List;

public record DeptTreeResponse(
        int totCnt,
        List<DeptNodeResponse> itemList
) {
}

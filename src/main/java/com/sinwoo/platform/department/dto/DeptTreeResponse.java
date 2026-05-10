package com.sinwoo.platform.dept.dto;

import java.util.List;

public record DeptTreeResponse(
        int totCnt,
        List<DeptNodeResponse> itemList
) {
}

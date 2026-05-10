package com.sinwoo.business.dto;

import java.util.Map;

public record BusinessRecordSaveRequest(
        Map<String, Object> values
) {
}

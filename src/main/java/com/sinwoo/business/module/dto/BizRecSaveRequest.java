package com.sinwoo.business.module.dto;

import java.util.Map;

public record BizRecSaveRequest(
        Map<String, Object> values
) {
}

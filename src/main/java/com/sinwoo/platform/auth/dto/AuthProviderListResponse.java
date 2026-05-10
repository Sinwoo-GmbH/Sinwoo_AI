package com.sinwoo.platform.auth.dto;

import java.util.List;

public record AuthProviderListResponse(
        int totCnt,
        List<AuthProviderResponse> itemList
) {
}

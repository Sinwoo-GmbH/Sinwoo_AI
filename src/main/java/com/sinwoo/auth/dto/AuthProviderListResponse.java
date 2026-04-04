package com.sinwoo.auth.dto;

import java.util.List;

public record AuthProviderListResponse(
        int totCnt,
        List<AuthProviderResponse> itemList
) {
}

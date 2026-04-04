package com.sinwoo.user.dto;

import java.util.List;

public record UserListResponse(
        long totCnt,
        List<UserResponse> itemList
) {
}

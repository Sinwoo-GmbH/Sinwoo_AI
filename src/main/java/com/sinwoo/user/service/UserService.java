package com.sinwoo.user.service;

import com.sinwoo.user.dto.CreateUserRequest;
import com.sinwoo.user.dto.UserListResponse;
import com.sinwoo.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserListResponse getUsers(Long tenantId, Long coId);
}

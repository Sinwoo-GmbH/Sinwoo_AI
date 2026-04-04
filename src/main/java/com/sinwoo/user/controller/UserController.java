package com.sinwoo.user.controller;

import com.sinwoo.user.dto.CreateUserRequest;
import com.sinwoo.user.dto.UserListResponse;
import com.sinwoo.user.dto.UserResponse;
import com.sinwoo.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    public UserListResponse getUsers(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long coId
    ) {
        return userService.getUsers(tenantId, coId);
    }
}

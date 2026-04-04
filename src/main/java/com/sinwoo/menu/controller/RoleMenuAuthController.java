package com.sinwoo.menu.controller;

import com.sinwoo.menu.dto.RoleMenuAuthListResponse;
import com.sinwoo.menu.dto.UpsertRoleMenuAuthRequest;
import com.sinwoo.menu.service.RoleMenuAuthService;
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
@RequestMapping("/api/v1/role-menu-auths")
@RequiredArgsConstructor
public class RoleMenuAuthController {

    private final RoleMenuAuthService roleMenuAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleMenuAuthListResponse upsertRoleMenuAuths(@Valid @RequestBody UpsertRoleMenuAuthRequest request) {
        return roleMenuAuthService.upsertRoleMenuAuths(request);
    }

    @GetMapping
    public RoleMenuAuthListResponse getRoleMenuAuths(@RequestParam String roleCd) {
        return roleMenuAuthService.getRoleMenuAuths(roleCd);
    }
}

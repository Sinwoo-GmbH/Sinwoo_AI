package com.sinwoo.platform.mnu.controller;

import com.sinwoo.platform.mnu.dto.RoleMnuAuthListResponse;
import com.sinwoo.platform.mnu.dto.UpsertRoleMnuAuthRequest;
import com.sinwoo.platform.mnu.service.RoleMnuAuthService;
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
@RequestMapping("/api/v1/role-mnu-auths")
@RequiredArgsConstructor
public class RoleMnuAuthController {

    private final RoleMnuAuthService roleMnuAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleMnuAuthListResponse upsertRoleMnuAuths(@Valid @RequestBody UpsertRoleMnuAuthRequest request) {
        return roleMnuAuthService.upsertRoleMnuAuths(request);
    }

    @GetMapping
    public RoleMnuAuthListResponse getRoleMnuAuths(@RequestParam String roleCd) {
        return roleMnuAuthService.getRoleMnuAuths(roleCd);
    }
}

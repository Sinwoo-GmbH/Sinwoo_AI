package com.sinwoo.worklocation.controller;

import com.sinwoo.auth.support.AuthErrorCode;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.worklocation.dto.CreateWorkLocationRequest;
import com.sinwoo.worklocation.dto.UpdateWorkLocationRequest;
import com.sinwoo.worklocation.dto.WorkLocationListResponse;
import com.sinwoo.worklocation.dto.WorkLocationResponse;
import com.sinwoo.worklocation.service.WorkLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-locations")
@RequiredArgsConstructor
public class WorkLocationController {

    private final WorkLocationService workLocationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkLocationResponse createWorkLocation(
            Authentication authentication,
            @Valid @RequestBody CreateWorkLocationRequest request
    ) {
        return workLocationService.createWorkLocation(requireAuthenticatedUser(authentication), request);
    }

    @PutMapping("/{workLocId}")
    public WorkLocationResponse updateWorkLocation(
            Authentication authentication,
            @PathVariable Long workLocId,
            @Valid @RequestBody UpdateWorkLocationRequest request
    ) {
        return workLocationService.updateWorkLocation(requireAuthenticatedUser(authentication), workLocId, request);
    }

    @GetMapping
    public WorkLocationListResponse getWorkLocations(
            Authentication authentication,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long coId
    ) {
        return workLocationService.getWorkLocations(requireAuthenticatedUser(authentication), tenantId, coId);
    }

    private AuthenticatedUser requireAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCode.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return authenticatedUser;
    }
}

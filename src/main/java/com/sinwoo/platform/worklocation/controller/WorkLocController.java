package com.sinwoo.platform.worklocation.controller;

import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.platform.worklocation.dto.CreateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.UpdateWorkLocRequest;
import com.sinwoo.platform.worklocation.dto.WorkLocListResponse;
import com.sinwoo.platform.worklocation.dto.WorkLocResponse;
import com.sinwoo.platform.worklocation.service.WorkLocService;
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
public class WorkLocController {

    private final WorkLocService workLocService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkLocResponse createWorkLoc(
            Authentication authentication,
            @Valid @RequestBody CreateWorkLocRequest request
    ) {
        return workLocService.createWorkLoc(requireAuthenticatedUsr(authentication), request);
    }

    @PutMapping("/{workLocId}")
    public WorkLocResponse updateWorkLoc(
            Authentication authentication,
            @PathVariable Long workLocId,
            @Valid @RequestBody UpdateWorkLocRequest request
    ) {
        return workLocService.updateWorkLoc(requireAuthenticatedUsr(authentication), workLocId, request);
    }

    @GetMapping
    public WorkLocListResponse getWorkLocs(
            Authentication authentication,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long coId
    ) {
        return workLocService.getWorkLocs(requireAuthenticatedUsr(authentication), tenantId, coId);
    }

    private AuthenticatedUsr requireAuthenticatedUsr(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUsr authenticatedUsr)) {
            throw new ApiException(
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.status(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.code(),
                    AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.message()
            );
        }
        return authenticatedUsr;
    }
}

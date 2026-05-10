package com.sinwoo.platform.code.controller;

import com.sinwoo.platform.code.dto.CdGroupListResponse;
import com.sinwoo.platform.code.dto.CdGroupResponse;
import com.sinwoo.platform.code.dto.CreateCdGroupRequest;
import com.sinwoo.platform.code.dto.UpdateCdGroupRequest;
import com.sinwoo.platform.code.service.CommonCdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/code-groups")
@RequiredArgsConstructor
public class CdGroupController {

    private final CommonCdService commonCdService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CdGroupResponse createCdGroup(@Valid @RequestBody CreateCdGroupRequest request) {
        return commonCdService.createCdGroup(request);
    }

    @PutMapping("/{grpId}")
    public CdGroupResponse updateCdGroup(
            @PathVariable Long grpId,
            @Valid @RequestBody UpdateCdGroupRequest request
    ) {
        return commonCdService.updateCdGroup(grpId, request);
    }

    @GetMapping
    public CdGroupListResponse getCdGroups() {
        return commonCdService.getCdGroups();
    }
}

package com.sinwoo.code.controller;

import com.sinwoo.code.dto.CodeGroupListResponse;
import com.sinwoo.code.dto.CodeGroupResponse;
import com.sinwoo.code.dto.CreateCodeGroupRequest;
import com.sinwoo.code.dto.UpdateCodeGroupRequest;
import com.sinwoo.code.service.CommonCodeService;
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
public class CodeGroupController {

    private final CommonCodeService commonCodeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CodeGroupResponse createCodeGroup(@Valid @RequestBody CreateCodeGroupRequest request) {
        return commonCodeService.createCodeGroup(request);
    }

    @PutMapping("/{grpId}")
    public CodeGroupResponse updateCodeGroup(
            @PathVariable Long grpId,
            @Valid @RequestBody UpdateCodeGroupRequest request
    ) {
        return commonCodeService.updateCodeGroup(grpId, request);
    }

    @GetMapping
    public CodeGroupListResponse getCodeGroups() {
        return commonCodeService.getCodeGroups();
    }
}

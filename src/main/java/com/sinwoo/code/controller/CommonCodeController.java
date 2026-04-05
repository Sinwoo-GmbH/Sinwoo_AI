package com.sinwoo.code.controller;

import com.sinwoo.code.dto.CommonCodeListResponse;
import com.sinwoo.code.dto.CommonCodeResponse;
import com.sinwoo.code.dto.CreateCommonCodeRequest;
import com.sinwoo.code.dto.UpdateCommonCodeRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonCodeResponse createCode(@Valid @RequestBody CreateCommonCodeRequest request) {
        return commonCodeService.createCode(request);
    }

    @PutMapping("/{cdId}")
    public CommonCodeResponse updateCode(
            @PathVariable Long cdId,
            @Valid @RequestBody UpdateCommonCodeRequest request
    ) {
        return commonCodeService.updateCode(cdId, request);
    }

    @GetMapping
    public CommonCodeListResponse getCodes(@RequestParam(required = false) String grpCd) {
        return commonCodeService.getCodes(grpCd);
    }
}

package com.sinwoo.platform.code.controller;

import com.sinwoo.platform.code.dto.CommonCdListResponse;
import com.sinwoo.platform.code.dto.CommonCdResponse;
import com.sinwoo.platform.code.dto.CreateCommonCdRequest;
import com.sinwoo.platform.code.dto.UpdateCommonCdRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/codes")
@RequiredArgsConstructor
public class CommonCdController {

    private final CommonCdService commonCdService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonCdResponse createCd(@Valid @RequestBody CreateCommonCdRequest request) {
        return commonCdService.createCd(request);
    }

    @PutMapping("/{cdId}")
    public CommonCdResponse updateCd(
            @PathVariable Long cdId,
            @Valid @RequestBody UpdateCommonCdRequest request
    ) {
        return commonCdService.updateCd(cdId, request);
    }

    @GetMapping
    public CommonCdListResponse getCds(@RequestParam(required = false) String grpCd) {
        return commonCdService.getCds(grpCd);
    }
}

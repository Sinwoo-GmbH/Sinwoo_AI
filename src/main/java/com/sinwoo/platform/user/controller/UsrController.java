package com.sinwoo.platform.user.controller;

import com.sinwoo.platform.user.dto.CreateUsrRequest;
import com.sinwoo.platform.user.dto.UsrListResponse;
import com.sinwoo.platform.user.dto.UsrResponse;
import com.sinwoo.platform.user.service.UsrService;
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
public class UsrController {

    private final UsrService usrService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsrResponse createUsr(@Valid @RequestBody CreateUsrRequest request) {
        return usrService.createUsr(request);
    }

    @GetMapping
    public UsrListResponse getUsrs(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long coId
    ) {
        return usrService.getUsrs(tenantId, coId);
    }
}

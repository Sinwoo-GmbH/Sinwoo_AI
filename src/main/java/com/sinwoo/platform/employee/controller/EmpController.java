package com.sinwoo.platform.emp.controller;

import com.sinwoo.platform.emp.dto.CreateEmpRequest;
import com.sinwoo.platform.emp.dto.EmpListResponse;
import com.sinwoo.platform.emp.dto.EmpResponse;
import com.sinwoo.platform.emp.service.EmpService;
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
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpResponse createEmp(@Valid @RequestBody CreateEmpRequest request) {
        return empService.createEmp(request);
    }

    @GetMapping
    public EmpListResponse getEmps(
            @RequestParam Long tenantId,
            @RequestParam Long coId,
            @RequestParam(required = false) Long deptId
    ) {
        return empService.getEmps(tenantId, coId, deptId);
    }
}

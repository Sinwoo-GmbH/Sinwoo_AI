package com.sinwoo.platform.department.controller;

import com.sinwoo.platform.department.dto.CreateDeptRequest;
import com.sinwoo.platform.department.dto.DeptListResponse;
import com.sinwoo.platform.department.dto.DeptResponse;
import com.sinwoo.platform.department.dto.DeptTreeResponse;
import com.sinwoo.platform.department.service.DeptService;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeptResponse createDept(@Valid @RequestBody CreateDeptRequest request) {
        return deptService.createDept(request);
    }

    @GetMapping
    public DeptListResponse getDepts(@RequestParam Long tenantId, @RequestParam Long coId) {
        return deptService.getDepts(tenantId, coId);
    }

    @GetMapping("/tree")
    public DeptTreeResponse getDeptTree(@RequestParam Long tenantId, @RequestParam Long coId) {
        return deptService.getDeptTree(tenantId, coId);
    }
}

package com.sinwoo.department.controller;

import com.sinwoo.department.dto.CreateDepartmentRequest;
import com.sinwoo.department.dto.DepartmentListResponse;
import com.sinwoo.department.dto.DepartmentResponse;
import com.sinwoo.department.dto.DepartmentTreeResponse;
import com.sinwoo.department.service.DepartmentService;
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
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return departmentService.createDepartment(request);
    }

    @GetMapping
    public DepartmentListResponse getDepartments(@RequestParam Long tenantId, @RequestParam Long coId) {
        return departmentService.getDepartments(tenantId, coId);
    }

    @GetMapping("/tree")
    public DepartmentTreeResponse getDepartmentTree(@RequestParam Long tenantId, @RequestParam Long coId) {
        return departmentService.getDepartmentTree(tenantId, coId);
    }
}

package com.sinwoo.employee.controller;

import com.sinwoo.employee.dto.CreateEmployeeRequest;
import com.sinwoo.employee.dto.EmployeeListResponse;
import com.sinwoo.employee.dto.EmployeeResponse;
import com.sinwoo.employee.service.EmployeeService;
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
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @GetMapping
    public EmployeeListResponse getEmployees(
            @RequestParam Long tenantId,
            @RequestParam Long coId,
            @RequestParam(required = false) Long deptId
    ) {
        return employeeService.getEmployees(tenantId, coId, deptId);
    }
}

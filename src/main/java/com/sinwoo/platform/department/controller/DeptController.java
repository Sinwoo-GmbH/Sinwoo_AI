package com.sinwoo.platform.department.controller;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.department.dto.DeptRequest;
import com.sinwoo.platform.department.dto.DeptResponse;
import com.sinwoo.platform.department.service.DeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeptResponse createDept(@AuthenticationPrincipal AuthenticatedUsr usr,
                                    @Valid @RequestBody DeptRequest request) {
        return deptService.createDept(usr, request);
    }

    @PutMapping("/{deptId}")
    public DeptResponse updateDept(@AuthenticationPrincipal AuthenticatedUsr usr,
                                    @PathVariable Long deptId,
                                    @Valid @RequestBody DeptRequest request) {
        return deptService.updateDept(usr, deptId, request);
    }

    @DeleteMapping("/{deptId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDept(@AuthenticationPrincipal AuthenticatedUsr usr,
                            @PathVariable Long deptId) {
        deptService.deleteDept(usr, deptId);
    }

    @GetMapping
    public DeptResponse.ListWrap getDepts(@AuthenticationPrincipal AuthenticatedUsr usr) {
        return deptService.getDepts(usr);
    }

    @GetMapping("/tree")
    public DeptResponse.TreeWrap getDeptTree(@AuthenticationPrincipal AuthenticatedUsr usr) {
        return deptService.getDeptTree(usr);
    }

    @GetMapping("/{deptId}/emp-count")
    public DeptResponse.EmpCount getEmpCount(@AuthenticationPrincipal AuthenticatedUsr usr,
                                              @PathVariable Long deptId) {
        return deptService.getEmpCount(usr, deptId);
    }
}

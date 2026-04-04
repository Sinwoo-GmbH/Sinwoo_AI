package com.sinwoo.department.service;

import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.department.domain.Department;
import com.sinwoo.department.dto.CreateDepartmentRequest;
import com.sinwoo.department.dto.DepartmentListResponse;
import com.sinwoo.department.dto.DepartmentNodeResponse;
import com.sinwoo.department.dto.DepartmentResponse;
import com.sinwoo.department.dto.DepartmentTreeResponse;
import com.sinwoo.department.repository.DepartmentRepository;
import com.sinwoo.tenant.repository.TenantRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        validateTenant(request.tenantId());
        validateCompany(request.tenantId(), request.coId());

        Department parentDepartment = resolveParentDepartment(request.tenantId(), request.coId(), request.upDeptId());
        String normalizedDeptCd = normalizeCode(request.deptCd());
        if (departmentRepository.existsByTenantIdAndCoIdAndDeptCdIgnoreCase(request.tenantId(), request.coId(), normalizedDeptCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists in company");
        }

        Department department = Department.create(
                request.tenantId(),
                request.coId(),
                normalizedDeptCd,
                request.deptNm().trim(),
                request.upDeptId(),
                parentDepartment == null ? 1 : parentDepartment.getDeptLvlNo() + 1,
                normalizeStatus(request.stsCd())
        );

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    public DepartmentListResponse getDepartments(Long tenantId, Long coId) {
        validateTenant(tenantId);
        validateCompany(tenantId, coId);

        List<DepartmentResponse> items = departmentRepository.findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, coId).stream()
                .map(DepartmentResponse::from)
                .toList();

        return new DepartmentListResponse(items.size(), items);
    }

    @Override
    public DepartmentTreeResponse getDepartmentTree(Long tenantId, Long coId) {
        validateTenant(tenantId);
        validateCompany(tenantId, coId);

        List<Department> departments = departmentRepository.findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, coId);
        Map<Long, DepartmentNodeResponse> nodeById = new LinkedHashMap<>();
        departments.forEach(department -> nodeById.put(department.getId(), DepartmentNodeResponse.from(department)));

        List<DepartmentNodeResponse> roots = new ArrayList<>();
        for (Department department : departments) {
            DepartmentNodeResponse node = nodeById.get(department.getId());
            if (department.getUpDeptId() == null || !nodeById.containsKey(department.getUpDeptId())) {
                roots.add(node);
                continue;
            }
            nodeById.get(department.getUpDeptId()).childList().add(node);
        }

        return new DepartmentTreeResponse(roots.size(), roots);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCompany(Long tenantId, Long coId) {
        if (coId == null || companyRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found in tenant");
        }
    }

    private Department resolveParentDepartment(Long tenantId, Long coId, Long upDeptId) {
        if (upDeptId == null) {
            return null;
        }
        return departmentRepository.findByIdAndTenantIdAndCoId(upDeptId, tenantId, coId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent department not found in company"));
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }
}

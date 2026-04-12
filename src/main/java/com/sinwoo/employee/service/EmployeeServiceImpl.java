package com.sinwoo.employee.service;

import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.department.repository.DepartmentRepository;
import com.sinwoo.employee.domain.Employee;
import com.sinwoo.employee.dto.CreateEmployeeRequest;
import com.sinwoo.employee.dto.EmployeeListResponse;
import com.sinwoo.employee.dto.EmployeeResponse;
import com.sinwoo.employee.repository.EmployeeRepository;
import com.sinwoo.tenant.repository.TenantRepository;
import com.sinwoo.user.domain.User;
import com.sinwoo.user.repository.UserRepository;
import com.sinwoo.worklocation.repository.WorkLocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final WorkLocationRepository workLocationRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        validateTenant(request.tenantId());
        validateCompany(request.tenantId(), request.coId());
        validateDepartment(request.tenantId(), request.coId(), request.deptId());
        validateWorkLocation(request.tenantId(), request.coId(), request.workLocId());
        validateManager(request.tenantId(), request.coId(), request.mgrEmpId());
        validateUser(request.tenantId(), request.coId(), request.usrId());

        String normalizedEmpNo = request.empNo().trim().toUpperCase();
        if (employeeRepository.existsByTenantIdAndCoIdAndEmpNoIgnoreCase(request.tenantId(), request.coId(), normalizedEmpNo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee number already exists in company");
        }

        if (request.usrId() != null && employeeRepository.existsByUsrId(request.usrId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already linked to employee");
        }

        Employee employee = Employee.create(
                request.tenantId(),
                request.coId(),
                request.usrId(),
                request.deptId(),
                request.workLocId(),
                request.mgrEmpId(),
                normalizedEmpNo,
                request.empNm().trim(),
                normalizeTeamRole(request.teamRoleCd()),
                blankToNull(request.jobTtlNm()),
                request.hireDt(),
                request.retrDt(),
                normalizeStatus(request.stsCd())
        );

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Override
    public EmployeeListResponse getEmployees(Long tenantId, Long coId, Long deptId) {
        validateTenant(tenantId);
        validateCompany(tenantId, coId);
        validateDepartment(tenantId, coId, deptId);

        List<EmployeeResponse> items = (deptId == null
                ? employeeRepository.findAllByTenantIdAndCoIdOrderByEmpNmAscIdAsc(tenantId, coId)
                : employeeRepository.findAllByTenantIdAndCoIdAndDeptIdOrderByEmpNmAscIdAsc(tenantId, coId, deptId))
                .stream()
                .map(EmployeeResponse::from)
                .toList();

        return new EmployeeListResponse(items.size(), items);
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

    private void validateDepartment(Long tenantId, Long coId, Long deptId) {
        if (deptId == null) {
            return;
        }
        if (departmentRepository.findByIdAndTenantIdAndCoId(deptId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found in company");
        }
    }

    private void validateWorkLocation(Long tenantId, Long coId, Long workLocId) {
        if (workLocId == null) {
            return;
        }
        if (workLocationRepository.findByIdAndTenantIdAndCoId(workLocId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work location not found in company");
        }
    }

    private void validateManager(Long tenantId, Long coId, Long mgrEmpId) {
        if (mgrEmpId == null) {
            return;
        }
        if (employeeRepository.findByIdAndTenantIdAndCoId(mgrEmpId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager employee not found in company");
        }
    }

    private void validateUser(Long tenantId, Long coId, Long usrId) {
        if (usrId == null) {
            return;
        }

        User user = userRepository.findById(usrId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (!tenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found in tenant");
        }

        if (user.getCoId() == null || !coId.equals(user.getCoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found in company");
        }
    }

    private String normalizeTeamRole(String value) {
        if (value == null || value.isBlank()) {
            return "TEAM_MEMBER";
        }
        String normalized = value.trim().toUpperCase();
        if (!List.of("TEAM_MEMBER", "TEAM_LEADER").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported team role");
        }
        return normalized;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

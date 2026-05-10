package com.sinwoo.platform.emp.service;

import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.dept.repository.DeptRepository;
import com.sinwoo.platform.emp.domain.Emp;
import com.sinwoo.platform.emp.dto.CreateEmpRequest;
import com.sinwoo.platform.emp.dto.EmpListResponse;
import com.sinwoo.platform.emp.dto.EmpResponse;
import com.sinwoo.platform.emp.repository.EmpRepository;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.user.domain.Usr;
import com.sinwoo.platform.user.repository.UsrRepository;
import com.sinwoo.platform.worklocation.repository.WorkLocRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpServiceImpl implements EmpService {

    private final EmpRepository empRepository;
    private final TenantRepository tenantRepository;
    private final CoRepository coRepository;
    private final DeptRepository deptRepository;
    private final UsrRepository usrRepository;
    private final WorkLocRepository workLocRepository;

    @Override
    @Transactional
    public EmpResponse createEmp(CreateEmpRequest request) {
        validateTenant(request.tenantId());
        validateCo(request.tenantId(), request.coId());
        validateDept(request.tenantId(), request.coId(), request.deptId());
        validateWorkLoc(request.tenantId(), request.coId(), request.workLocId());
        validateManager(request.tenantId(), request.coId(), request.mgrEmpId());
        validateUsr(request.tenantId(), request.coId(), request.usrId());

        String normalizedEmpNo = request.empNo().trim().toUpperCase();
        if (empRepository.existsByTenantIdAndCoIdAndEmpNoIgnoreCase(request.tenantId(), request.coId(), normalizedEmpNo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Emp number already exists in company");
        }

        if (request.usrId() != null && empRepository.existsByUsrId(request.usrId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usr is already linked to emp");
        }

        Emp emp = Emp.create(
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

        return EmpResponse.from(empRepository.save(emp));
    }

    @Override
    public EmpListResponse getEmps(Long tenantId, Long coId, Long deptId) {
        validateTenant(tenantId);
        validateCo(tenantId, coId);
        validateDept(tenantId, coId, deptId);

        List<EmpResponse> items = (deptId == null
                ? empRepository.findAllByTenantIdAndCoIdOrderByEmpNmAscIdAsc(tenantId, coId)
                : empRepository.findAllByTenantIdAndCoIdAndDeptIdOrderByEmpNmAscIdAsc(tenantId, coId, deptId))
                .stream()
                .map(EmpResponse::from)
                .toList();

        return new EmpListResponse(items.size(), items);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCo(Long tenantId, Long coId) {
        if (coId == null || coRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Co not found in tenant");
        }
    }

    private void validateDept(Long tenantId, Long coId, Long deptId) {
        if (deptId == null) {
            return;
        }
        if (deptRepository.findByIdAndTenantIdAndCoId(deptId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dept not found in company");
        }
    }

    private void validateWorkLoc(Long tenantId, Long coId, Long workLocId) {
        if (workLocId == null) {
            return;
        }
        if (workLocRepository.findByIdAndTenantIdAndCoId(workLocId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work location not found in company");
        }
    }

    private void validateManager(Long tenantId, Long coId, Long mgrEmpId) {
        if (mgrEmpId == null) {
            return;
        }
        if (empRepository.findByIdAndTenantIdAndCoId(mgrEmpId, tenantId, coId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manager emp not found in company");
        }
    }

    private void validateUsr(Long tenantId, Long coId, Long usrId) {
        if (usrId == null) {
            return;
        }

        Usr user = usrRepository.findById(usrId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usr not found"));

        if (!tenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usr not found in tenant");
        }

        if (user.getCoId() == null || !coId.equals(user.getCoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usr not found in company");
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

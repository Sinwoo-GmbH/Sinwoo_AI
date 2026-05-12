package com.sinwoo.platform.department.service;

import static com.sinwoo.common.util.StringNormalizer.defaultIfBlankUpper;
import static com.sinwoo.common.util.StringNormalizer.trimAndUpper;

import com.sinwoo.platform.department.domain.Dept;
import com.sinwoo.platform.department.dto.CreateDeptRequest;
import com.sinwoo.platform.department.dto.DeptListResponse;
import com.sinwoo.platform.department.dto.DeptNodeResponse;
import com.sinwoo.platform.department.dto.DeptResponse;
import com.sinwoo.platform.department.dto.DeptTreeResponse;
import com.sinwoo.platform.department.repository.DeptRepository;
import com.sinwoo.platform.support.PlatformRefValidator;
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
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final PlatformRefValidator refValidator;

    @Override
    @Transactional
    public DeptResponse createDept(CreateDeptRequest request) {
        refValidator.requireTenantExists(request.tenantId());
        refValidator.requireCoInTenant(request.tenantId(), request.coId());

        Dept parentDept = resolveParentDept(request.tenantId(), request.coId(), request.upDeptId());
        String normalizedDeptCd = trimAndUpper(request.deptCd());
        if (deptRepository.existsByTenantIdAndCoIdAndDeptCdIgnoreCase(request.tenantId(), request.coId(), normalizedDeptCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Dept code already exists in company");
        }

        Dept dept = Dept.create(
                request.tenantId(),
                request.coId(),
                normalizedDeptCd,
                request.deptNm().trim(),
                request.upDeptId(),
                parentDept == null ? 1 : parentDept.getDeptLvlNo() + 1,
                defaultIfBlankUpper(request.stsCd(), "ACTIVE")
        );

        return DeptResponse.from(deptRepository.save(dept));
    }

    @Override
    public DeptListResponse getDepts(Long tenantId, Long coId) {
        refValidator.requireTenantExists(tenantId);
        refValidator.requireCoInTenant(tenantId, coId);

        List<DeptResponse> items = deptRepository.findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, coId).stream()
                .map(DeptResponse::from)
                .toList();

        return new DeptListResponse(items.size(), items);
    }

    @Override
    public DeptTreeResponse getDeptTree(Long tenantId, Long coId) {
        refValidator.requireTenantExists(tenantId);
        refValidator.requireCoInTenant(tenantId, coId);

        List<Dept> depts = deptRepository.findAllByTenantIdAndCoIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, coId);
        Map<Long, DeptNodeResponse> nodeById = new LinkedHashMap<>();
        depts.forEach(dept -> nodeById.put(dept.getId(), DeptNodeResponse.from(dept)));

        List<DeptNodeResponse> roots = new ArrayList<>();
        for (Dept dept : depts) {
            DeptNodeResponse node = nodeById.get(dept.getId());
            if (dept.getUpDeptId() == null || !nodeById.containsKey(dept.getUpDeptId())) {
                roots.add(node);
                continue;
            }
            nodeById.get(dept.getUpDeptId()).childList().add(node);
        }

        return new DeptTreeResponse(roots.size(), roots);
    }

    private Dept resolveParentDept(Long tenantId, Long coId, Long upDeptId) {
        if (upDeptId == null) {
            return null;
        }
        return deptRepository.findByIdAndTenantIdAndCoId(upDeptId, tenantId, coId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent dept not found in company"));
    }
}

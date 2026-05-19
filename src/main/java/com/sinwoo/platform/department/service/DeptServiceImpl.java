package com.sinwoo.platform.department.service;

import static com.sinwoo.common.util.StringNormalizer.defaultIfBlankUpper;
import static com.sinwoo.common.util.StringNormalizer.trimAndUpper;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.department.domain.Dept;
import com.sinwoo.platform.department.dto.DeptRequest;
import com.sinwoo.platform.department.dto.DeptResponse;
import com.sinwoo.platform.department.repository.DeptRepository;
import com.sinwoo.platform.employee.repository.EmpRepository;
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
    private final EmpRepository empRepository;

    // ── Create ──────────────────────────────────────────────

    @Override
    @Transactional
    public DeptResponse createDept(AuthenticatedUsr usr, DeptRequest request) {
        Long tid = usr.tenantId();
        Long cid = usr.coId();

        Dept parentDept = resolveParent(tid, cid, request.upDeptId());
        String normalizedCd = trimAndUpper(request.deptCd());

        if (deptRepository.existsByCd(tid, cid, normalizedCd)) {
            throw conflict("Dept code already exists in company");
        }

        int lvl = parentDept == null ? 1 : parentDept.getDeptLvlNo() + 1;
        // sort: 같은 부모 아래 마지막+1
        int dspOrd = request.dspOrd() != null
                ? request.dspOrd()
                : calcNextDspOrd(tid, cid, request.upDeptId());

        Dept dept = Dept.create(
                tid, cid, normalizedCd, request.deptNm().trim(),
                request.upDeptId(), lvl,
                defaultIfBlankUpper(request.stsCd(), "ACTIVE"),
                request.regionCd(), request.vacCnt(), request.vacInc(), dspOrd
        );

        // 같은 레벨 형제들 sort 밀기 (insert mode)
        bumpSortAfter(tid, cid, request.upDeptId(), dspOrd, null);

        return DeptResponse.from(deptRepository.save(dept));
    }

    // ── Update ──────────────────────────────────────────────

    @Override
    @Transactional
    public DeptResponse updateDept(AuthenticatedUsr usr, Long deptId, DeptRequest request) {
        Long tid = usr.tenantId();
        Long cid = usr.coId();

        Dept dept = deptRepository.findOne(deptId, tid, cid)
                .orElseThrow(() -> notFound("Dept not found"));

        Dept parentDept = resolveParent(tid, cid, request.upDeptId());
        int newLvl = parentDept == null ? 1 : parentDept.getDeptLvlNo() + 1;
        int newDspOrd = request.dspOrd() != null
                ? request.dspOrd()
                : calcNextDspOrd(tid, cid, request.upDeptId());

        Long oldUpDeptId = dept.getUpDeptId();
        boolean parentChanged = !equalsNullable(oldUpDeptId, request.upDeptId());

        if (parentChanged) {
            // 새 부모 쪽 sort 밀기
            bumpSortAfter(tid, cid, request.upDeptId(), newDspOrd, null);
            // 옛 부모 쪽 sort 당기기
            shrinkSortAfter(tid, cid, oldUpDeptId, dept.getDspOrd());
        }

        dept.update(request.deptNm().trim(), request.upDeptId(), newLvl,
                request.regionCd(), request.vacCnt(), request.vacInc(), newDspOrd);

        return DeptResponse.from(deptRepository.save(dept));
    }

    // ── Delete ──────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteDept(AuthenticatedUsr usr, Long deptId) {
        Long tid = usr.tenantId();
        Long cid = usr.coId();

        Dept dept = deptRepository.findOne(deptId, tid, cid)
                .orElseThrow(() -> notFound("Dept not found"));

        // 자식 부서 체크
        if (deptRepository.countChildren(tid, cid, deptId) > 0) {
            throw badRequest("Cannot delete: child departments exist");
        }

        // 소속 직원 체크
        long empCnt = empRepository.findByDept(tid, cid, deptId).size();
        if (empCnt > 0) {
            throw badRequest("Cannot delete: " + empCnt + " employee(s) belong to this department");
        }

        Long upDeptId = dept.getUpDeptId();
        int oldDspOrd = dept.getDspOrd();

        // 물리적 삭제
        deptRepository.delete(dept);

        // 형제 sort 당기기
        shrinkSortAfter(tid, cid, upDeptId, oldDspOrd);
    }

    // ── Read ────────────────────────────────────────────────

    @Override
    public DeptResponse.ListWrap getDepts(AuthenticatedUsr usr) {
        List<DeptResponse> items = deptRepository.findByCo(usr.tenantId(), usr.coId()).stream()
                .map(DeptResponse::from)
                .toList();
        return new DeptResponse.ListWrap(items.size(), items);
    }

    @Override
    public DeptResponse.TreeWrap getDeptTree(AuthenticatedUsr usr) {
        List<Dept> depts = deptRepository.findByCo(usr.tenantId(), usr.coId());
        Map<Long, DeptResponse.Node> nodeById = new LinkedHashMap<>();
        depts.forEach(d -> nodeById.put(d.getId(), DeptResponse.Node.from(d)));

        List<DeptResponse.Node> roots = new ArrayList<>();
        for (Dept d : depts) {
            DeptResponse.Node node = nodeById.get(d.getId());
            if (d.getUpDeptId() == null || !nodeById.containsKey(d.getUpDeptId())) {
                roots.add(node);
                continue;
            }
            nodeById.get(d.getUpDeptId()).childList().add(node);
        }
        return new DeptResponse.TreeWrap(roots.size(), roots);
    }

    @Override
    public DeptResponse.EmpCount getEmpCount(AuthenticatedUsr usr, Long deptId) {
        int cnt = empRepository.findByDept(usr.tenantId(), usr.coId(), deptId).size();
        return new DeptResponse.EmpCount(deptId, cnt);
    }

    // ── Helpers ─────────────────────────────────────────────

    private Dept resolveParent(Long tid, Long cid, Long upDeptId) {
        if (upDeptId == null) return null;
        return deptRepository.findOne(upDeptId, tid, cid)
                .orElseThrow(() -> badRequest("Parent dept not found"));
    }

    private int calcNextDspOrd(Long tid, Long cid, Long upDeptId) {
        if (upDeptId == null) {
            // 루트 레벨: 전체 루트 부서 수 + 1
            return (int) deptRepository.findByCo(tid, cid).stream()
                    .filter(d -> d.getUpDeptId() == null)
                    .count() + 1;
        }
        return deptRepository.countChildren(tid, cid, upDeptId) + 1;
    }

    /** insert mode: sort >= dspOrd인 형제들 +1 */
    private void bumpSortAfter(Long tid, Long cid, Long upDeptId, int dspOrd, Long excludeId) {
        List<Dept> siblings = upDeptId == null
                ? deptRepository.findByCo(tid, cid).stream().filter(d -> d.getUpDeptId() == null).toList()
                : deptRepository.findChildren(tid, cid, upDeptId);

        for (Dept s : siblings) {
            if (excludeId != null && excludeId.equals(s.getId())) continue;
            if (s.getDspOrd() >= dspOrd) {
                s.updateDspOrd(s.getDspOrd() + 1);
            }
        }
    }

    /** delete mode: sort > dspOrd인 형제들 -1 */
    private void shrinkSortAfter(Long tid, Long cid, Long upDeptId, int oldDspOrd) {
        List<Dept> siblings = upDeptId == null
                ? deptRepository.findByCo(tid, cid).stream().filter(d -> d.getUpDeptId() == null).toList()
                : deptRepository.findChildren(tid, cid, upDeptId);

        for (Dept s : siblings) {
            if (s.getDspOrd() > oldDspOrd) {
                s.updateDspOrd(s.getDspOrd() - 1);
            }
        }
    }

    private boolean equalsNullable(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}

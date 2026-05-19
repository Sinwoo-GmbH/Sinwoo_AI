package com.sinwoo.platform.leave.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.aprv.domain.AprvLine;
import com.sinwoo.platform.aprv.repository.AprvLineRepository;
import com.sinwoo.platform.code.domain.CdGroup;
import com.sinwoo.platform.code.domain.CommonCd;
import com.sinwoo.platform.code.repository.CdGroupRepository;
import com.sinwoo.platform.code.repository.CommonCdRepository;
import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.department.domain.Dept;
import com.sinwoo.platform.department.repository.DeptRepository;
import com.sinwoo.platform.employee.domain.Emp;
import com.sinwoo.platform.employee.repository.EmpRepository;
import com.sinwoo.platform.hol.domain.CoHol;
import com.sinwoo.platform.hol.domain.RgnHol;
import com.sinwoo.platform.hol.repository.CoHolRepository;
import com.sinwoo.platform.hol.repository.RgnHolRepository;
import com.sinwoo.platform.leave.domain.LeaveCoPolicy;
import com.sinwoo.platform.leave.domain.LeaveGrant;
import com.sinwoo.platform.leave.domain.LeaveReq;
import com.sinwoo.platform.leave.dto.LeaveRequest;
import com.sinwoo.platform.leave.dto.LeaveResponse;
import com.sinwoo.platform.leave.repository.LeaveCoPolicyRepository;
import com.sinwoo.platform.leave.repository.LeaveGrantRepository;
import com.sinwoo.platform.leave.repository.LeaveReqRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveReqServiceImpl implements LeaveReqService {

    private static final String REQ_TP_LEAVE = "LV";

    private final LeaveReqRepository leaveReqRepository;
    private final AprvLineRepository aprvLineRepository;
    private final EmpRepository empRepository;
    private final CoRepository coRepository;
    private final DeptRepository deptRepository;
    private final CdGroupRepository cdGroupRepository;
    private final CommonCdRepository commonCdRepository;
    private final RgnHolRepository rgnHolRepository;
    private final CoHolRepository coHolRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveCoPolicyRepository leaveCoPolicyRepository;

    // ── Context ─────────────────────────────────────────────

    @Override
    public LeaveResponse.Context getContext(AuthenticatedUsr usr) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        String deptNm = emp.getDeptId() != null
                ? deptRepository.findById(emp.getDeptId()).map(Dept::getDeptNm).orElse("")
                : "";

        var applicant = new LeaveResponse.Applicant(emp.getEmpNm(), deptNm, emp.getJobTtlCd());
        var balance = calcBalance(usr.tenantCd(), co.getCoCd(), emp.getEmpNo());

        return new LeaveResponse.Context(
                applicant, balance,
                loadCdList("VAC_TP"),
                loadCdList("DEDUCT_TP"),
                loadCdList("LEAVE_UNIT"),
                loadCdList("APRV_STS"),
                buildOrgTree(usr),
                buildEmpList(usr)
        );
    }

    // ── List ────────────────────────────────────────────────

    @Override
    public LeaveResponse.ItemList getMyLeaves(AuthenticatedUsr usr, String startDateFrom, String startDateTo, String status) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        String tenantCd = usr.tenantCd();
        String coCd = co.getCoCd();
        String empNo = emp.getEmpNo();

        List<LeaveReq> reqs;
        if (startDateFrom != null && !startDateFrom.isBlank() && startDateTo != null && !startDateTo.isBlank()) {
            reqs = leaveReqRepository
                    .findByEmpPeriod(
                            tenantCd, coCd, empNo,
                            LocalDate.parse(startDateFrom), LocalDate.parse(startDateTo), "N");
        } else {
            reqs = leaveReqRepository.findByEmp(
                    tenantCd, coCd, empNo, "N");
        }

        if (status != null && !status.isBlank() && !"All".equalsIgnoreCase(status)) {
            reqs = reqs.stream().filter(r -> status.equals(r.getStsCd())).toList();
        }

        var balance = calcBalance(tenantCd, coCd, empNo);
        List<LeaveResponse.Item> items = new ArrayList<>();
        int no = 1;
        for (LeaveReq req : reqs) {
            items.add(toItem(req, no++, empNo));
        }

        return new LeaveResponse.ItemList(balance, items);
    }

    // ── Detail ──────────────────────────────────────────────

    @Override
    public LeaveResponse.Item getLeave(AuthenticatedUsr usr, Long leaveId) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        return toItem(req, 0, emp.getEmpNo());
    }

    // ── Create ──────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse.Item createLeave(AuthenticatedUsr usr, LeaveRequest request) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        String tenantCd = usr.tenantCd();
        String coCd = co.getCoCd();
        String empNo = emp.getEmpNo();

        LocalDate strDt = LocalDate.parse(request.startDate());
        LocalDate endDt = LocalDate.parse(request.endDate());
        BigDecimal useDays = calcBusinessDays(usr, emp, co, request.leaveUnit(), strDt, endDt);
        String stsCd = mapNextStatus(request.nextStatus());

        if ("REQ".equals(stsCd)) {
            validateLeaveRequest(tenantCd, coCd, empNo, request, strDt, endDt, useDays, null);
        }

        LeaveReq req = LeaveReq.create(
                tenantCd, coCd, empNo,
                request.leaveType(), request.deductionType(), request.leaveUnit(),
                strDt, endDt, useDays, null, null,
                request.reason(), request.attachmentName(), stsCd
        );
        req = leaveReqRepository.save(req);

        saveAprvLines(tenantCd, coCd, req.getId(), request.approvalSteps(), request.ccIds(), stsCd);
        return toItem(req, 0, empNo);
    }

    // ── Update ──────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse.Item updateLeave(AuthenticatedUsr usr, Long leaveId, LeaveRequest request) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        if (!"DRF".equals(req.getStsCd())) {
            throw badRequest("Only draft can be updated");
        }

        LocalDate strDt = LocalDate.parse(request.startDate());
        LocalDate endDt = LocalDate.parse(request.endDate());
        BigDecimal useDays = calcBusinessDays(usr, emp, co, request.leaveUnit(), strDt, endDt);
        String stsCd = mapNextStatus(request.nextStatus());

        if ("REQ".equals(stsCd)) {
            validateLeaveRequest(usr.tenantCd(), co.getCoCd(), emp.getEmpNo(), request, strDt, endDt, useDays, req.getId());
        }

        req.update(
                request.leaveType(), request.deductionType(), request.leaveUnit(),
                strDt, endDt, useDays, null, null,
                request.reason(), request.attachmentName()
        );
        req.changeStatus(stsCd);

        aprvLineRepository.deleteAllByReqTpCdAndReqId(REQ_TP_LEAVE, req.getId());
        saveAprvLines(usr.tenantCd(), co.getCoCd(), req.getId(), request.approvalSteps(), request.ccIds(), stsCd);

        return toItem(req, 0, emp.getEmpNo());
    }

    // ── Delete ──────────────────────────────────────────────```````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````````

    @Override
    @Transactional
    public void deleteLeave(AuthenticatedUsr usr, Long leaveId) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        // 본인 신청 건만 삭제 가능
        if (!emp.getEmpNo().equals(req.getEmpId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete other's leave request");
        }

        boolean canDelete = isDeletable(req);
        if (!canDelete) {
            throw badRequest("Leave cannot be deleted");
        }

        // 결재선 물리적 삭제
        aprvLineRepository.deleteAllByReqTpCdAndReqId(REQ_TP_LEAVE, req.getId());

        String sts = req.getStsCd();
        if ("CAN".equals(sts) || "REJ".equals(sts)) {
            // 취소/반려 건은 물리적 삭제
            leaveReqRepository.delete(req);
        } else {
            // DRF, 자동승인(No-Approver) 건은 soft delete
            req.softDelete();
        }
    }

    /**
     * 삭제 가능 조건:
     * 1) Draft 상태 (DRF) — 언제든 삭제 가능
     * 2) 취소(CAN) / 반려(REJ) 상태 — 물리적 삭제 (이미 무효화된 건)
     * 3) 결재자 없음(No Approver) + 휴가 시작일 전 — 신청자 단독 삭제 가능
     *    (자동 승인된 APR 건이지만 결재자가 없었고 아직 시작 안 한 경우)
     */
    private boolean isDeletable(LeaveReq req) {
        String sts = req.getStsCd();
        if ("DRF".equals(sts) || "CAN".equals(sts) || "REJ".equals(sts)) {
            return true;
        }

        List<AprvLine> lines = aprvLineRepository.findByReq(
                REQ_TP_LEAVE, req.getId(), "N");
        boolean hasApprover = lines.stream().anyMatch(l -> "APP".equals(l.getAprvTpCd()));
        boolean beforeStart = req.getStrDt() != null && LocalDate.now().isBefore(req.getStrDt());

        return !hasApprover && beforeStart;
    }

    // ── Cancel ──────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse.Item cancelLeave(AuthenticatedUsr usr, Long leaveId) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        if (!"REQ".equals(req.getStsCd()) && !"APR".equals(req.getStsCd())) {
            throw badRequest("Only requested or approved can be cancelled");
        }
        req.changeStatus("CAN");
        return toItem(req, 0, emp.getEmpNo());
    }

    // ── Confirm (Approve) ───────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse.Item confirmLeave(AuthenticatedUsr usr, Long leaveId) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        if (!"REQ".equals(req.getStsCd())) {
            throw badRequest("Only requested leave can be approved");
        }

        List<AprvLine> lines = aprvLineRepository.findByReq(
                REQ_TP_LEAVE, req.getId(), "N");

        AprvLine myLine = lines.stream()
                .filter(l -> "APP".equals(l.getAprvTpCd()))
                .filter(l -> emp.getEmpNo().equals(l.getEmpId()))
                .filter(l -> "WAT".equals(l.getStsCd()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not an active approver for this request"));

        myLine.changeStatus("APR");

        boolean allApproved = lines.stream()
                .filter(l -> "APP".equals(l.getAprvTpCd()))
                .allMatch(l -> "APR".equals(l.getStsCd()) || (l.getId().equals(myLine.getId())));
        if (allApproved) {
            req.changeStatus("APR");
        }

        return toItem(req, 0, emp.getEmpNo());
    }

    // ── Reject ──────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse.Item rejectLeave(AuthenticatedUsr usr, Long leaveId, String rejectReason) {
        if (rejectReason == null || rejectReason.isBlank()) {
            throw badRequest("Reject reason is required");
        }

        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LeaveReq req = leaveReqRepository.findOne(
                        leaveId, usr.tenantCd(), co.getCoCd(), "N")
                .orElseThrow(() -> notFound("Leave request not found"));

        if (!"REQ".equals(req.getStsCd())) {
            throw badRequest("Only requested leave can be rejected");
        }

        List<AprvLine> lines = aprvLineRepository.findByReq(
                REQ_TP_LEAVE, req.getId(), "N");

        AprvLine myLine = lines.stream()
                .filter(l -> "APP".equals(l.getAprvTpCd()))
                .filter(l -> emp.getEmpNo().equals(l.getEmpId()))
                .filter(l -> "WAT".equals(l.getStsCd()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not an active approver for this request"));

        myLine.changeStatus("REJ");
        req.reject("REJ", rejectReason);

        return toItem(req, 0, emp.getEmpNo());
    }

    // ── Calculate ───────────────────────────────────────────

    @Override
    public LeaveResponse.CalcResult calculateDays(AuthenticatedUsr usr, LeaveRequest.Calc request) {
        Emp emp = resolveEmp(usr);
        Co co = coRepository.findById(usr.coId())
                .orElseThrow(() -> notFound("Company not found"));

        LocalDate strDt = LocalDate.parse(request.startDate());
        LocalDate endDt = LocalDate.parse(request.endDate());
        BigDecimal useDays = calcBusinessDays(usr, emp, co, request.leaveUnit(), strDt, endDt);

        var balance = calcBalance(usr.tenantCd(), co.getCoCd(), emp.getEmpNo());
        boolean isDeducted = "DD".equals(request.deductionType())
                || "Deducted Leave".equals(request.deductionType());
        double afterDays = isDeducted
                ? Math.max(balance.availableDays() - useDays.doubleValue(), 0)
                : balance.availableDays();

        List<LeaveReq> overlaps = leaveReqRepository
                .findOverlapping(usr.tenantCd(), co.getCoCd(), emp.getEmpNo(), strDt, endDt, "N")
                .stream()
                .filter(r -> !"CAN".equals(r.getStsCd()) && !"REJ".equals(r.getStsCd()))
                .filter(r -> request.leaveId() == null || !String.valueOf(r.getId()).equals(request.leaveId()))
                .toList();

        List<LeaveResponse.Dup> duplicates = overlaps.stream()
                .map(r -> new LeaveResponse.Dup(
                        r.getLeaveTpCd(), String.valueOf(r.getId()),
                        r.getStrDt().toString(), r.getEndDt().toString()))
                .toList();

        String resultCd = duplicates.isEmpty() ? "OK" : "DUP";
        String resultMsg = duplicates.isEmpty() ? "" : "Overlapping leave requests found";

        return new LeaveResponse.CalcResult(
                resultCd, resultMsg,
                balance.previousYearDays(), balance.currentYearDays(),
                useDays.doubleValue(), afterDays, duplicates
        );
    }

    // ── Validation ──────────────────────────────────────────

    private void validateLeaveRequest(
            String tenantCd, String coCd, String empNo,
            LeaveRequest request, LocalDate strDt, LocalDate endDt,
            BigDecimal useDays, Long excludeReqId
    ) {
        // 1. 시작일 <= 종료일
        if (endDt.isBefore(strDt)) {
            throw badRequest("End date must be on or after start date");
        }

        // 2. 중복 기간 검사 (날짜 겹침: existing.strDt <= newEnd AND existing.endDt >= newStr)
        List<LeaveReq> overlaps = leaveReqRepository
                .findOverlapping(tenantCd, coCd, empNo, strDt, endDt, "N")
                .stream()
                .filter(r -> !"CAN".equals(r.getStsCd()) && !"REJ".equals(r.getStsCd()))
                .filter(r -> excludeReqId == null || !excludeReqId.equals(r.getId()))
                .toList();
        if (!overlaps.isEmpty()) {
            throw badRequest("Overlapping leave request exists for the selected dates");
        }

        // 3. 잔여일수 초과 체크 (차감 유형일 때만)
        // update는 DRF→REQ만 허용, DRF 일수는 calcBalance에서 이미 제외되므로 복원 불필요
        boolean isDeducted = "DD".equals(request.deductionType())
                || "Deducted Leave".equals(request.deductionType());
        if (isDeducted && useDays.signum() > 0) {
            var balance = calcBalance(tenantCd, coCd, empNo);
            if (useDays.doubleValue() > balance.availableDays()) {
                throw badRequest("Insufficient leave balance. Available: " + balance.availableDays() + " days, Requested: " + useDays.doubleValue() + " days");
            }
        }

        // 4. Special Leave / 비차감 유형 사유 필수
        boolean isSpecialLeave = "SP".equals(request.leaveType())
                || "Special Leave".equals(request.leaveType());
        boolean isNonDeducted = "ND".equals(request.deductionType())
                || "Non-deducted Leave".equals(request.deductionType());
        if ((isSpecialLeave || isNonDeducted)
                && (request.reason() == null || request.reason().isBlank())) {
            throw badRequest("Reason is required for Special Leave or Non-deducted Leave");
        }

        // 5. 결재선 검증 (최대 3단, 중복 결재자 불가)
        if (request.approvalSteps() != null && !request.approvalSteps().isEmpty()) {
            if (request.approvalSteps().size() > 3) {
                throw badRequest("Maximum 3 approval steps allowed");
            }
            Set<String> allApprovers = new HashSet<>();
            for (LeaveRequest.AprvStep step : request.approvalSteps()) {
                if (step.usrIds() == null || step.usrIds().isEmpty()) continue;
                for (String uid : step.usrIds()) {
                    if (!allApprovers.add(uid)) {
                        throw badRequest("Duplicate approver: " + uid);
                    }
                }
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────

    private Emp resolveEmp(AuthenticatedUsr usr) {
        return empRepository.findByUsrId(usr.usrId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No employee record linked to user"));
    }

    private LeaveResponse.Balance calcBalance(String tenantCd, String coCd, String empNo) {
        Short currYr = (short) LocalDate.now().getYear();

        LeaveGrant grant = leaveGrantRepository
                .findOne(tenantCd, coCd, empNo, currYr, "N")
                .orElse(null);

        if (grant == null) {
            // 아직 부여 row 없는 직원 — 0으로 응답 (admin이 부여해야 함)
            return new LeaveResponse.Balance(0, 0, 0, 0);
        }

        // USED_DAYS 캐시 대신 실시간 합산 (DD 차감 + 활성 상태만)
        java.math.BigDecimal usedRuntime = leaveReqRepository
                .findByEmp(tenantCd, coCd, empNo, "N")
                .stream()
                .filter(r -> {
                    String s = r.getStsCd();
                    return !"CAN".equals(s) && !"REJ".equals(s) && !"DRF".equals(s);
                })
                .filter(r -> {
                    String d = r.getDeductTpCd();
                    return "DD".equals(d) || "Deducted Leave".equals(d);
                })
                .filter(r -> r.getStrDt() != null && r.getStrDt().getYear() == currYr.intValue())
                .map(LeaveReq::getUseDays)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal carryoverRemain = grant.getCarryoverRemain();   // 만료 차감 후 이월
        java.math.BigDecimal grantDays = grant.getGrantDays();

        // 이월 만료 체크: 정책의 만료일(MM/DD) 이후면 이월분 0 처리
        if (carryoverRemain.signum() > 0) {
            LeaveCoPolicy policy = leaveCoPolicyRepository
                    .findByTenantIdAndCoIdAndDelYn(tenantCd, coCd, "N")
                    .orElse(null);
            if (policy != null && policy.getCarryoverExpireMm() != null && policy.getCarryoverExpireDd() != null) {
                LocalDate expiryDate = LocalDate.of(currYr, policy.getCarryoverExpireMm(), policy.getCarryoverExpireDd());
                if (LocalDate.now().isAfter(expiryDate)) {
                    carryoverRemain = java.math.BigDecimal.ZERO;
                }
            }
        }

        java.math.BigDecimal totalAvail = carryoverRemain.add(grantDays).subtract(usedRuntime);
        if (totalAvail.signum() < 0) totalAvail = java.math.BigDecimal.ZERO;

        return new LeaveResponse.Balance(
                totalAvail.doubleValue(),   // availableDays  = 잔여(이월 + 부여 - 사용)
                totalAvail.doubleValue(),   // afterRequestDays = 동일 (실제 차감은 /calculate에서)
                carryoverRemain.doubleValue(), // previousYearDays = 이월분 (화면 괄호)
                grantDays.doubleValue()        // currentYearDays = 당해 부여
        );
    }

    private List<String> loadCdList(String grpCd) {
        CdGroup grp = cdGroupRepository.findByGrpCdIgnoreCase(grpCd).orElse(null);
        if (grp == null) return List.of();
        return commonCdRepository.findByGrp(grp.getId())
                .stream()
                .filter(cd -> "Y".equals(cd.getUseYn()))
                .map(CommonCd::getCd)
                .toList();
    }

    private List<LeaveResponse.Org> buildOrgTree(AuthenticatedUsr usr) {
        return deptRepository.findByCo(
                        usr.tenantId(), usr.coId())
                .stream()
                .map(d -> new LeaveResponse.Org(String.valueOf(d.getId()), d.getDeptNm(), null))
                .toList();
    }

    private List<LeaveResponse.Part> buildEmpList(AuthenticatedUsr usr) {
        List<Emp> emps = empRepository.findByCo(
                usr.tenantId(), usr.coId());

        Map<Long, String> deptMap = deptRepository
                .findByCo(usr.tenantId(), usr.coId())
                .stream()
                .collect(Collectors.toMap(Dept::getId, Dept::getDeptNm, (a, b) -> a));

        return emps.stream()
                .map(e -> new LeaveResponse.Part(
                        e.getEmpNo(), e.getEmpNm(),
                        e.getDeptId() != null ? deptMap.getOrDefault(e.getDeptId(), "") : "",
                        e.getJobTtlCd() != null ? e.getJobTtlCd() : "",
                        e.getDeptId() != null ? String.valueOf(e.getDeptId()) : ""
                ))
                .toList();
    }

    /**
     * 영업일 계산: 주말 + 지역공휴일(TB_REGION_HOLIDAY) + 회사휴일(TB_CO_HOLIDAY) 제외.
     * Half Day (HA/HP) 는 시작일=종료일 1일짜리로 가정, 그 날이 영업일이면 0.5, 아니면 0.
     */
    private BigDecimal calcBusinessDays(
            AuthenticatedUsr usr, Emp emp, Co co,
            String leaveUnit, LocalDate strDt, LocalDate endDt
    ) {
        if (strDt == null || endDt == null || endDt.isBefore(strDt)) {
            return BigDecimal.ZERO;
        }

        Set<LocalDate> excludedDays = loadExcludedDays(usr, emp, co, strDt, endDt);

        if (!"FD".equals(leaveUnit)) {
            // Half Day: 그 날이 영업일이면 0.5, 아니면 0
            boolean isBusiness = !isWeekend(strDt) && !excludedDays.contains(strDt);
            return isBusiness ? new BigDecimal("0.5") : BigDecimal.ZERO;
        }

        int businessDays = 0;
        LocalDate cursor = strDt;
        while (!cursor.isAfter(endDt)) {
            if (!isWeekend(cursor) && !excludedDays.contains(cursor)) {
                businessDays++;
            }
            cursor = cursor.plusDays(1);
        }
        return BigDecimal.valueOf(businessDays);
    }

    private Set<LocalDate> loadExcludedDays(
            AuthenticatedUsr usr, Emp emp, Co co, LocalDate strDt, LocalDate endDt
    ) {
        Set<LocalDate> excluded = new HashSet<>();

        // 1. 지역 공휴일: 유저 부서 → REGION_CD (없으면 'ALL'만 적용)
        String regionCd = resolveRegionCd(emp);
        Set<String> regions = new HashSet<>(Arrays.asList("ALL", regionCd != null ? regionCd : "ALL"));

        List<RgnHol> rgnHols = rgnHolRepository
                .findByPeriod(strDt, endDt, regions);
        for (RgnHol h : rgnHols) {
            excluded.add(h.getHolidayDt());
        }

        // 2. 회사 자체 휴일: 기간이 겹치는 모든 휴일 → 일자 펼치기
        Short fromYr = (short) strDt.getYear();
        Short toYr = (short) endDt.getYear();
        for (short yr = fromYr; yr <= toYr; yr++) {
            List<CoHol> coHols = coHolRepository.findByPeriod(
                    usr.tenantId(), usr.coId(), yr, strDt, endDt);
            for (CoHol h : coHols) {
                LocalDate cursor = h.getStrDt().isBefore(strDt) ? strDt : h.getStrDt();
                LocalDate hEnd = h.getEndDt().isAfter(endDt) ? endDt : h.getEndDt();
                while (!cursor.isAfter(hEnd)) {
                    excluded.add(cursor);
                    cursor = cursor.plusDays(1);
                }
            }
        }

        return excluded;
    }

    private String resolveRegionCd(Emp emp) {
        if (emp.getDeptId() == null) return null;
        return deptRepository.findById(emp.getDeptId())
                .map(Dept::getRegionCd)
                .orElse(null);
    }

    private boolean isWeekend(LocalDate d) {
        DayOfWeek dow = d.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    private String mapNextStatus(String nextStatus) {
        return switch (nextStatus) {
            case "Draft" -> "DRF";
            case "Requested" -> "REQ";
            default -> "DRF";
        };
    }

    private void saveAprvLines(
            String tenantCd, String coCd, Long reqId,
            List<LeaveRequest.AprvStep> steps, List<String> ccIds, String stsCd
    ) {
        String lineStsCd = "REQ".equals(stsCd) ? "WAT" : "DRF";

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                LeaveRequest.AprvStep step = steps.get(i);
                boolean isFinal = (i == steps.size() - 1);
                for (String usrId : step.usrIds()) {
                    aprvLineRepository.save(AprvLine.create(
                            tenantCd, coCd, REQ_TP_LEAVE, reqId,
                            "APP", usrId, step.order(), lineStsCd, isFinal ? "Y" : "N"
                    ));
                }
            }
        }

        if (ccIds != null) {
            for (String ccId : ccIds) {
                aprvLineRepository.save(AprvLine.create(
                        tenantCd, coCd, REQ_TP_LEAVE, reqId,
                        "REF", ccId, 0, "RF", "N"
                ));
            }
        }
    }

    private LeaveResponse.Item toItem(LeaveReq req, int no, String currentEmpNo) {
        List<AprvLine> lines = aprvLineRepository.findByReq(
                REQ_TP_LEAVE, req.getId(), "N");

        Map<Integer, List<AprvLine>> stepMap = lines.stream()
                .filter(l -> "APP".equals(l.getAprvTpCd()))
                .collect(Collectors.groupingBy(AprvLine::getStepOrder));

        List<LeaveResponse.AprvStep> approvalSteps = stepMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new LeaveResponse.AprvStep(
                        "step-" + e.getKey(), e.getKey(),
                        e.getValue().stream().map(l -> buildPart(l.getEmpId())).toList()
                ))
                .toList();

        List<LeaveResponse.Part> ccs = lines.stream()
                .filter(l -> "REF".equals(l.getAprvTpCd()))
                .map(l -> buildPart(l.getEmpId()))
                .toList();

        String approverStatus = calcApproverStatus(lines);

        boolean isOwner = req.getEmpId().equals(currentEmpNo);
        Boolean canEdit = isOwner && "DRF".equals(req.getStsCd()) ? true : null;
        Boolean canCancel = isOwner && ("REQ".equals(req.getStsCd()) || "APR".equals(req.getStsCd())) ? true : null;

        // 삭제 조건: Draft / CAN / REJ 또는 (No Approver + 시작일 전)
        String sts = req.getStsCd();
        boolean hasApprover = lines.stream().anyMatch(l -> "APP".equals(l.getAprvTpCd()));
        boolean beforeStart = req.getStrDt() != null && LocalDate.now().isBefore(req.getStrDt());
        boolean canDeleteFlag = isOwner && (
                "DRF".equals(sts) || "CAN".equals(sts) || "REJ".equals(sts)
                || (!hasApprover && beforeStart)
        );
        Boolean canDelete = canDeleteFlag ? true : null;

        boolean isApprover = lines.stream()
                .anyMatch(l -> "APP".equals(l.getAprvTpCd()) && l.getEmpId().equals(currentEmpNo));
        Boolean canApprove = isApprover && "REQ".equals(req.getStsCd()) ? true : null;
        Boolean canReject = canApprove;

        String myRoleCd = null;
        if (isOwner) myRoleCd = "OWNER";
        else if (isApprover) myRoleCd = "APPROVER";
        else if (lines.stream().anyMatch(l -> "REF".equals(l.getAprvTpCd()) && l.getEmpId().equals(currentEmpNo)))
            myRoleCd = "CC";

        return LeaveResponse.Item.from(
                req, no, approverStatus, approvalSteps, ccs,
                canEdit, canCancel, canDelete, canApprove, canReject, myRoleCd
        );
    }

    private LeaveResponse.Part buildPart(String empNo) {
        return empRepository.findAll().stream()
                .filter(e -> empNo.equals(e.getEmpNo()))
                .findFirst()
                .map(e -> {
                    String deptNm = e.getDeptId() != null
                            ? deptRepository.findById(e.getDeptId()).map(Dept::getDeptNm).orElse("")
                            : "";
                    return new LeaveResponse.Part(
                            e.getEmpNo(), e.getEmpNm(), deptNm,
                            e.getJobTtlCd() != null ? e.getJobTtlCd() : "",
                            e.getDeptId() != null ? String.valueOf(e.getDeptId()) : ""
                    );
                })
                .orElse(new LeaveResponse.Part(empNo, empNo, "", "", ""));
    }

    private String calcApproverStatus(List<AprvLine> lines) {
        List<AprvLine> approvers = lines.stream()
                .filter(l -> "APP".equals(l.getAprvTpCd())).toList();

        if (approvers.isEmpty()) return "No Approver";
        if (approvers.stream().anyMatch(l -> "REJ".equals(l.getStsCd()))) return "Rejected";

        long approved = approvers.stream().filter(l -> "APR".equals(l.getStsCd())).count();
        if (approved > 0) return "Approved by " + approved;
        return "Waiting";
    }

    private static ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}

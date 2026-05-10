package com.sinwoo.business.leave.service;

import com.sinwoo.business.leave.dto.*;
import com.sinwoo.business.leave.repository.LeaveReqRepository;
import com.sinwoo.business.leave.repository.LeaveReqRepository.*;
import com.sinwoo.business.leave.support.LeaveBizConst;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.common.security.AuthenticatedUsr;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveReqServiceImpl implements LeaveReqService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final LocalTime FULL_DAY_START = LocalTime.of(8, 0);
    private static final LocalTime FULL_DAY_END = LocalTime.of(17, 0);
    private static final LocalTime HALF_AM_END = LocalTime.of(12, 0);
    private static final LocalTime HALF_PM_START = LocalTime.of(13, 0);

    private static final List<String> LEAVE_TYPE_OPTS = List.of(
            "Annual Leave",
            "Sick Leave",
            "Marriage Leave",
            "Bereavement Leave",
            "Unpaid Leave",
            "Special Leave"
    );
    private static final List<String> DEDUCTION_TYPE_OPTS = List.of("Deducted Leave", "Non-deducted Leave");
    private static final List<String> LEAVE_UNIT_OPTS = List.of("Full Day", "Half Day AM", "Half Day PM");
    private static final List<String> STATUS_OPTS = List.of(
            "All",
            "Draft",
            "Requested",
            "Approved",
            "Rejected",
            "Cancelled"
    );

    private final LeaveReqRepository leaveReqRepository;

    @Override
    public LeaveCtxResponse getContext(AuthenticatedUsr authenticatedUsr) {
        EmpCtx emp = resolveEmp(authenticatedUsr);
        List<EmpCtx> emps = leaveReqRepository.findEmps(emp.tenantId(), emp.coId());
        List<DeptRow> depts = leaveReqRepository.findDepts(emp.tenantId(), emp.coId());

        return new LeaveCtxResponse(
                new LeaveApplResponse(
                        emp.empNm(),
                        textOrDefault(emp.deptNm(), "-"),
                        textOrDefault(emp.jobTitleNm(), "-")
                ),
                toBalance(emp, emp.currVacCnt().add(emp.preVacCnt())),
                LEAVE_TYPE_OPTS,
                DEDUCTION_TYPE_OPTS,
                LEAVE_UNIT_OPTS,
                STATUS_OPTS,
                toOrgs(depts, emps),
                emps.stream().map(this::toPart).toList()
        );
    }

    @Override
    public LeaveListResponse getLeaves(
            AuthenticatedUsr authenticatedUsr,
            String startDateFrom,
            String startDateTo,
            String status
    ) {
        EmpCtx emp = resolveEmp(authenticatedUsr);
        LocalDate from = parseOptionalDate(startDateFrom, "LEAVE_START_FROM_INVALID");
        LocalDate to = parseOptionalDate(startDateTo, "LEAVE_START_TO_INVALID");
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_RANGE_INVALID", "Start date from must be before start date to");
        }

        List<LeaveRow> rows = leaveReqRepository.findLeaveRows(
                emp.tenantId(),
                emp.coId(),
                emp.empId(),
                from,
                to,
                LeaveBizConst.statusCdFromLabel(status)
        );
        Map<Long, List<ApprovalRow>> approvalsByLeaveId = approvalsByLeaveId(rows.stream().map(LeaveRow::id).toList());
        List<LeaveReqResponse> responses = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            responses.add(toResponse(rows.get(i), i + 1, emp, approvalsByLeaveId.getOrDefault(rows.get(i).id(), List.of())));
        }

        return new LeaveListResponse(
                toBalance(emp, emp.currVacCnt().add(emp.preVacCnt())),
                responses
        );
    }

    @Override
    public LeaveCalcResponse calculate(AuthenticatedUsr authenticatedUsr, LeaveCalcRequest request) {
        EmpCtx emp = resolveEmp(authenticatedUsr);
        LeaveInput input = normalizeInput(request);
        List<DuplicateRow> duplicates = leaveReqRepository.findDuplicates(
                emp.tenantId(),
                emp.coId(),
                emp.empId(),
                parseOptionalLong(request == null ? null : request.leaveId()),
                input.startDate(),
                input.endDate()
        );
        BigDecimal days = calculateLeaveDays(emp, input.dayTpCd(), input.startDate(), input.endDate());

        if (!duplicates.isEmpty()) {
            return new LeaveCalcResponse(
                    "D",
                    "There are duplicate leave or business trip dates.",
                    emp.preVacCnt(),
                    emp.currVacCnt(),
                    days,
                    emp.preVacCnt().add(emp.currVacCnt()),
                    toDuplicateResponses(duplicates)
            );
        }

        BalAlloc balAlloc = allocateBalance(
                emp,
                new BalanceState(emp.preVacCnt(), emp.currVacCnt()),
                input.vacTpCd(),
                input.deductYn(),
                input.startDate(),
                days,
                true
        );
        return new LeaveCalcResponse(
                balAlloc.valid() ? "S" : "E",
                balAlloc.message(),
                balAlloc.nextPreVacCnt(),
                balAlloc.nextCurrVacCnt(),
                days,
                balAlloc.nextPreVacCnt().add(balAlloc.nextCurrVacCnt()),
                List.of()
        );
    }

    @Override
    @Transactional
    public LeaveReqResponse createLeave(AuthenticatedUsr authenticatedUsr, LeaveSaveRequest request) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        EmpCtx emp = resolveEmpForUpdate(user);
        String actor = actor(user);
        PreparedLeave preparedLeave = prepareLeave(emp, null, null, request, actor);

        Long leaveId = leaveReqRepository.insertLeave(preparedLeave.values());
        insertApprovals(leaveId, emp, request, preparedLeave.status(), actor);
        if (preparedLeave.updateBalanceYn()) {
            leaveReqRepository.updateHrProfileBalance(
                    emp.profileId(),
                    preparedLeave.nextBalance().preVacCnt(),
                    preparedLeave.nextBalance().currVacCnt(),
                    actor
            );
        }
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveReqResponse updateLeave(AuthenticatedUsr authenticatedUsr, Long leaveId, LeaveSaveRequest request) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        LeaveRow current = lockLeave(user, leaveId);
        ensureRequester(current, resolveEmp(user));
        if (!canEdit(current)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_UPDATE_NOT_ALLOWED", "Only draft or rejected leave requests can be updated");
        }

        EmpCtx emp = leaveReqRepository.lockEmpProfile(current.tenantId(), current.coId(), current.empId());
        String actor = actor(user);
        BalanceState restoredBalance = restoreBalance(emp, current);
        PreparedLeave preparedLeave = prepareLeave(emp, restoredBalance, leaveId, request, actor);

        leaveReqRepository.updateLeave(leaveId, preparedLeave.values());
        leaveReqRepository.deleteApprovals(leaveId);
        insertApprovals(leaveId, emp, request, preparedLeave.status(), actor);
        leaveReqRepository.updateHrProfileBalance(
                emp.profileId(),
                preparedLeave.nextBalance().preVacCnt(),
                preparedLeave.nextBalance().currVacCnt(),
                actor
        );
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveReqResponse cancelLeave(AuthenticatedUsr authenticatedUsr, Long leaveId) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        LeaveRow current = lockLeave(user, leaveId);
        EmpCtx requester = resolveEmp(user);
        ensureRequester(current, requester);
        if (!canCancel(current)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_CANCEL_NOT_ALLOWED", "Only draft or requested leave requests can be cancelled");
        }

        String actor = actor(user);
        if (LeaveBizConst.isReservedStatus(current.stsCd(), current.aprvStsCd())) {
            EmpCtx emp = leaveReqRepository.lockEmpProfile(current.tenantId(), current.coId(), current.empId());
            BalanceState restoredBalance = restoreBalance(emp, current);
            leaveReqRepository.updateHrProfileBalance(emp.profileId(), restoredBalance.preVacCnt(), restoredBalance.currVacCnt(), actor);
        }
        leaveReqRepository.cancelLeave(leaveId, actor);
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveReqResponse confirmLeave(AuthenticatedUsr authenticatedUsr, Long leaveId) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        EmpCtx approver = resolveEmp(user);
        LeaveRow current = lockLeave(user, leaveId);
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(current.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(current.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVAL_NOT_REQUESTED", "Only requested leave can be approved");
        }
        if (Objects.equals(current.empId(), approver.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_SELF_APPROVAL_NOT_ALLOWED", "Requester cannot approve own leave");
        }
        ApprovalRow currentApproval = leaveReqRepository
                .findCurrentApproval(current.id(), approver.empId(), current.aprvCurrLvlNo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "LEAVE_APPROVAL_FORBIDDEN", "Current user is not the current approver"));
        if (!LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(currentApproval.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVAL_STEP_INVALID", "Current approval step is not waiting for approval");
        }

        String actor = actor(user);
        leaveReqRepository.updateApprovalStatus(current.id(), current.aprvCurrLvlNo(), LeaveBizConst.APRV_APPROVED, "Y", actor);
        if (current.aprvCurrLvlNo() >= current.aprvTotLvlNo()) {
            leaveReqRepository.updateLeaveApprovalStatus(current.id(), current.aprvTotLvlNo(), LeaveBizConst.APRV_APPROVED, null, actor);
        } else {
            int nextLevel = current.aprvCurrLvlNo() + 1;
            leaveReqRepository.updateLeaveApprovalStatus(current.id(), nextLevel, LeaveBizConst.APRV_REQUESTED, null, actor);
            leaveReqRepository.updateApprovalStatus(current.id(), nextLevel, LeaveBizConst.APRV_REQUESTED, "N", actor);
        }

        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveReqResponse rejectLeave(AuthenticatedUsr authenticatedUsr, Long leaveId, LeaveActRequest request) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        EmpCtx approver = resolveEmp(user);
        LeaveRow current = lockLeave(user, leaveId);
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(current.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(current.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_REJECT_NOT_REQUESTED", "Only requested leave can be rejected");
        }
        if (Objects.equals(current.empId(), approver.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_SELF_REJECT_NOT_ALLOWED", "Requester cannot reject own leave");
        }
        leaveReqRepository
                .findCurrentApproval(current.id(), approver.empId(), current.aprvCurrLvlNo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "LEAVE_REJECT_FORBIDDEN", "Current user is not the current approver"));

        String actor = actor(user);
        if (LeaveBizConst.isReservedStatus(current.stsCd(), current.aprvStsCd())) {
            EmpCtx emp = leaveReqRepository.lockEmpProfile(current.tenantId(), current.coId(), current.empId());
            BalanceState restoredBalance = restoreBalance(emp, current);
            leaveReqRepository.updateHrProfileBalance(emp.profileId(), restoredBalance.preVacCnt(), restoredBalance.currVacCnt(), actor);
        }
        leaveReqRepository.updateLeaveApprovalStatus(
                current.id(),
                0,
                LeaveBizConst.APRV_REJECTED,
                request == null ? "" : textOrDefault(request.rejectReason(), ""),
                actor
        );
        leaveReqRepository.rejectApprovals(current.id(), actor);
        return findResponse(user, leaveId);
    }

    private PreparedLeave prepareLeave(
            EmpCtx emp,
            BalanceState baseBalance,
            Long excludeLeaveId,
            LeaveSaveRequest request,
            String actor
    ) {
        LeaveInput input = normalizeInput(request);
        List<DuplicateRow> duplicates = leaveReqRepository.findDuplicates(
                emp.tenantId(),
                emp.coId(),
                emp.empId(),
                excludeLeaveId,
                input.startDate(),
                input.endDate()
        );
        if (!duplicates.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_DUPLICATED", "Leave dates overlap with another leave or business trip");
        }

        BigDecimal days = calculateLeaveDays(emp, input.dayTpCd(), input.startDate(), input.endDate());
        String status = resolveSaveStatus(request, normalizeApprovalSteps(request == null ? null : request.approvalSteps()).isEmpty());
        boolean reserveBalance = LeaveBizConst.APRV_REQUESTED.equals(status) || LeaveBizConst.APRV_APPROVED.equals(status);
        BalanceState startBalance = baseBalance == null
                ? new BalanceState(emp.preVacCnt(), emp.currVacCnt())
                : baseBalance;
        BalAlloc balAlloc = allocateBalance(
                emp,
                startBalance,
                input.vacTpCd(),
                input.deductYn(),
                input.startDate(),
                days,
                reserveBalance
        );
        if (!balAlloc.valid()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_BALANCE_INVALID", balAlloc.message());
        }

        List<NormalizedApprovalStep> approvalSteps = normalizeApprovalSteps(request == null ? null : request.approvalSteps());
        int totalLevel = LeaveBizConst.APRV_APPROVED.equals(status) ? 0 : approvalSteps.size();
        int currentLevel = LeaveBizConst.APRV_REQUESTED.equals(status) && totalLevel > 0 ? 1 : 0;
        TimeRange timeRange = resolveTimeRange(input.dayTpCd());

        return new PreparedLeave(
                new LeaveWriteValues(
                        emp.tenantId(),
                        emp.coId(),
                        emp.deptId(),
                        emp.empId(),
                        emp.deptId() == null ? null : String.valueOf(emp.deptId()),
                        emp.empNo(),
                        input.vacTpCd(),
                        input.dayTpCd(),
                        input.deductYn(),
                        balAlloc.currUseVacCnt(),
                        balAlloc.preUseVacCnt(),
                        days,
                        input.startDate(),
                        timeRange.startTime(),
                        LeaveBizConst.isHalfDay(input.dayTpCd()) ? input.startDate() : input.endDate(),
                        timeRange.endTime(),
                        totalLevel,
                        currentLevel,
                        status,
                        request == null ? "" : textOrDefault(request.reason(), ""),
                        request == null ? null : blankToNull(request.attachmentName()),
                        actor
                ),
                balAlloc.nextBalance(),
                !startBalance.equals(balAlloc.nextBalance()),
                status
        );
    }

    private String resolveSaveStatus(LeaveSaveRequest request, boolean noApprovers) {
        String requestedStatus = LeaveBizConst.normalizeSaveStatus(request == null ? null : request.nextStatus());
        if (LeaveBizConst.APRV_REQUESTED.equals(requestedStatus) && noApprovers) {
            return LeaveBizConst.APRV_APPROVED;
        }
        return requestedStatus;
    }

    private void insertApprovals(
            Long leaveId,
            EmpCtx emp,
            LeaveSaveRequest request,
            String leaveStatus,
            String actor
    ) {
        Map<Long, EmpCtx> empById = leaveReqRepository.findEmps(emp.tenantId(), emp.coId())
                .stream()
                .collect(Collectors.toMap(EmpCtx::empId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<NormalizedApprovalStep> approvalSteps = normalizeApprovalSteps(request == null ? null : request.approvalSteps());
        for (NormalizedApprovalStep step : approvalSteps) {
            for (Long approverId : step.approverIds()) {
                EmpCtx approver = requireApprovalEmp(empById, approverId);
                String approvalStatus = resolveApprovalStepStatus(leaveStatus, step.order());
                leaveReqRepository.insertApproval(new ApprovalWriteValues(
                        leaveId,
                        emp.tenantId(),
                        emp.coId(),
                        emp.deptId(),
                        emp.empId(),
                        approver.empId(),
                        emp.empNo(),
                        approver.empNo(),
                        LeaveBizConst.APRV_TYPE_APPROVER,
                        step.order(),
                        approvalStatus,
                        "N",
                        step.order() == approvalSteps.size() ? "Y" : "N",
                        actor
                ));
            }
        }

        for (Long referrerId : normalizeCcIds(request == null ? null : request.ccIds())) {
            EmpCtx referrer = requireApprovalEmp(empById, referrerId);
            leaveReqRepository.insertApproval(new ApprovalWriteValues(
                    leaveId,
                    emp.tenantId(),
                    emp.coId(),
                    emp.deptId(),
                    emp.empId(),
                    referrer.empId(),
                    emp.empNo(),
                    referrer.empNo(),
                    LeaveBizConst.APRV_TYPE_REFERENCE,
                    0,
                    LeaveBizConst.APRV_REFERENCE,
                    "N",
                    "N",
                    actor
            ));
        }
    }

    private String resolveApprovalStepStatus(String leaveStatus, Integer order) {
        if (LeaveBizConst.APRV_APPROVED.equals(leaveStatus)) {
            return LeaveBizConst.APRV_APPROVED;
        }
        if (LeaveBizConst.APRV_REQUESTED.equals(leaveStatus) && Integer.valueOf(1).equals(order)) {
            return LeaveBizConst.APRV_REQUESTED;
        }
        return LeaveBizConst.APRV_WAITING;
    }

    private EmpCtx requireApprovalEmp(Map<Long, EmpCtx> empById, Long empId) {
        EmpCtx emp = empById.get(empId);
        if (emp == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVER_INVALID", "Selected approver is not an active emp in this company");
        }
        return emp;
    }

    private LeaveReqResponse findResponse(AuthenticatedUsr user, Long leaveId) {
        EmpCtx emp = resolveEmp(user);
        LeaveRow row = leaveReqRepository.findLeaveRow(emp.tenantId(), emp.coId(), leaveId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LEAVE_NOT_FOUND", "Leave request not found"));
        List<ApprovalRow> approvals = leaveReqRepository.findApprovals(List.of(row.id()));
        return toResponse(row, 1, emp, approvals);
    }

    private LeaveRow lockLeave(AuthenticatedUsr user, Long leaveId) {
        if (leaveId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_ID_REQUIRED", "Leave id is required");
        }
        AuthenticatedUsr authenticatedUsr = requireUsr(user);
        EmpCtx emp = resolveEmp(authenticatedUsr);
        try {
            return leaveReqRepository.lockLeaveRow(emp.tenantId(), emp.coId(), leaveId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "LEAVE_NOT_FOUND", "Leave request not found");
        }
    }

    private LeaveReqResponse toResponse(
            LeaveRow row,
            int no,
            EmpCtx actorEmp,
            List<ApprovalRow> approvals
    ) {
        String status = LeaveBizConst.statusLabel(row.stsCd(), row.aprvStsCd());
        List<ApprovalRow> approvers = approvals.stream()
                .filter(approval -> LeaveBizConst.APRV_TYPE_APPROVER.equalsIgnoreCase(approval.aprvTpCd()))
                .toList();
        List<ApprovalRow> referrers = approvals.stream()
                .filter(approval -> LeaveBizConst.APRV_TYPE_REFERENCE.equalsIgnoreCase(approval.aprvTpCd()))
                .toList();

        return new LeaveReqResponse(
                String.valueOf(row.id()),
                no,
                LeaveBizConst.leaveTypeLabel(row.vacTpCd()),
                LeaveBizConst.deductionTypeLabel(row.deductYn()),
                LeaveBizConst.dayTypeLabel(row.dayTpCd()),
                formatDate(row.startDate()),
                formatDate(row.endDate()),
                scale(row.useVacCnt()),
                approverStatus(row, approvers),
                status,
                formatCreatedAt(row.createdAt()),
                row.attachmentName(),
                row.requestReason(),
                toApprovalSteps(row.id(), approvers),
                referrers.stream().map(this::toPart).toList(),
                canEdit(row) && Objects.equals(row.empId(), actorEmp.empId()),
                canCancel(row) && Objects.equals(row.empId(), actorEmp.empId()),
                canApprove(row, actorEmp, approvers),
                canApprove(row, actorEmp, approvers),
                resolveMyRole(row, actorEmp, approvals)
        );
    }

    private String approverStatus(LeaveRow row, List<ApprovalRow> approvers) {
        if (LeaveBizConst.STS_CANCELLED.equalsIgnoreCase(row.stsCd()) || approvers.isEmpty()) {
            return "No Approver";
        }
        if (LeaveBizConst.APRV_REJECTED.equalsIgnoreCase(row.aprvStsCd())) {
            return "Rejected";
        }
        if (LeaveBizConst.APRV_APPROVED.equalsIgnoreCase(row.aprvStsCd())) {
            return "Approved by " + row.aprvTotLvlNo();
        }
        if (LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(row.aprvStsCd())) {
            return "Waiting";
        }
        return "No Approver";
    }

    private List<LeaveAprvStepResponse> toApprovalSteps(Long leaveId, List<ApprovalRow> approvers) {
        return approvers.stream()
                .collect(Collectors.groupingBy(ApprovalRow::aprvLvlNo, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new ApprovalStepResponseAdapter(
                        "approval-step-" + leaveId + "-" + entry.getKey(),
                        entry.getKey(),
                        entry.getValue().stream().map(this::toPart).toList()
                ))
                .sorted(Comparator.comparing(ApprovalStepResponseAdapter::order))
                .map(adapter -> new LeaveAprvStepResponse(adapter.id(), adapter.order(), adapter.usrs()))
                .toList();
    }

    private LeavePartResponse toPart(ApprovalRow approval) {
        return new LeavePartResponse(
                approval.aprvEmpId() == null ? "" : String.valueOf(approval.aprvEmpId()),
                textOrDefault(approval.approverName(), "-"),
                textOrDefault(approval.approverDeptName(), "-"),
                textOrDefault(approval.approverJobTitle(), "-"),
                orgId(approval.deptId())
        );
    }

    private LeavePartResponse toPart(EmpCtx emp) {
        return new LeavePartResponse(
                String.valueOf(emp.empId()),
                textOrDefault(emp.empNm(), "-"),
                textOrDefault(emp.deptNm(), "-"),
                textOrDefault(emp.jobTitleNm(), "-"),
                orgId(emp.deptId())
        );
    }

    private List<LeaveOrgResponse> toOrgs(List<DeptRow> depts, List<EmpCtx> emps) {
        Map<Long, List<DeptRow>> childrenByParent = new LinkedHashMap<>();
        for (DeptRow dept : depts) {
            childrenByParent.computeIfAbsent(dept.parentDeptId(), key -> new ArrayList<>()).add(dept);
        }
        List<LeaveOrgResponse> roots = childrenByParent.getOrDefault(null, List.of())
                .stream()
                .map(dept -> toOrg(dept, childrenByParent))
                .collect(Collectors.toCollection(ArrayList::new));
        if (roots.isEmpty()) {
            roots.addAll(depts.stream()
                    .map(dept -> new LeaveOrgResponse(orgId(dept.deptId()), dept.deptNm(), List.of()))
                    .toList());
        }
        boolean hasUnassigned = emps.stream().anyMatch(emp -> emp.deptId() == null);
        if (hasUnassigned) {
            roots.add(new LeaveOrgResponse(orgId(null), "Unassigned", List.of()));
        }
        return roots;
    }

    private LeaveOrgResponse toOrg(
            DeptRow dept,
            Map<Long, List<DeptRow>> childrenByParent
    ) {
        List<LeaveOrgResponse> children = childrenByParent.getOrDefault(dept.deptId(), List.of())
                .stream()
                .map(child -> toOrg(child, childrenByParent))
                .toList();
        return new LeaveOrgResponse(orgId(dept.deptId()), textOrDefault(dept.deptNm(), "-"), children);
    }

    private boolean canEdit(LeaveRow row) {
        return LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(row.stsCd())
                && (LeaveBizConst.APRV_SAVED.equalsIgnoreCase(row.aprvStsCd())
                || LeaveBizConst.APRV_REJECTED.equalsIgnoreCase(row.aprvStsCd()));
    }

    private boolean canCancel(LeaveRow row) {
        return LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(row.stsCd())
                && (LeaveBizConst.APRV_SAVED.equalsIgnoreCase(row.aprvStsCd())
                || LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(row.aprvStsCd()));
    }

    private boolean canApprove(LeaveRow row, EmpCtx emp, List<ApprovalRow> approvers) {
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(row.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(row.aprvStsCd())) {
            return false;
        }
        if (Objects.equals(row.empId(), emp.empId())) {
            return false;
        }
        return approvers.stream()
                .anyMatch(approval -> Objects.equals(approval.aprvEmpId(), emp.empId())
                        && Objects.equals(approval.aprvLvlNo(), row.aprvCurrLvlNo())
                        && LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(approval.aprvStsCd()));
    }

    private String resolveMyRole(LeaveRow row, EmpCtx emp, List<ApprovalRow> approvals) {
        if (Objects.equals(row.empId(), emp.empId())) {
            return LeaveBizConst.ROLE_REQUESTER;
        }
        return approvals.stream()
                .filter(approval -> Objects.equals(approval.aprvEmpId(), emp.empId()))
                .findFirst()
                .map(approval -> LeaveBizConst.APRV_TYPE_APPROVER.equalsIgnoreCase(approval.aprvTpCd())
                        ? LeaveBizConst.ROLE_APPROVER
                        : LeaveBizConst.ROLE_REFERRER)
                .orElse(LeaveBizConst.ROLE_REQUESTER);
    }

    private Map<Long, List<ApprovalRow>> approvalsByLeaveId(Collection<Long> leaveIds) {
        return leaveReqRepository.findApprovals(leaveIds)
                .stream()
                .collect(Collectors.groupingBy(ApprovalRow::leaveReqId, LinkedHashMap::new, Collectors.toList()));
    }

    private BalAlloc allocateBalance(
            EmpCtx emp,
            BalanceState balance,
            String vacTpCd,
            String deductYn,
            LocalDate startDate,
            BigDecimal days,
            boolean reserveBalance
    ) {
        if (!reserveBalance || !LeaveBizConst.DEDUCT_Y.equalsIgnoreCase(deductYn)) {
            return BalAlloc.valid(ZERO, ZERO, days, balance, "Calculated successfully.");
        }
        if (LeaveBizConst.VAC_ANNUAL.equals(vacTpCd) && balance.preVacCnt().compareTo(ZERO) > 0 && startDate.getMonthValue() >= 4) {
            return BalAlloc.invalid(balance, "Previous year's vacation must be used before April.");
        }

        BigDecimal preUse = min(balance.preVacCnt(), days);
        BigDecimal remaining = days.subtract(preUse);
        BigDecimal currUse = remaining.max(BigDecimal.ZERO);
        BigDecimal nextPre = balance.preVacCnt().subtract(preUse);
        BigDecimal nextCurr = balance.currVacCnt().subtract(currUse);
        if (nextCurr.compareTo(ZERO) < 0) {
            return BalAlloc.invalid(balance, "The number of vacation days requested is greater than the number of vacation days remaining.");
        }
        return BalAlloc.valid(preUse, currUse, days, new BalanceState(scale(nextPre), scale(nextCurr)), "Calculated successfully.");
    }

    private BalanceState restoreBalance(EmpCtx emp, LeaveRow row) {
        if (!LeaveBizConst.isReservedStatus(row.stsCd(), row.aprvStsCd())) {
            return new BalanceState(emp.preVacCnt(), emp.currVacCnt());
        }
        BigDecimal maxCurr = emp.stdVacCnt().add(emp.incVacCnt());
        BigDecimal restoredCurr = emp.currVacCnt().add(row.currUseVacCnt());
        BigDecimal restoredPre = emp.preVacCnt().add(row.preUseVacCnt());
        if (restoredCurr.compareTo(maxCurr) > 0) {
            restoredPre = restoredPre.add(restoredCurr.subtract(maxCurr));
            restoredCurr = maxCurr;
        }
        return new BalanceState(scale(restoredPre), scale(restoredCurr));
    }

    private BigDecimal calculateLeaveDays(EmpCtx emp, String dayTpCd, LocalDate startDate, LocalDate endDate) {
        if (LeaveBizConst.isHalfDay(dayTpCd)) {
            return HALF_DAY;
        }
        Set<LocalDate> holidays = holidayDates(emp, startDate, endDate);
        BigDecimal days = ZERO;
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            if (!isWeekend(cursor) && !holidays.contains(cursor)) {
                days = days.add(BigDecimal.ONE);
            }
            cursor = cursor.plusDays(1);
        }
        if (days.compareTo(ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DAYS_EMPTY", "Selected date range has no working days");
        }
        return scale(days);
    }

    private Set<LocalDate> holidayDates(EmpCtx emp, LocalDate startDate, LocalDate endDate) {
        String countryCd = textOrDefault(emp.countryCd(), "DE");
        String regionCd = textOrDefault(emp.regionCd(), "ALL");
        Set<LocalDate> holidays = new LinkedHashSet<>(leaveReqRepository.findRegionHolidays(countryCd, regionCd, startDate, endDate));
        for (CoHoliRow holiday : leaveReqRepository.findCoHolidays(emp.tenantId(), emp.coId(), startDate, endDate)) {
            if ("Y".equalsIgnoreCase(holiday.annualYn())) {
                for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
                    try {
                        LocalDate annualHoliday = LocalDate.of(year, holiday.holiDt().getMonth(), holiday.holiDt().getDayOfMonth());
                        if (!annualHoliday.isBefore(startDate) && !annualHoliday.isAfter(endDate)) {
                            holidays.add(annualHoliday);
                        }
                    } catch (DateTimeException ignored) {
                        // Ignore annual leap-day holidays on non-leap years.
                    }
                }
            } else if (holiday.holiDt() != null) {
                holidays.add(holiday.holiDt());
            }
        }
        return holidays;
    }

    private LeaveInput normalizeInput(LeaveCalcRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_REQUEST_REQUIRED", "Leave request is required");
        }
        String dayTpCd = LeaveBizConst.normalizeDayType(request.leaveUnit());
        LocalDate startDate = parseDate(request.startDate(), "LEAVE_START_DATE_INVALID");
        LocalDate endDate = LeaveBizConst.isHalfDay(dayTpCd)
                ? startDate
                : parseDate(request.endDate(), "LEAVE_END_DATE_INVALID");
        if (startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_RANGE_INVALID", "Start date must be before end date");
        }
        return new LeaveInput(
                LeaveBizConst.normalizeLeaveType(request.leaveType()),
                dayTpCd,
                LeaveBizConst.normalizeDeductYn(request.deductionType()),
                startDate,
                endDate
        );
    }

    private LeaveInput normalizeInput(LeaveSaveRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_REQUEST_REQUIRED", "Leave request is required");
        }
        String dayTpCd = LeaveBizConst.normalizeDayType(request.leaveUnit());
        LocalDate startDate = parseDate(request.startDate(), "LEAVE_START_DATE_INVALID");
        LocalDate endDate = LeaveBizConst.isHalfDay(dayTpCd)
                ? startDate
                : parseDate(request.endDate(), "LEAVE_END_DATE_INVALID");
        if (startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_RANGE_INVALID", "Start date must be before end date");
        }
        return new LeaveInput(
                LeaveBizConst.normalizeLeaveType(request.leaveType()),
                dayTpCd,
                LeaveBizConst.normalizeDeductYn(request.deductionType()),
                startDate,
                endDate
        );
    }

    private List<NormalizedApprovalStep> normalizeApprovalSteps(List<LeaveAprvStepRequest> steps) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }
        List<NormalizedApprovalStep> normalized = new ArrayList<>();
        int order = 1;
        for (LeaveAprvStepRequest step : steps) {
            LinkedHashSet<Long> approverIds = new LinkedHashSet<>();
            if (step != null && step.usrIds() != null) {
                for (String usrId : step.usrIds()) {
                    Long parsed = parseOptionalLong(usrId);
                    if (parsed != null) {
                        approverIds.add(parsed);
                    }
                }
            }
            if (!approverIds.isEmpty()) {
                normalized.add(new NormalizedApprovalStep(order, List.copyOf(approverIds)));
                order++;
            }
        }
        return normalized;
    }

    private List<Long> normalizeCcIds(List<String> ccIds) {
        if (ccIds == null || ccIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String ccId : ccIds) {
            Long parsed = parseOptionalLong(ccId);
            if (parsed != null) {
                ids.add(parsed);
            }
        }
        return List.copyOf(ids);
    }

    private TimeRange resolveTimeRange(String dayTpCd) {
        String normalized = LeaveBizConst.normalizeDayType(dayTpCd);
        if (LeaveBizConst.DAY_HALF_AM.equals(normalized) || LeaveBizConst.DAY_HALF_LEGACY.equals(normalized)) {
            return new TimeRange(FULL_DAY_START, HALF_AM_END);
        }
        if (LeaveBizConst.DAY_HALF_PM.equals(normalized)) {
            return new TimeRange(HALF_PM_START, FULL_DAY_END);
        }
        return new TimeRange(FULL_DAY_START, FULL_DAY_END);
    }

    private void ensureRequester(LeaveRow row, EmpCtx emp) {
        if (!Objects.equals(row.empId(), emp.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_REQUESTER_FORBIDDEN", "Only the requester can change this leave request");
        }
    }

    private EmpCtx resolveEmp(AuthenticatedUsr authenticatedUsr) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        EmpCtx emp = leaveReqRepository.findEmpCtx(user.tenantId(), user.usrId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_EMPLOYEE_CONTEXT_NOT_FOUND", "Authenticated user is not connected to an active emp"));
        if (emp.profileId() == null) {
            leaveReqRepository.createDefaultHrProfile(emp);
            emp = leaveReqRepository.findEmpCtx(user.tenantId(), user.usrId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_EMPLOYEE_CONTEXT_NOT_FOUND", "Authenticated user is not connected to an active emp"));
        }
        return emp;
    }

    private EmpCtx resolveEmpForUpdate(AuthenticatedUsr authenticatedUsr) {
        EmpCtx emp = resolveEmp(authenticatedUsr);
        return leaveReqRepository.lockEmpProfile(emp.tenantId(), emp.coId(), emp.empId());
    }

    private AuthenticatedUsr requireUsr(AuthenticatedUsr authenticatedUsr) {
        if (authenticatedUsr == null || authenticatedUsr.tenantId() == null || authenticatedUsr.usrId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required");
        }
        return authenticatedUsr;
    }

    private LocalDate parseDate(String value, String code) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, code, "Date is required");
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, code, "Date must follow yyyy-MM-dd");
        }
    }

    private LocalDate parseOptionalDate(String value, String code) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseDate(value, code);
    }

    private Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private List<LeaveDupResponse> toDuplicateResponses(List<DuplicateRow> duplicates) {
        return duplicates.stream()
                .map(duplicate -> new LeaveDupResponse(
                        duplicate.type(),
                        String.valueOf(duplicate.id()),
                        formatDate(duplicate.startDate()),
                        formatDate(duplicate.endDate())
                ))
                .toList();
    }

    private LeaveBalResponse toBalance(EmpCtx emp, BigDecimal afterRequestDays) {
        return new LeaveBalResponse(
                scale(emp.currVacCnt().add(emp.preVacCnt())),
                scale(afterRequestDays),
                scale(emp.preVacCnt()),
                scale(emp.currVacCnt())
        );
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(1, RoundingMode.HALF_UP);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private static String formatCreatedAt(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(CREATED_AT_FORMATTER);
    }

    private static String orgId(Long deptId) {
        return deptId == null ? "org-unassigned" : "dept-" + deptId;
    }

    private static String actor(AuthenticatedUsr user) {
        if (user.eml() != null && !user.eml().isBlank()) {
            return user.eml();
        }
        if (user.lgnId() != null && !user.lgnId().isBlank()) {
            return user.lgnId();
        }
        return "SYSTEM";
    }

    private static String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record LeaveInput(
            String vacTpCd,
            String dayTpCd,
            String deductYn,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    private record TimeRange(
            LocalTime startTime,
            LocalTime endTime
    ) {
    }

    private record BalanceState(
            BigDecimal preVacCnt,
            BigDecimal currVacCnt
    ) {
        private BalanceState {
            preVacCnt = scale(preVacCnt);
            currVacCnt = scale(currVacCnt);
        }
    }

    private record BalAlloc(
            boolean valid,
            BigDecimal preUseVacCnt,
            BigDecimal currUseVacCnt,
            BigDecimal useVacCnt,
            BalanceState nextBalance,
            String message
    ) {
        private static BalAlloc valid(
                BigDecimal preUseVacCnt,
                BigDecimal currUseVacCnt,
                BigDecimal useVacCnt,
                BalanceState nextBalance,
                String message
        ) {
            return new BalAlloc(true, scale(preUseVacCnt), scale(currUseVacCnt), scale(useVacCnt), nextBalance, message);
        }

        private static BalAlloc invalid(BalanceState balance, String message) {
            return new BalAlloc(false, ZERO, ZERO, ZERO, balance, message);
        }

        private BigDecimal nextPreVacCnt() {
            return nextBalance.preVacCnt();
        }

        private BigDecimal nextCurrVacCnt() {
            return nextBalance.currVacCnt();
        }
    }

    private record PreparedLeave(
            LeaveWriteValues values,
            BalanceState nextBalance,
            boolean updateBalanceYn,
            String status
    ) {
    }

    private record NormalizedApprovalStep(
            Integer order,
            List<Long> approverIds
    ) {
    }

    private record ApprovalStepResponseAdapter(
            String id,
            Integer order,
            List<LeavePartResponse> usrs
    ) {
    }
}

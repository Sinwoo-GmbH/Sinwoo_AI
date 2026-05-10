package com.sinwoo.leave.service;

import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.leave.dto.LeaveActionRequest;
import com.sinwoo.leave.dto.LeaveApplicantResponse;
import com.sinwoo.leave.dto.LeaveApprovalStepRequest;
import com.sinwoo.leave.dto.LeaveApprovalStepResponse;
import com.sinwoo.leave.dto.LeaveBalanceResponse;
import com.sinwoo.leave.dto.LeaveCalculateRequest;
import com.sinwoo.leave.dto.LeaveCalculateResponse;
import com.sinwoo.leave.dto.LeaveContextResponse;
import com.sinwoo.leave.dto.LeaveDuplicateResponse;
import com.sinwoo.leave.dto.LeaveListResponse;
import com.sinwoo.leave.dto.LeaveOrganizationResponse;
import com.sinwoo.leave.dto.LeaveParticipantResponse;
import com.sinwoo.leave.dto.LeaveRequestResponse;
import com.sinwoo.leave.dto.LeaveSaveRequest;
import com.sinwoo.leave.repository.LeaveRequestRepository;
import com.sinwoo.leave.repository.LeaveRequestRepository.ApprovalRow;
import com.sinwoo.leave.repository.LeaveRequestRepository.ApprovalWriteValues;
import com.sinwoo.leave.repository.LeaveRequestRepository.CompanyHolidayRow;
import com.sinwoo.leave.repository.LeaveRequestRepository.DepartmentRow;
import com.sinwoo.leave.repository.LeaveRequestRepository.DuplicateRow;
import com.sinwoo.leave.repository.LeaveRequestRepository.EmployeeContext;
import com.sinwoo.leave.repository.LeaveRequestRepository.LeaveRow;
import com.sinwoo.leave.repository.LeaveRequestRepository.LeaveWriteValues;
import com.sinwoo.leave.support.LeaveBizConst;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final LocalTime FULL_DAY_START = LocalTime.of(8, 0);
    private static final LocalTime FULL_DAY_END = LocalTime.of(17, 0);
    private static final LocalTime HALF_AM_END = LocalTime.of(12, 0);
    private static final LocalTime HALF_PM_START = LocalTime.of(13, 0);

    private static final List<String> LEAVE_TYPE_OPTIONS = List.of(
            "Annual Leave",
            "Sick Leave",
            "Marriage Leave",
            "Bereavement Leave",
            "Unpaid Leave",
            "Special Leave"
    );
    private static final List<String> DEDUCTION_TYPE_OPTIONS = List.of("Deducted Leave", "Non-deducted Leave");
    private static final List<String> LEAVE_UNIT_OPTIONS = List.of("Full Day", "Half Day AM", "Half Day PM");
    private static final List<String> STATUS_OPTIONS = List.of(
            "All",
            "Draft",
            "Requested",
            "Approved",
            "Rejected",
            "Cancelled"
    );

    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public LeaveContextResponse getContext(AuthenticatedUser authenticatedUser) {
        EmployeeContext employee = resolveEmployee(authenticatedUser);
        List<EmployeeContext> employees = leaveRequestRepository.findEmployees(employee.tenantId(), employee.coId());
        List<DepartmentRow> departments = leaveRequestRepository.findDepartments(employee.tenantId(), employee.coId());

        return new LeaveContextResponse(
                new LeaveApplicantResponse(
                        employee.empNm(),
                        textOrDefault(employee.deptNm(), "-"),
                        textOrDefault(employee.jobTitleNm(), "-")
                ),
                toBalance(employee, employee.currVacCnt().add(employee.preVacCnt())),
                LEAVE_TYPE_OPTIONS,
                DEDUCTION_TYPE_OPTIONS,
                LEAVE_UNIT_OPTIONS,
                STATUS_OPTIONS,
                toOrganizations(departments, employees),
                employees.stream().map(this::toParticipant).toList()
        );
    }

    @Override
    public LeaveListResponse getLeaves(
            AuthenticatedUser authenticatedUser,
            String startDateFrom,
            String startDateTo,
            String status
    ) {
        EmployeeContext employee = resolveEmployee(authenticatedUser);
        LocalDate from = parseOptionalDate(startDateFrom, "LEAVE_START_FROM_INVALID");
        LocalDate to = parseOptionalDate(startDateTo, "LEAVE_START_TO_INVALID");
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_RANGE_INVALID", "Start date from must be before start date to");
        }

        List<LeaveRow> rows = leaveRequestRepository.findLeaveRows(
                employee.tenantId(),
                employee.coId(),
                employee.empId(),
                from,
                to,
                LeaveBizConst.statusCodeFromLabel(status)
        );
        Map<Long, List<ApprovalRow>> approvalsByLeaveId = approvalsByLeaveId(rows.stream().map(LeaveRow::id).toList());
        List<LeaveRequestResponse> responses = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            responses.add(toResponse(rows.get(i), i + 1, employee, approvalsByLeaveId.getOrDefault(rows.get(i).id(), List.of())));
        }

        return new LeaveListResponse(
                toBalance(employee, employee.currVacCnt().add(employee.preVacCnt())),
                responses
        );
    }

    @Override
    public LeaveCalculateResponse calculate(AuthenticatedUser authenticatedUser, LeaveCalculateRequest request) {
        EmployeeContext employee = resolveEmployee(authenticatedUser);
        LeaveInput input = normalizeInput(request);
        List<DuplicateRow> duplicates = leaveRequestRepository.findDuplicates(
                employee.tenantId(),
                employee.coId(),
                employee.empId(),
                parseOptionalLong(request == null ? null : request.leaveId()),
                input.startDate(),
                input.endDate()
        );
        BigDecimal days = calculateLeaveDays(employee, input.dayTpCd(), input.startDate(), input.endDate());

        if (!duplicates.isEmpty()) {
            return new LeaveCalculateResponse(
                    "D",
                    "There are duplicate leave or business trip dates.",
                    employee.preVacCnt(),
                    employee.currVacCnt(),
                    days,
                    employee.preVacCnt().add(employee.currVacCnt()),
                    toDuplicateResponses(duplicates)
            );
        }

        BalanceAllocation allocation = allocateBalance(
                employee,
                new BalanceState(employee.preVacCnt(), employee.currVacCnt()),
                input.vacTpCd(),
                input.deductYn(),
                input.startDate(),
                days,
                true
        );
        return new LeaveCalculateResponse(
                allocation.valid() ? "S" : "E",
                allocation.message(),
                allocation.nextPreVacCnt(),
                allocation.nextCurrVacCnt(),
                days,
                allocation.nextPreVacCnt().add(allocation.nextCurrVacCnt()),
                List.of()
        );
    }

    @Override
    @Transactional
    public LeaveRequestResponse createLeave(AuthenticatedUser authenticatedUser, LeaveSaveRequest request) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        EmployeeContext employee = resolveEmployeeForUpdate(user);
        String actor = actor(user);
        PreparedLeave preparedLeave = prepareLeave(employee, null, null, request, actor);

        Long leaveId = leaveRequestRepository.insertLeave(preparedLeave.values());
        insertApprovals(leaveId, employee, request, preparedLeave.status(), actor);
        if (preparedLeave.updateBalanceYn()) {
            leaveRequestRepository.updateHrProfileBalance(
                    employee.profileId(),
                    preparedLeave.nextBalance().preVacCnt(),
                    preparedLeave.nextBalance().currVacCnt(),
                    actor
            );
        }
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveRequestResponse updateLeave(AuthenticatedUser authenticatedUser, Long leaveId, LeaveSaveRequest request) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        LeaveRow current = lockLeave(user, leaveId);
        ensureRequester(current, resolveEmployee(user));
        if (!canEdit(current)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_UPDATE_NOT_ALLOWED", "Only draft or rejected leave requests can be updated");
        }

        EmployeeContext employee = leaveRequestRepository.lockEmployeeProfile(current.tenantId(), current.coId(), current.empId());
        String actor = actor(user);
        BalanceState restoredBalance = restoreBalance(employee, current);
        PreparedLeave preparedLeave = prepareLeave(employee, restoredBalance, leaveId, request, actor);

        leaveRequestRepository.updateLeave(leaveId, preparedLeave.values());
        leaveRequestRepository.deleteApprovals(leaveId);
        insertApprovals(leaveId, employee, request, preparedLeave.status(), actor);
        leaveRequestRepository.updateHrProfileBalance(
                employee.profileId(),
                preparedLeave.nextBalance().preVacCnt(),
                preparedLeave.nextBalance().currVacCnt(),
                actor
        );
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancelLeave(AuthenticatedUser authenticatedUser, Long leaveId) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        LeaveRow current = lockLeave(user, leaveId);
        EmployeeContext requester = resolveEmployee(user);
        ensureRequester(current, requester);
        if (!canCancel(current)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_CANCEL_NOT_ALLOWED", "Only draft or requested leave requests can be cancelled");
        }

        String actor = actor(user);
        if (LeaveBizConst.isReservedStatus(current.stsCd(), current.aprvStsCd())) {
            EmployeeContext employee = leaveRequestRepository.lockEmployeeProfile(current.tenantId(), current.coId(), current.empId());
            BalanceState restoredBalance = restoreBalance(employee, current);
            leaveRequestRepository.updateHrProfileBalance(employee.profileId(), restoredBalance.preVacCnt(), restoredBalance.currVacCnt(), actor);
        }
        leaveRequestRepository.cancelLeave(leaveId, actor);
        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveRequestResponse confirmLeave(AuthenticatedUser authenticatedUser, Long leaveId) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        EmployeeContext approver = resolveEmployee(user);
        LeaveRow current = lockLeave(user, leaveId);
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(current.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(current.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVAL_NOT_REQUESTED", "Only requested leave can be approved");
        }
        if (Objects.equals(current.empId(), approver.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_SELF_APPROVAL_NOT_ALLOWED", "Requester cannot approve own leave");
        }
        ApprovalRow currentApproval = leaveRequestRepository
                .findCurrentApproval(current.id(), approver.empId(), current.aprvCurrLvlNo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "LEAVE_APPROVAL_FORBIDDEN", "Current user is not the current approver"));
        if (!LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(currentApproval.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVAL_STEP_INVALID", "Current approval step is not waiting for approval");
        }

        String actor = actor(user);
        leaveRequestRepository.updateApprovalStatus(current.id(), current.aprvCurrLvlNo(), LeaveBizConst.APRV_APPROVED, "Y", actor);
        if (current.aprvCurrLvlNo() >= current.aprvTotLvlNo()) {
            leaveRequestRepository.updateLeaveApprovalStatus(current.id(), current.aprvTotLvlNo(), LeaveBizConst.APRV_APPROVED, null, actor);
        } else {
            int nextLevel = current.aprvCurrLvlNo() + 1;
            leaveRequestRepository.updateLeaveApprovalStatus(current.id(), nextLevel, LeaveBizConst.APRV_REQUESTED, null, actor);
            leaveRequestRepository.updateApprovalStatus(current.id(), nextLevel, LeaveBizConst.APRV_REQUESTED, "N", actor);
        }

        return findResponse(user, leaveId);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectLeave(AuthenticatedUser authenticatedUser, Long leaveId, LeaveActionRequest request) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        EmployeeContext approver = resolveEmployee(user);
        LeaveRow current = lockLeave(user, leaveId);
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(current.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(current.aprvStsCd())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_REJECT_NOT_REQUESTED", "Only requested leave can be rejected");
        }
        if (Objects.equals(current.empId(), approver.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_SELF_REJECT_NOT_ALLOWED", "Requester cannot reject own leave");
        }
        leaveRequestRepository
                .findCurrentApproval(current.id(), approver.empId(), current.aprvCurrLvlNo())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "LEAVE_REJECT_FORBIDDEN", "Current user is not the current approver"));

        String actor = actor(user);
        if (LeaveBizConst.isReservedStatus(current.stsCd(), current.aprvStsCd())) {
            EmployeeContext employee = leaveRequestRepository.lockEmployeeProfile(current.tenantId(), current.coId(), current.empId());
            BalanceState restoredBalance = restoreBalance(employee, current);
            leaveRequestRepository.updateHrProfileBalance(employee.profileId(), restoredBalance.preVacCnt(), restoredBalance.currVacCnt(), actor);
        }
        leaveRequestRepository.updateLeaveApprovalStatus(
                current.id(),
                0,
                LeaveBizConst.APRV_REJECTED,
                request == null ? "" : textOrDefault(request.rejectReason(), ""),
                actor
        );
        leaveRequestRepository.rejectApprovals(current.id(), actor);
        return findResponse(user, leaveId);
    }

    private PreparedLeave prepareLeave(
            EmployeeContext employee,
            BalanceState baseBalance,
            Long excludeLeaveId,
            LeaveSaveRequest request,
            String actor
    ) {
        LeaveInput input = normalizeInput(request);
        List<DuplicateRow> duplicates = leaveRequestRepository.findDuplicates(
                employee.tenantId(),
                employee.coId(),
                employee.empId(),
                excludeLeaveId,
                input.startDate(),
                input.endDate()
        );
        if (!duplicates.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_DATE_DUPLICATED", "Leave dates overlap with another leave or business trip");
        }

        BigDecimal days = calculateLeaveDays(employee, input.dayTpCd(), input.startDate(), input.endDate());
        String status = resolveSaveStatus(request, normalizeApprovalSteps(request == null ? null : request.approvalSteps()).isEmpty());
        boolean reserveBalance = LeaveBizConst.APRV_REQUESTED.equals(status) || LeaveBizConst.APRV_APPROVED.equals(status);
        BalanceState startBalance = baseBalance == null
                ? new BalanceState(employee.preVacCnt(), employee.currVacCnt())
                : baseBalance;
        BalanceAllocation allocation = allocateBalance(
                employee,
                startBalance,
                input.vacTpCd(),
                input.deductYn(),
                input.startDate(),
                days,
                reserveBalance
        );
        if (!allocation.valid()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_BALANCE_INVALID", allocation.message());
        }

        List<NormalizedApprovalStep> approvalSteps = normalizeApprovalSteps(request == null ? null : request.approvalSteps());
        int totalLevel = LeaveBizConst.APRV_APPROVED.equals(status) ? 0 : approvalSteps.size();
        int currentLevel = LeaveBizConst.APRV_REQUESTED.equals(status) && totalLevel > 0 ? 1 : 0;
        TimeRange timeRange = resolveTimeRange(input.dayTpCd());

        return new PreparedLeave(
                new LeaveWriteValues(
                        employee.tenantId(),
                        employee.coId(),
                        employee.deptId(),
                        employee.empId(),
                        employee.deptId() == null ? null : String.valueOf(employee.deptId()),
                        employee.empNo(),
                        input.vacTpCd(),
                        input.dayTpCd(),
                        input.deductYn(),
                        allocation.currUseVacCnt(),
                        allocation.preUseVacCnt(),
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
                allocation.nextBalance(),
                !startBalance.equals(allocation.nextBalance()),
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
            EmployeeContext employee,
            LeaveSaveRequest request,
            String leaveStatus,
            String actor
    ) {
        Map<Long, EmployeeContext> employeeById = leaveRequestRepository.findEmployees(employee.tenantId(), employee.coId())
                .stream()
                .collect(Collectors.toMap(EmployeeContext::empId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<NormalizedApprovalStep> approvalSteps = normalizeApprovalSteps(request == null ? null : request.approvalSteps());
        for (NormalizedApprovalStep step : approvalSteps) {
            for (Long approverId : step.approverIds()) {
                EmployeeContext approver = requireApprovalEmployee(employeeById, approverId);
                String approvalStatus = resolveApprovalStepStatus(leaveStatus, step.order());
                leaveRequestRepository.insertApproval(new ApprovalWriteValues(
                        leaveId,
                        employee.tenantId(),
                        employee.coId(),
                        employee.deptId(),
                        employee.empId(),
                        approver.empId(),
                        employee.empNo(),
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
            EmployeeContext referrer = requireApprovalEmployee(employeeById, referrerId);
            leaveRequestRepository.insertApproval(new ApprovalWriteValues(
                    leaveId,
                    employee.tenantId(),
                    employee.coId(),
                    employee.deptId(),
                    employee.empId(),
                    referrer.empId(),
                    employee.empNo(),
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

    private EmployeeContext requireApprovalEmployee(Map<Long, EmployeeContext> employeeById, Long employeeId) {
        EmployeeContext employee = employeeById.get(employeeId);
        if (employee == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_APPROVER_INVALID", "Selected approver is not an active employee in this company");
        }
        return employee;
    }

    private LeaveRequestResponse findResponse(AuthenticatedUser user, Long leaveId) {
        EmployeeContext employee = resolveEmployee(user);
        LeaveRow row = leaveRequestRepository.findLeaveRow(employee.tenantId(), employee.coId(), leaveId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LEAVE_NOT_FOUND", "Leave request not found"));
        List<ApprovalRow> approvals = leaveRequestRepository.findApprovals(List.of(row.id()));
        return toResponse(row, 1, employee, approvals);
    }

    private LeaveRow lockLeave(AuthenticatedUser user, Long leaveId) {
        if (leaveId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_ID_REQUIRED", "Leave id is required");
        }
        AuthenticatedUser authenticatedUser = requireUser(user);
        EmployeeContext employee = resolveEmployee(authenticatedUser);
        try {
            return leaveRequestRepository.lockLeaveRow(employee.tenantId(), employee.coId(), leaveId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "LEAVE_NOT_FOUND", "Leave request not found");
        }
    }

    private LeaveRequestResponse toResponse(
            LeaveRow row,
            int no,
            EmployeeContext actorEmployee,
            List<ApprovalRow> approvals
    ) {
        String status = LeaveBizConst.statusLabel(row.stsCd(), row.aprvStsCd());
        List<ApprovalRow> approvers = approvals.stream()
                .filter(approval -> LeaveBizConst.APRV_TYPE_APPROVER.equalsIgnoreCase(approval.aprvTpCd()))
                .toList();
        List<ApprovalRow> referrers = approvals.stream()
                .filter(approval -> LeaveBizConst.APRV_TYPE_REFERENCE.equalsIgnoreCase(approval.aprvTpCd()))
                .toList();

        return new LeaveRequestResponse(
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
                referrers.stream().map(this::toParticipant).toList(),
                canEdit(row) && Objects.equals(row.empId(), actorEmployee.empId()),
                canCancel(row) && Objects.equals(row.empId(), actorEmployee.empId()),
                canApprove(row, actorEmployee, approvers),
                canApprove(row, actorEmployee, approvers),
                resolveMyRole(row, actorEmployee, approvals)
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

    private List<LeaveApprovalStepResponse> toApprovalSteps(Long leaveId, List<ApprovalRow> approvers) {
        return approvers.stream()
                .collect(Collectors.groupingBy(ApprovalRow::aprvLvlNo, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new ApprovalStepResponseAdapter(
                        "approval-step-" + leaveId + "-" + entry.getKey(),
                        entry.getKey(),
                        entry.getValue().stream().map(this::toParticipant).toList()
                ))
                .sorted(Comparator.comparing(ApprovalStepResponseAdapter::order))
                .map(adapter -> new LeaveApprovalStepResponse(adapter.id(), adapter.order(), adapter.users()))
                .toList();
    }

    private LeaveParticipantResponse toParticipant(ApprovalRow approval) {
        return new LeaveParticipantResponse(
                approval.aprvEmpId() == null ? "" : String.valueOf(approval.aprvEmpId()),
                textOrDefault(approval.approverName(), "-"),
                textOrDefault(approval.approverDeptName(), "-"),
                textOrDefault(approval.approverJobTitle(), "-"),
                orgId(approval.deptId())
        );
    }

    private LeaveParticipantResponse toParticipant(EmployeeContext employee) {
        return new LeaveParticipantResponse(
                String.valueOf(employee.empId()),
                textOrDefault(employee.empNm(), "-"),
                textOrDefault(employee.deptNm(), "-"),
                textOrDefault(employee.jobTitleNm(), "-"),
                orgId(employee.deptId())
        );
    }

    private List<LeaveOrganizationResponse> toOrganizations(List<DepartmentRow> departments, List<EmployeeContext> employees) {
        Map<Long, List<DepartmentRow>> childrenByParent = new LinkedHashMap<>();
        for (DepartmentRow department : departments) {
            childrenByParent.computeIfAbsent(department.parentDeptId(), key -> new ArrayList<>()).add(department);
        }
        List<LeaveOrganizationResponse> roots = childrenByParent.getOrDefault(null, List.of())
                .stream()
                .map(department -> toOrganization(department, childrenByParent))
                .collect(Collectors.toCollection(ArrayList::new));
        if (roots.isEmpty()) {
            roots.addAll(departments.stream()
                    .map(department -> new LeaveOrganizationResponse(orgId(department.deptId()), department.deptNm(), List.of()))
                    .toList());
        }
        boolean hasUnassigned = employees.stream().anyMatch(employee -> employee.deptId() == null);
        if (hasUnassigned) {
            roots.add(new LeaveOrganizationResponse(orgId(null), "Unassigned", List.of()));
        }
        return roots;
    }

    private LeaveOrganizationResponse toOrganization(
            DepartmentRow department,
            Map<Long, List<DepartmentRow>> childrenByParent
    ) {
        List<LeaveOrganizationResponse> children = childrenByParent.getOrDefault(department.deptId(), List.of())
                .stream()
                .map(child -> toOrganization(child, childrenByParent))
                .toList();
        return new LeaveOrganizationResponse(orgId(department.deptId()), textOrDefault(department.deptNm(), "-"), children);
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

    private boolean canApprove(LeaveRow row, EmployeeContext employee, List<ApprovalRow> approvers) {
        if (!LeaveBizConst.STS_ACTIVE.equalsIgnoreCase(row.stsCd())
                || !LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(row.aprvStsCd())) {
            return false;
        }
        if (Objects.equals(row.empId(), employee.empId())) {
            return false;
        }
        return approvers.stream()
                .anyMatch(approval -> Objects.equals(approval.aprvEmpId(), employee.empId())
                        && Objects.equals(approval.aprvLvlNo(), row.aprvCurrLvlNo())
                        && LeaveBizConst.APRV_REQUESTED.equalsIgnoreCase(approval.aprvStsCd()));
    }

    private String resolveMyRole(LeaveRow row, EmployeeContext employee, List<ApprovalRow> approvals) {
        if (Objects.equals(row.empId(), employee.empId())) {
            return LeaveBizConst.ROLE_REQUESTER;
        }
        return approvals.stream()
                .filter(approval -> Objects.equals(approval.aprvEmpId(), employee.empId()))
                .findFirst()
                .map(approval -> LeaveBizConst.APRV_TYPE_APPROVER.equalsIgnoreCase(approval.aprvTpCd())
                        ? LeaveBizConst.ROLE_APPROVER
                        : LeaveBizConst.ROLE_REFERRER)
                .orElse(LeaveBizConst.ROLE_REQUESTER);
    }

    private Map<Long, List<ApprovalRow>> approvalsByLeaveId(Collection<Long> leaveIds) {
        return leaveRequestRepository.findApprovals(leaveIds)
                .stream()
                .collect(Collectors.groupingBy(ApprovalRow::leaveReqId, LinkedHashMap::new, Collectors.toList()));
    }

    private BalanceAllocation allocateBalance(
            EmployeeContext employee,
            BalanceState balance,
            String vacTpCd,
            String deductYn,
            LocalDate startDate,
            BigDecimal days,
            boolean reserveBalance
    ) {
        if (!reserveBalance || !LeaveBizConst.DEDUCT_Y.equalsIgnoreCase(deductYn)) {
            return BalanceAllocation.valid(ZERO, ZERO, days, balance, "Calculated successfully.");
        }
        if (LeaveBizConst.VAC_ANNUAL.equals(vacTpCd) && balance.preVacCnt().compareTo(ZERO) > 0 && startDate.getMonthValue() >= 4) {
            return BalanceAllocation.invalid(balance, "Previous year's vacation must be used before April.");
        }

        BigDecimal preUse = min(balance.preVacCnt(), days);
        BigDecimal remaining = days.subtract(preUse);
        BigDecimal currUse = remaining.max(BigDecimal.ZERO);
        BigDecimal nextPre = balance.preVacCnt().subtract(preUse);
        BigDecimal nextCurr = balance.currVacCnt().subtract(currUse);
        if (nextCurr.compareTo(ZERO) < 0) {
            return BalanceAllocation.invalid(balance, "The number of vacation days requested is greater than the number of vacation days remaining.");
        }
        return BalanceAllocation.valid(preUse, currUse, days, new BalanceState(scale(nextPre), scale(nextCurr)), "Calculated successfully.");
    }

    private BalanceState restoreBalance(EmployeeContext employee, LeaveRow row) {
        if (!LeaveBizConst.isReservedStatus(row.stsCd(), row.aprvStsCd())) {
            return new BalanceState(employee.preVacCnt(), employee.currVacCnt());
        }
        BigDecimal maxCurr = employee.stdVacCnt().add(employee.incVacCnt());
        BigDecimal restoredCurr = employee.currVacCnt().add(row.currUseVacCnt());
        BigDecimal restoredPre = employee.preVacCnt().add(row.preUseVacCnt());
        if (restoredCurr.compareTo(maxCurr) > 0) {
            restoredPre = restoredPre.add(restoredCurr.subtract(maxCurr));
            restoredCurr = maxCurr;
        }
        return new BalanceState(scale(restoredPre), scale(restoredCurr));
    }

    private BigDecimal calculateLeaveDays(EmployeeContext employee, String dayTpCd, LocalDate startDate, LocalDate endDate) {
        if (LeaveBizConst.isHalfDay(dayTpCd)) {
            return HALF_DAY;
        }
        Set<LocalDate> holidays = holidayDates(employee, startDate, endDate);
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

    private Set<LocalDate> holidayDates(EmployeeContext employee, LocalDate startDate, LocalDate endDate) {
        String countryCode = textOrDefault(employee.countryCode(), "DE");
        String regionCode = textOrDefault(employee.regionCode(), "ALL");
        Set<LocalDate> holidays = new LinkedHashSet<>(leaveRequestRepository.findRegionHolidays(countryCode, regionCode, startDate, endDate));
        for (CompanyHolidayRow holiday : leaveRequestRepository.findCompanyHolidays(employee.tenantId(), employee.coId(), startDate, endDate)) {
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

    private LeaveInput normalizeInput(LeaveCalculateRequest request) {
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

    private List<NormalizedApprovalStep> normalizeApprovalSteps(List<LeaveApprovalStepRequest> steps) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }
        List<NormalizedApprovalStep> normalized = new ArrayList<>();
        int order = 1;
        for (LeaveApprovalStepRequest step : steps) {
            LinkedHashSet<Long> approverIds = new LinkedHashSet<>();
            if (step != null && step.userIds() != null) {
                for (String userId : step.userIds()) {
                    Long parsed = parseOptionalLong(userId);
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

    private void ensureRequester(LeaveRow row, EmployeeContext employee) {
        if (!Objects.equals(row.empId(), employee.empId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LEAVE_REQUESTER_FORBIDDEN", "Only the requester can change this leave request");
        }
    }

    private EmployeeContext resolveEmployee(AuthenticatedUser authenticatedUser) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        EmployeeContext employee = leaveRequestRepository.findEmployeeContext(user.tenantId(), user.usrId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_EMPLOYEE_CONTEXT_NOT_FOUND", "Authenticated user is not connected to an active employee"));
        if (employee.profileId() == null) {
            leaveRequestRepository.createDefaultHrProfile(employee);
            employee = leaveRequestRepository.findEmployeeContext(user.tenantId(), user.usrId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "LEAVE_EMPLOYEE_CONTEXT_NOT_FOUND", "Authenticated user is not connected to an active employee"));
        }
        return employee;
    }

    private EmployeeContext resolveEmployeeForUpdate(AuthenticatedUser authenticatedUser) {
        EmployeeContext employee = resolveEmployee(authenticatedUser);
        return leaveRequestRepository.lockEmployeeProfile(employee.tenantId(), employee.coId(), employee.empId());
    }

    private AuthenticatedUser requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.tenantId() == null || authenticatedUser.usrId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_AUTHENTICATION_REQUIRED", "Authentication is required");
        }
        return authenticatedUser;
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

    private List<LeaveDuplicateResponse> toDuplicateResponses(List<DuplicateRow> duplicates) {
        return duplicates.stream()
                .map(duplicate -> new LeaveDuplicateResponse(
                        duplicate.type(),
                        String.valueOf(duplicate.id()),
                        formatDate(duplicate.startDate()),
                        formatDate(duplicate.endDate())
                ))
                .toList();
    }

    private LeaveBalanceResponse toBalance(EmployeeContext employee, BigDecimal afterRequestDays) {
        return new LeaveBalanceResponse(
                scale(employee.currVacCnt().add(employee.preVacCnt())),
                scale(afterRequestDays),
                scale(employee.preVacCnt()),
                scale(employee.currVacCnt())
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

    private static String actor(AuthenticatedUser user) {
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

    private record BalanceAllocation(
            boolean valid,
            BigDecimal preUseVacCnt,
            BigDecimal currUseVacCnt,
            BigDecimal useVacCnt,
            BalanceState nextBalance,
            String message
    ) {
        private static BalanceAllocation valid(
                BigDecimal preUseVacCnt,
                BigDecimal currUseVacCnt,
                BigDecimal useVacCnt,
                BalanceState nextBalance,
                String message
        ) {
            return new BalanceAllocation(true, scale(preUseVacCnt), scale(currUseVacCnt), scale(useVacCnt), nextBalance, message);
        }

        private static BalanceAllocation invalid(BalanceState balance, String message) {
            return new BalanceAllocation(false, ZERO, ZERO, ZERO, balance, message);
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
            List<LeaveParticipantResponse> users
    ) {
    }
}

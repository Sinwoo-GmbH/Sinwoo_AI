"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { LeaveBalanceSummary } from "@/components/features/attendance/request-leave/leave-balance-summary";
import { LeaveConfirmDialog } from "@/components/features/attendance/request-leave/leave-confirm-dialog";
import { LeaveFilterBar } from "@/components/features/attendance/request-leave/leave-filter-bar";
import type {
  DeductionType,
  LeaveApplicantProfile,
  LeaveFilterStatus,
  LeaveFilterValue,
  LeaveOrganizationNode,
  LeaveParticipant,
  LeaveRequestFormValue,
  LeaveRequestRecord,
  LeaveStatus,
  LeaveType,
  LeaveUnit,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import {
  DEFAULT_LEAVE_FILTER_VALUE,
  DEDUCTION_TYPE_OPTIONS,
  LEAVE_STATUS_OPTIONS,
  LEAVE_TYPE_OPTIONS,
  LEAVE_UNIT_OPTIONS,
  cloneApprovalSteps,
  formatLeaveDays,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import { LeaveRequestModal } from "@/components/features/attendance/request-leave/leave-request-modal";
import { LeaveRequestTable } from "@/components/features/attendance/request-leave/leave-request-table";
import {
  LEAVE_API_PATH,
  type LeaveContextRes,
  type LeaveListRes,
  type LeaveRequestRes,
  type LeaveSaveReq,
} from "@/lib/api/leave-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type RequestLeavePageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

type LeaveDialogState =
  | {
      open: false;
      mode: "create";
      value: LeaveRequestFormValue;
    }
  | {
      open: true;
      mode: "create" | "edit" | "view";
      value: LeaveRequestFormValue;
    };

type ApiErrorBody = {
  message?: string;
  error?: string;
  code?: string;
};

function localTodayIsoDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function createBlankLeaveRequestFormValue(): LeaveRequestFormValue {
  const today = localTodayIsoDate();
  return {
    id: null,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: today,
    endDate: today,
    attachmentName: null,
    reason: "",
    approvalSteps: [{ id: "approval-step-1", order: 1, users: [] }],
    ccs: [],
  };
}

function toTypedOptions<T extends string>(
  source: readonly string[] | undefined,
  fallback: readonly T[]
): readonly T[] {
  return source?.length ? (source as readonly T[]) : fallback;
}

function toNumber(value: number | string | null | undefined) {
  if (typeof value === "number") return value;
  if (typeof value === "string") {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
  return 0;
}

function normalizeStatus(status: string): LeaveStatus {
  const allowed: readonly LeaveStatus[] = [
    "Draft",
    "Requested",
    "Approved",
    "Rejected",
    "Cancelled",
    "Admin Cancelled",
  ];
  return allowed.includes(status as LeaveStatus) ? (status as LeaveStatus) : "Draft";
}

function normalizeFilterStatus(status: string): LeaveFilterStatus {
  return status === "All" ? "All" : normalizeStatus(status);
}

function toParticipant(user: LeaveParticipant): LeaveParticipant {
  return {
    id: user.id,
    name: user.name,
    department: user.department,
    position: user.position,
    orgId: user.orgId,
  };
}

function toLeaveRequestRecord(response: LeaveRequestRes): LeaveRequestRecord {
  return {
    id: response.id,
    no: response.no,
    leaveType: response.leaveType as LeaveType,
    deductionType: response.deductionType as DeductionType,
    leaveUnit: response.leaveUnit as LeaveUnit,
    startDate: response.startDate,
    endDate: response.endDate,
    days: toNumber(response.days),
    approverStatus: response.approverStatus as LeaveRequestRecord["approverStatus"],
    status: normalizeStatus(response.status),
    createdAt: response.createdAt,
    attachmentName: response.attachmentName ?? null,
    reason: response.reason ?? "",
    approvalSteps: cloneApprovalSteps(
      (response.approvalSteps ?? []).map((step) => ({
        id: step.id,
        order: step.order,
        users: step.users.map(toParticipant),
      }))
    ),
    ccs: (response.ccs ?? []).map(toParticipant),
    canEdit: response.canEdit,
    canCancel: response.canCancel,
    canApprove: response.canApprove,
    canReject: response.canReject,
    myRoleCd: response.myRoleCd,
  };
}

function toLeaveRequestFormValue(record: LeaveRequestRecord): LeaveRequestFormValue {
  return {
    id: record.id,
    leaveType: record.leaveType,
    deductionType: record.deductionType,
    leaveUnit: record.leaveUnit,
    startDate: record.startDate,
    endDate: record.endDate,
    attachmentName: record.attachmentName,
    reason: record.reason ?? "",
    approvalSteps: cloneApprovalSteps(record.approvalSteps),
    ccs: record.ccs.map(toParticipant),
  };
}

function toSaveRequest(
  value: LeaveRequestFormValue,
  nextStatus: "Draft" | "Requested"
): LeaveSaveReq {
  return {
    leaveType: value.leaveType,
    deductionType: value.deductionType,
    leaveUnit: value.leaveUnit,
    startDate: value.startDate,
    endDate: value.endDate,
    attachmentName: value.attachmentName,
    reason: value.reason,
    approvalSteps: value.approvalSteps.map((step, index) => ({
      order: index + 1,
      userIds: step.users.map((user) => user.id),
    })),
    ccIds: value.ccs.map((user) => user.id),
    nextStatus,
  };
}

function buildLeaveListQuery(filters: LeaveFilterValue) {
  const params = new URLSearchParams();
  if (filters.startDateFrom) params.set("startDateFrom", filters.startDateFrom);
  if (filters.startDateTo) params.set("startDateTo", filters.startDateTo);
  if (filters.status && filters.status !== "All") params.set("status", filters.status);
  const query = params.toString();
  return query ? `?${query}` : "";
}

async function parseError(response: Response) {
  try {
    const body = (await response.json()) as ApiErrorBody;
    return body.message ?? body.error ?? body.code ?? "Request failed.";
  } catch {
    return "Request failed.";
  }
}

export function RequestLeavePage({
  accessToken,
  locale: _locale,
  onLoadingChange,
  onUnauthorized,
}: RequestLeavePageProps) {
  const [context, setContext] = useState<LeaveContextRes | null>(null);
  const [requests, setRequests] = useState<LeaveRequestRecord[]>([]);
  const [draftFilters, setDraftFilters] = useState(DEFAULT_LEAVE_FILTER_VALUE);
  const [activeFilters, setActiveFilters] = useState(DEFAULT_LEAVE_FILTER_VALUE);
  const [dialogState, setDialogState] = useState<LeaveDialogState>({
    open: false,
    mode: "create",
    value: createBlankLeaveRequestFormValue(),
  });
  const [cancelTarget, setCancelTarget] = useState<LeaveRequestRecord | null>(null);
  const [approveTarget, setApproveTarget] = useState<LeaveRequestRecord | null>(null);
  const [rejectTarget, setRejectTarget] = useState<LeaveRequestRecord | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const requestJson = useCallback(
    async <T,>(path: string, init?: RequestInit): Promise<T> => {
      if (!accessToken) {
        onUnauthorized();
        throw new Error("Authentication is required.");
      }

      const headers = new Headers(init?.headers);
      headers.set("Content-Type", "application/json");
      headers.set("Authorization", `Bearer ${accessToken}`);
      const response = await fetch(`${API_BASE_URL}${path}`, {
        ...init,
        headers,
      });

      if (response.status === 401) {
        onUnauthorized();
        throw new Error("Authentication is required.");
      }

      if (!response.ok) {
        throw new Error(await parseError(response));
      }

      if (response.status === 204) {
        return undefined as T;
      }
      return (await response.json()) as T;
    },
    [accessToken, onUnauthorized]
  );

  const runWithLoading = useCallback(
    async <T,>(work: () => Promise<T>) => {
      setBusy(true);
      onLoadingChange(true);
      try {
        return await work();
      } finally {
        setBusy(false);
        onLoadingChange(false);
      }
    },
    [onLoadingChange]
  );

  const loadContext = useCallback(async () => {
    try {
      const nextContext = await runWithLoading(() =>
        requestJson<LeaveContextRes>(`${LEAVE_API_PATH}/context`)
      );
      setContext(nextContext);
      setErrorMessage(null);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to load leave context.");
    }
  }, [requestJson, runWithLoading]);

  const loadLeaves = useCallback(
    async (filters: LeaveFilterValue) => {
      try {
        const list = await runWithLoading(() =>
          requestJson<LeaveListRes>(`${LEAVE_API_PATH}${buildLeaveListQuery(filters)}`)
        );
        setRequests((list.itemList ?? []).map(toLeaveRequestRecord));
        setContext((current) =>
          current
            ? {
                ...current,
                balance: list.balance,
              }
            : current
        );
        setErrorMessage(null);
      } catch (error) {
        setErrorMessage(error instanceof Error ? error.message : "Failed to load leave requests.");
      }
    },
    [requestJson, runWithLoading]
  );

  useEffect(() => {
    void loadContext();
  }, [loadContext]);

  useEffect(() => {
    void loadLeaves(activeFilters);
  }, [activeFilters, loadLeaves]);

  const applicant: LeaveApplicantProfile = context?.applicant ?? {
    name: "-",
    department: "-",
    position: "-",
  };
  const availableDays = toNumber(context?.balance.availableDays);
  const afterRequestDays = toNumber(context?.balance.afterRequestDays);
  const leaveTypeOptions = toTypedOptions<LeaveType>(context?.leaveTypeOptions, LEAVE_TYPE_OPTIONS);
  const deductionTypeOptions = toTypedOptions<DeductionType>(
    context?.deductionTypeOptions,
    DEDUCTION_TYPE_OPTIONS
  );
  const leaveUnitOptions = toTypedOptions<LeaveUnit>(context?.leaveUnitOptions, LEAVE_UNIT_OPTIONS);
  const statusOptions = toTypedOptions<LeaveFilterStatus>(
    context?.statusOptions?.map(normalizeFilterStatus),
    LEAVE_STATUS_OPTIONS
  );
  const organizations = (context?.organizations ?? []) as LeaveOrganizationNode[];
  const employees = (context?.employees ?? []) as LeaveParticipant[];

  const filteredRequests = useMemo(() => requests, [requests]);

  const closeDialog = () => {
    setDialogState({
      open: false,
      mode: "create",
      value: createBlankLeaveRequestFormValue(),
    });
  };

  const openCreateDialog = () => {
    setDialogState({
      open: true,
      mode: "create",
      value: createBlankLeaveRequestFormValue(),
    });
  };

  const openEditDialog = (record: LeaveRequestRecord) => {
    setDialogState({
      open: true,
      mode: "edit",
      value: toLeaveRequestFormValue(record),
    });
  };

  const openViewDialog = (record: LeaveRequestRecord) => {
    setDialogState({
      open: true,
      mode: "view",
      value: toLeaveRequestFormValue(record),
    });
  };

  const openRowDialog = (record: LeaveRequestRecord) => {
    if (record.canEdit) {
      openEditDialog(record);
      return;
    }

    openViewDialog(record);
  };

  const refreshAfterMutation = async () => {
    await loadContext();
    await loadLeaves(activeFilters);
  };

  const handleSave = async (
    nextValue: LeaveRequestFormValue,
    nextStatus: "Draft" | "Requested"
  ) => {
    try {
      const body = JSON.stringify(toSaveRequest(nextValue, nextStatus));
      await runWithLoading(() =>
        requestJson<LeaveRequestRes>(
          nextValue.id ? `${LEAVE_API_PATH}/${nextValue.id}` : LEAVE_API_PATH,
          {
            method: nextValue.id ? "PUT" : "POST",
            body,
          }
        )
      );
      closeDialog();
      await refreshAfterMutation();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to save leave request.");
    }
  };

  const handleConfirmCancelRequest = async () => {
    if (!cancelTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveRequestRes>(`${LEAVE_API_PATH}/${cancelTarget.id}/cancel`, {
          method: "PATCH",
        })
      );
      setCancelTarget(null);
      await refreshAfterMutation();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to cancel leave request.");
    }
  };

  const handleConfirmApprove = async () => {
    if (!approveTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveRequestRes>(`${LEAVE_API_PATH}/${approveTarget.id}/confirm`, {
          method: "POST",
        })
      );
      setApproveTarget(null);
      await refreshAfterMutation();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to approve leave request.");
    }
  };

  const handleConfirmReject = async () => {
    if (!rejectTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveRequestRes>(`${LEAVE_API_PATH}/${rejectTarget.id}/reject`, {
          method: "POST",
          body: JSON.stringify({ rejectReason }),
        })
      );
      setRejectTarget(null);
      setRejectReason("");
      await refreshAfterMutation();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to reject leave request.");
    }
  };

  return (
    <div className="mx-auto flex h-full min-h-0 max-w-[1440px] flex-col">
      <section
        id="workspace-request-leave-panel"
        className="-mt-1 flex min-h-0 flex-1 flex-col overflow-hidden rounded-[4px] border border-slate-300 bg-white px-2.5 py-2"
      >
        <div className="flex flex-col gap-1 xl:flex-row xl:items-end xl:justify-between">
          <div className="min-w-0 space-y-0.5 pr-2">
            <h2
              id="workspace-request-leave-title"
              className="text-[13px] font-semibold leading-4 text-slate-900"
            >
              Request Leave
            </h2>
            <p
              id="workspace-request-leave-description"
              className="text-[9px] leading-3 text-slate-500"
            >
              Create and review leave requests
            </p>
          </div>

          <LeaveBalanceSummary
            availableDays={availableDays}
            afterRequestDays={afterRequestDays}
          />
        </div>

        {errorMessage ? (
          <div className="mt-1 rounded-[3px] border border-rose-200 bg-rose-50 px-2 py-1 text-[10px] leading-4 text-rose-700">
            {errorMessage}
          </div>
        ) : null}

        <div className="mt-1 border-t border-slate-200 pt-1">
          <LeaveFilterBar
            value={draftFilters}
            statusOptions={statusOptions}
            onChange={setDraftFilters}
            onSearch={() => setActiveFilters(draftFilters)}
            onCreate={openCreateDialog}
          />
        </div>

        <div className="mt-1 min-h-0 flex-1">
          <LeaveRequestTable
            rows={filteredRequests}
            onEdit={openEditDialog}
            onView={openViewDialog}
            onCancel={setCancelTarget}
            onApprove={setApproveTarget}
            onReject={(record) => {
              setRejectReason("");
              setRejectTarget(record);
            }}
            onOpen={openRowDialog}
          />
        </div>
      </section>

      <LeaveRequestModal
        open={dialogState.open}
        mode={dialogState.mode}
        applicant={applicant}
        availableDays={availableDays}
        organizations={organizations}
        employees={employees}
        leaveTypeOptions={leaveTypeOptions}
        deductionTypeOptions={deductionTypeOptions}
        leaveUnitOptions={leaveUnitOptions}
        initialValue={dialogState.value}
        onClose={closeDialog}
        onSave={handleSave}
      />

      <LeaveConfirmDialog
        open={cancelTarget !== null}
        title="Cancel Leave Request"
        description={
          cancelTarget
            ? `Cancel ${cancelTarget.leaveType} request for ${cancelTarget.startDate}${cancelTarget.endDate !== cancelTarget.startDate ? ` to ${cancelTarget.endDate}` : ""}?`
            : ""
        }
        confirmLabel="Cancel Request"
        cancelLabel="Close"
        onClose={() => setCancelTarget(null)}
        onConfirm={handleConfirmCancelRequest}
      />

      <LeaveConfirmDialog
        open={approveTarget !== null}
        title="Approve Leave Request"
        description={
          approveTarget
            ? `Approve ${approveTarget.leaveType} request for ${formatLeaveDays(approveTarget.days)} day(s)?`
            : ""
        }
        confirmLabel="Approve"
        cancelLabel="Close"
        onClose={() => setApproveTarget(null)}
        onConfirm={handleConfirmApprove}
      />

      <LeaveConfirmDialog
        open={rejectTarget !== null}
        title="Reject Leave Request"
        description={
          rejectTarget
            ? `Reject ${rejectTarget.leaveType} request for ${formatLeaveDays(rejectTarget.days)} day(s)?`
            : ""
        }
        confirmLabel="Reject"
        cancelLabel="Close"
        onClose={() => {
          setRejectTarget(null);
          setRejectReason("");
        }}
        onConfirm={handleConfirmReject}
      >
        <textarea
          value={rejectReason}
          rows={3}
          onChange={(event) => setRejectReason(event.target.value)}
          className="min-h-[58px] w-full resize-y rounded-[3px] border border-slate-300 bg-white px-2 py-1.5 text-[10px] leading-4 text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
          placeholder="Reject reason"
        />
      </LeaveConfirmDialog>

      {busy ? (
        <div className="pointer-events-none fixed bottom-3 right-3 z-[95] rounded-[3px] border border-slate-300 bg-white px-2 py-1 text-[10px] text-slate-600 shadow-sm">
          Processing...
        </div>
      ) : null}
    </div>
  );
}

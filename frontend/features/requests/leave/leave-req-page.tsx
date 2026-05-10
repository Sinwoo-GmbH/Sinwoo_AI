"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { LeaveBalSum } from "@/features/requests/leave/leave-bal-sum";
import { LeaveConfirmDialog } from "@/features/requests/leave/leave-confirm-dialog";
import { LeaveFiltBar } from "@/features/requests/leave/leave-filt-bar";
import type {
  DeductionType,
  LeaveApplProfile,
  LeaveFiltStatus,
  LeaveFiltValue,
  LeaveOrgNode,
  LeavePart,
  LeaveReqFormValue,
  LeaveReqRec,
  LeaveStatus,
  LeaveType,
  LeaveUnit,
} from "@/features/requests/leave/leave-mock-data";
import {
  DEFAULT_LEAVE_FILT_VALUE,
  DEDUCTION_TYPE_OPTS,
  LEAVE_STATUS_OPTS,
  LEAVE_TYPE_OPTS,
  LEAVE_UNIT_OPTS,
  cloneAprvSteps,
  formatLeaveDays,
} from "@/features/requests/leave/leave-mock-data";
import { LeaveReqModal } from "@/features/requests/leave/leave-req-modal";
import { LeaveReqTable } from "@/features/requests/leave/leave-req-table";
import {
  LEAVE_API_PATH,
  type LeaveCtxResponse,
  type LeaveListResponse,
  type LeaveReqResponse,
  type LeaveSaveRequest,
} from "@/lib/api/leave-contract";
import type { LoginLocale } from "@/lib/i18n/login-cnt";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type LeaveReqPageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

type LeaveDialogState =
  | {
      open: false;
      mode: "create";
      value: LeaveReqFormValue;
    }
  | {
      open: true;
      mode: "create" | "edit" | "view";
      value: LeaveReqFormValue;
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

function createBlankLeaveReqFormValue(): LeaveReqFormValue {
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
    approvalSteps: [{ id: "approval-step-1", order: 1, usrs: [] }],
    ccs: [],
  };
}

function toTypedOpts<T extends string>(
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

function normalizeFiltStatus(status: string): LeaveFiltStatus {
  return status === "All" ? "All" : normalizeStatus(status);
}

function toPart(user: LeavePart): LeavePart {
  return {
    id: user.id,
    name: user.name,
    dept: user.dept,
    position: user.position,
    orgId: user.orgId,
  };
}

function toLeaveReqRec(response: LeaveReqResponse): LeaveReqRec {
  return {
    id: response.id,
    no: response.no,
    leaveType: response.leaveType as LeaveType,
    deductionType: response.deductionType as DeductionType,
    leaveUnit: response.leaveUnit as LeaveUnit,
    startDate: response.startDate,
    endDate: response.endDate,
    days: toNumber(response.days),
    approverStatus: response.approverStatus as LeaveReqRec["approverStatus"],
    status: normalizeStatus(response.status),
    createdAt: response.createdAt,
    attachmentName: response.attachmentName ?? null,
    reason: response.reason ?? "",
    approvalSteps: cloneAprvSteps(
      (response.approvalSteps ?? []).map((step) => ({
        id: step.id,
        order: step.order,
        usrs: step.usrs.map(toPart),
      }))
    ),
    ccs: (response.ccs ?? []).map(toPart),
    canEdit: response.canEdit,
    canCancel: response.canCancel,
    canApprove: response.canApprove,
    canReject: response.canReject,
    myRoleCd: response.myRoleCd,
  };
}

function toLeaveReqFormValue(rec: LeaveReqRec): LeaveReqFormValue {
  return {
    id: rec.id,
    leaveType: rec.leaveType,
    deductionType: rec.deductionType,
    leaveUnit: rec.leaveUnit,
    startDate: rec.startDate,
    endDate: rec.endDate,
    attachmentName: rec.attachmentName,
    reason: rec.reason ?? "",
    approvalSteps: cloneAprvSteps(rec.approvalSteps),
    ccs: rec.ccs.map(toPart),
  };
}

function toSaveRequest(
  value: LeaveReqFormValue,
  nextStatus: "Draft" | "Requested"
): LeaveSaveRequest {
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
      usrIds: step.usrs.map((user) => user.id),
    })),
    ccIds: value.ccs.map((user) => user.id),
    nextStatus,
  };
}

function buildLeaveListQuery(filters: LeaveFiltValue) {
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

export function LeaveReqPage({
  accessToken,
  onLoadingChange,
  onUnauthorized,
}: LeaveReqPageProps) {
  const [context, setContext] = useState<LeaveCtxResponse | null>(null);
  const [requests, setRequests] = useState<LeaveReqRec[]>([]);
  const [draftFilts, setDraftFilts] = useState(DEFAULT_LEAVE_FILT_VALUE);
  const [activeFilts, setActiveFilts] = useState(DEFAULT_LEAVE_FILT_VALUE);
  const [dialogState, setDialogState] = useState<LeaveDialogState>({
    open: false,
    mode: "create",
    value: createBlankLeaveReqFormValue(),
  });
  const [cancelTarget, setCancelTarget] = useState<LeaveReqRec | null>(null);
  const [approveTarget, setApproveTarget] = useState<LeaveReqRec | null>(null);
  const [rejectTarget, setRejectTarget] = useState<LeaveReqRec | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
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
        requestJson<LeaveCtxResponse>(`${LEAVE_API_PATH}/context`)
      );
      setContext(nextContext);
      setErrorMsg(null);
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : "Failed to load leave context.");
    }
  }, [requestJson, runWithLoading]);

  const loadLeaves = useCallback(
    async (filters: LeaveFiltValue) => {
      try {
        const list = await runWithLoading(() =>
          requestJson<LeaveListResponse>(`${LEAVE_API_PATH}${buildLeaveListQuery(filters)}`)
        );
        setRequests((list.itemList ?? []).map(toLeaveReqRec));
        setContext((current) =>
          current
            ? {
                ...current,
                balance: list.balance,
              }
            : current
        );
        setErrorMsg(null);
      } catch (error) {
        setErrorMsg(error instanceof Error ? error.message : "Failed to load leave requests.");
      }
    },
    [requestJson, runWithLoading]
  );

  useEffect(() => {
    void loadContext();
  }, [loadContext]);

  useEffect(() => {
    void loadLeaves(activeFilts);
  }, [activeFilts, loadLeaves]);

  const applicant: LeaveApplProfile = context?.applicant ?? {
    name: "-",
    dept: "-",
    position: "-",
  };
  const availableDays = toNumber(context?.balance.availableDays);
  const afterRequestDays = toNumber(context?.balance.afterRequestDays);
  const leaveTypeOpts = toTypedOpts<LeaveType>(context?.leaveTypeOpts, LEAVE_TYPE_OPTS);
  const deductionTypeOpts = toTypedOpts<DeductionType>(
    context?.deductionTypeOpts,
    DEDUCTION_TYPE_OPTS
  );
  const leaveUnitOpts = toTypedOpts<LeaveUnit>(context?.leaveUnitOpts, LEAVE_UNIT_OPTS);
  const statusOpts = toTypedOpts<LeaveFiltStatus>(
    context?.statusOpts?.map(normalizeFiltStatus),
    LEAVE_STATUS_OPTS
  );
  const organizations = (context?.organizations ?? []) as LeaveOrgNode[];
  const emps = (context?.emps ?? []) as LeavePart[];

  const filteredRequests = useMemo(() => requests, [requests]);

  const closeDialog = () => {
    setDialogState({
      open: false,
      mode: "create",
      value: createBlankLeaveReqFormValue(),
    });
  };

  const openCreateDialog = () => {
    setDialogState({
      open: true,
      mode: "create",
      value: createBlankLeaveReqFormValue(),
    });
  };

  const openEditDialog = (rec: LeaveReqRec) => {
    setDialogState({
      open: true,
      mode: "edit",
      value: toLeaveReqFormValue(rec),
    });
  };

  const openViewDialog = (rec: LeaveReqRec) => {
    setDialogState({
      open: true,
      mode: "view",
      value: toLeaveReqFormValue(rec),
    });
  };

  const openRowDialog = (rec: LeaveReqRec) => {
    if (rec.canEdit) {
      openEditDialog(rec);
      return;
    }

    openViewDialog(rec);
  };

  const refreshAfterMutation = async () => {
    await loadContext();
    await loadLeaves(activeFilts);
  };

  const handleSave = async (
    nextValue: LeaveReqFormValue,
    nextStatus: "Draft" | "Requested"
  ) => {
    try {
      const body = JSON.stringify(toSaveRequest(nextValue, nextStatus));
      await runWithLoading(() =>
        requestJson<LeaveReqResponse>(
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
      setErrorMsg(error instanceof Error ? error.message : "Failed to save leave request.");
    }
  };

  const handleConfirmCancelRequest = async () => {
    if (!cancelTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveReqResponse>(`${LEAVE_API_PATH}/${cancelTarget.id}/cancel`, {
          method: "PATCH",
        })
      );
      setCancelTarget(null);
      await refreshAfterMutation();
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : "Failed to cancel leave request.");
    }
  };

  const handleConfirmApprove = async () => {
    if (!approveTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveReqResponse>(`${LEAVE_API_PATH}/${approveTarget.id}/confirm`, {
          method: "POST",
        })
      );
      setApproveTarget(null);
      await refreshAfterMutation();
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : "Failed to approve leave request.");
    }
  };

  const handleConfirmReject = async () => {
    if (!rejectTarget) {
      return;
    }

    try {
      await runWithLoading(() =>
        requestJson<LeaveReqResponse>(`${LEAVE_API_PATH}/${rejectTarget.id}/reject`, {
          method: "POST",
          body: JSON.stringify({ rejectReason }),
        })
      );
      setRejectTarget(null);
      setRejectReason("");
      await refreshAfterMutation();
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : "Failed to reject leave request.");
    }
  };

  return (
    <div className="mx-auto flex h-full min-h-0 max-w-[1440px] flex-col">
      <section
        id="wsp-request-leave-panel"
        className="-mt-1 flex min-h-0 flex-1 flex-col overflow-hidden rounded-[4px] border border-slate-300 bg-white px-2.5 py-2"
      >
        <div className="flex flex-col gap-1 xl:flex-row xl:items-end xl:justify-between">
          <div className="min-w-0 space-y-0.5 pr-2">
            <h2
              id="wsp-request-leave-title"
              className="text-[13px] font-semibold leading-4 text-slate-900"
            >
              Request Leave
            </h2>
            <p
              id="wsp-request-leave-desc"
              className="text-[9px] leading-3 text-slate-500"
            >
              Create and review leave requests
            </p>
          </div>

          <LeaveBalSum
            availableDays={availableDays}
            afterRequestDays={afterRequestDays}
          />
        </div>

        {errorMsg ? (
          <div className="mt-1 rounded-[3px] border border-rose-200 bg-rose-50 px-2 py-1 text-[10px] leading-4 text-rose-700">
            {errorMsg}
          </div>
        ) : null}

        <div className="mt-1 border-t border-slate-200 pt-1">
          <LeaveFiltBar
            value={draftFilts}
            statusOpts={statusOpts}
            onChange={setDraftFilts}
            onSearch={() => setActiveFilts(draftFilts)}
            onCreate={openCreateDialog}
          />
        </div>

        <div className="mt-1 min-h-0 flex-1">
          <LeaveReqTable
            rows={filteredRequests}
            onEdit={openEditDialog}
            onView={openViewDialog}
            onCancel={setCancelTarget}
            onApprove={setApproveTarget}
            onReject={(rec) => {
              setRejectReason("");
              setRejectTarget(rec);
            }}
            onOpen={openRowDialog}
          />
        </div>
      </section>

      <LeaveReqModal
        open={dialogState.open}
        mode={dialogState.mode}
        applicant={applicant}
        availableDays={availableDays}
        organizations={organizations}
        emps={emps}
        leaveTypeOpts={leaveTypeOpts}
        deductionTypeOpts={deductionTypeOpts}
        leaveUnitOpts={leaveUnitOpts}
        initialValue={dialogState.value}
        onClose={closeDialog}
        onSave={handleSave}
      />

      <LeaveConfirmDialog
        open={cancelTarget !== null}
        title="Cancel Leave Request"
        desc={
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
        desc={
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
        desc={
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

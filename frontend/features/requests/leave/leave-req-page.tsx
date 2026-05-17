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
  type LeaveCalcResponse,
  type LeaveCtxResponse,
  type LeaveListResponse,
  type LeaveReqResponse,
  type LeaveSaveRequest,
} from "@/lib/api/leave-contract";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getLeavePageMsgs, leaveTypeLabel } from "@/lib/i18n/leave-cnt";
import { toast } from "@/components/ui/toast";

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
      canDelete: boolean;
    }
  | {
      open: true;
      mode: "create" | "edit" | "view";
      value: LeaveReqFormValue;
      canDelete: boolean;
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

const STATUS_CD_ALIAS: Record<string, LeaveStatus> = {
  DRF: "Draft",
  REQ: "Requested",
  WAT: "Requested",
  APR: "Approved",
  REJ: "Rejected",
  CAN: "Cancelled",
  ACN: "Admin Cancelled",
};

function normalizeStatus(status: string): LeaveStatus {
  const allowed: readonly LeaveStatus[] = [
    "Draft",
    "Requested",
    "Approved",
    "Rejected",
    "Cancelled",
    "Admin Cancelled",
  ];
  if (allowed.includes(status as LeaveStatus)) return status as LeaveStatus;
  const aliased = STATUS_CD_ALIAS[status];
  if (aliased) return aliased;
  return "Draft";
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
    canDelete: response.canDelete,
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
  locale,
  onLoadingChange,
  onUnauthorized,
}: LeaveReqPageProps) {
  const L = getLeavePageMsgs(locale);
  const [context, setContext] = useState<LeaveCtxResponse | null>(null);
  const [requests, setRequests] = useState<LeaveReqRec[]>([]);
  const [draftFilts, setDraftFilts] = useState(DEFAULT_LEAVE_FILT_VALUE);
  const [activeFilts, setActiveFilts] = useState(DEFAULT_LEAVE_FILT_VALUE);
  const [dialogState, setDialogState] = useState<LeaveDialogState>({
    open: false,
    mode: "create",
    value: createBlankLeaveReqFormValue(),
    canDelete: false,
  });
  const [cancelTarget, setCancelTarget] = useState<LeaveReqRec | null>(null);
  const [approveTarget, setApproveTarget] = useState<LeaveReqRec | null>(null);
  const [rejectTarget, setRejectTarget] = useState<LeaveReqRec | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [deleteTarget, setDeleteTarget] = useState<LeaveReqRec | null>(null);
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
  const previousYearDays = toNumber(context?.balance.previousYearDays);
  const leaveTypeOpts = toTypedOpts<LeaveType>(context?.leaveTypeOpts, LEAVE_TYPE_OPTS);
  const deductionTypeOpts = toTypedOpts<DeductionType>(
    context?.deductionTypeOpts,
    DEDUCTION_TYPE_OPTS
  );
  const leaveUnitOpts = toTypedOpts<LeaveUnit>(context?.leaveUnitOpts, LEAVE_UNIT_OPTS);
  const statusOpts = toTypedOpts<LeaveFiltStatus>(
    context?.statusOpts
      ? Array.from(new Set(context.statusOpts.map(normalizeFiltStatus)))
      : undefined,
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
      canDelete: false,
    });
  };

  const openCreateDialog = () => {
    setDialogState({
      open: true,
      mode: "create",
      value: createBlankLeaveReqFormValue(),
      canDelete: false,
    });
  };

  const openEditDialog = (rec: LeaveReqRec) => {
    setDialogState({
      open: true,
      mode: "edit",
      value: toLeaveReqFormValue(rec),
      canDelete: !!rec.canDelete,
    });
  };

  const openViewDialog = (rec: LeaveReqRec) => {
    setDialogState({
      open: true,
      mode: "view",
      value: toLeaveReqFormValue(rec),
      canDelete: !!rec.canDelete,
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
      const saved = await runWithLoading(() =>
        requestJson<LeaveReqResponse>(
          nextValue.id ? `${LEAVE_API_PATH}/${nextValue.id}` : LEAVE_API_PATH,
          {
            method: nextValue.id ? "PUT" : "POST",
            body,
          }
        )
      );

      if (nextStatus === "Draft") {
        // 임시저장: 모달 유지 + toast 알림. 저장된 id 등 최신 값으로 모달 폼 동기화
        const savedRec = toLeaveReqRec(saved);
        setDialogState({
          open: true,
          mode: "edit",
          value: toLeaveReqFormValue(savedRec),
          canDelete: !!savedRec.canDelete,
        });
        toast.success(L.toastDraftSaved);
        await refreshAfterMutation();
      } else {
        closeDialog();
        await refreshAfterMutation();
      }
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
          method: "POST",
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

  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;

    try {
      await runWithLoading(() =>
        requestJson<void>(`${LEAVE_API_PATH}/${deleteTarget.id}`, {
          method: "DELETE",
        })
      );
      setDeleteTarget(null);
      await refreshAfterMutation();
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : "Failed to delete leave request.");
    }
  };

  const handleCalculate = useCallback(
    async (params: {
      leaveId: string | null;
      leaveType: string;
      deductionType: string;
      leaveUnit: string;
      startDate: string;
      endDate: string;
    }): Promise<LeaveCalcResponse | null> => {
      try {
        return await requestJson<LeaveCalcResponse>(
          `${LEAVE_API_PATH}/calculate`,
          {
            method: "POST",
            body: JSON.stringify({
              leaveId: params.leaveId,
              leaveType: params.leaveType,
              deductionType: params.deductionType,
              leaveUnit: params.leaveUnit,
              startDate: params.startDate,
              endDate: params.endDate,
            }),
          }
        );
      } catch {
        return null;
      }
    },
    [requestJson]
  );

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
              {L.pageTitle}
            </h2>
            <p
              id="wsp-request-leave-desc"
              className="text-[9px] leading-3 text-slate-500"
            >
              {L.pageDesc}
            </p>
          </div>

          <LeaveBalSum
            availableDays={availableDays}
            afterRequestDays={afterRequestDays}
            previousYearDays={previousYearDays}
            locale={locale}
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
            locale={locale}
            statusOpts={statusOpts}
            onChange={setDraftFilts}
            onSearch={() => setActiveFilts(draftFilts)}
            onCreate={openCreateDialog}
          />
        </div>

        <div className="mt-1 min-h-0 flex-1">
          <LeaveReqTable
            rows={filteredRequests}
            locale={locale}
            onEdit={openEditDialog}
            onView={openViewDialog}
            onCancel={setCancelTarget}
            onDelete={setDeleteTarget}
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
        locale={locale}
        applicant={applicant}
        availableDays={availableDays}
        organizations={organizations}
        emps={emps}
        leaveTypeOpts={leaveTypeOpts}
        deductionTypeOpts={deductionTypeOpts}
        leaveUnitOpts={leaveUnitOpts}
        initialValue={dialogState.value}
        canDelete={dialogState.canDelete}
        onClose={closeDialog}
        onSave={handleSave}
        onCalculate={handleCalculate}
        onDelete={async (leaveId) => {
          try {
            await runWithLoading(() =>
              requestJson<void>(`${LEAVE_API_PATH}/${leaveId}`, {
                method: "DELETE",
              })
            );
            closeDialog();
            await refreshAfterMutation();
          } catch (error) {
            setErrorMsg(error instanceof Error ? error.message : "Failed to delete leave request.");
          }
        }}
      />

      <LeaveConfirmDialog
        open={cancelTarget !== null}
        title={L.confirmCancelTitle}
        desc={
          cancelTarget
            ? `${leaveTypeLabel(locale, cancelTarget.leaveType)} — ${cancelTarget.startDate}${cancelTarget.endDate !== cancelTarget.startDate ? ` ~ ${cancelTarget.endDate}` : ""}`
            : ""
        }
        confirmLabel={L.confirmCancelBtn}
        cancelLabel={L.confirmCloseBtn}
        onClose={() => setCancelTarget(null)}
        onConfirm={handleConfirmCancelRequest}
      />

      <LeaveConfirmDialog
        open={approveTarget !== null}
        title={L.confirmApproveTitle}
        desc={
          approveTarget
            ? `${leaveTypeLabel(locale, approveTarget.leaveType)} — ${formatLeaveDays(approveTarget.days)} ${L.thDays}`
            : ""
        }
        confirmLabel={L.confirmApproveBtn}
        cancelLabel={L.confirmCloseBtn}
        onClose={() => setApproveTarget(null)}
        onConfirm={handleConfirmApprove}
      />

      <LeaveConfirmDialog
        open={rejectTarget !== null}
        title={L.confirmRejectTitle}
        desc={
          rejectTarget
            ? `${leaveTypeLabel(locale, rejectTarget.leaveType)} — ${formatLeaveDays(rejectTarget.days)} ${L.thDays}`
            : ""
        }
        confirmLabel={L.confirmRejectBtn}
        cancelLabel={L.confirmCloseBtn}
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
          placeholder={L.rejectReasonPlaceholder}
        />
      </LeaveConfirmDialog>

      <LeaveConfirmDialog
        open={deleteTarget !== null}
        title={L.confirmDeleteTitle}
        desc={L.confirmDeleteDesc}
        confirmLabel={L.confirmDeleteBtn}
        cancelLabel={L.confirmCloseBtn}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleConfirmDelete}
      />

      {busy ? (
        <div className="pointer-events-none fixed bottom-3 right-3 z-[95] rounded-[3px] border border-slate-300 bg-white px-2 py-1 text-[10px] text-slate-600 shadow-sm">
          Processing...
        </div>
      ) : null}
    </div>
  );
}

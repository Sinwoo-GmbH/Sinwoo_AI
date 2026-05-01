"use client";

import { useMemo, useState } from "react";

import { LeaveBalanceSummary } from "@/components/features/attendance/request-leave/leave-balance-summary";
import { LeaveConfirmDialog } from "@/components/features/attendance/request-leave/leave-confirm-dialog";
import { LeaveFilterBar } from "@/components/features/attendance/request-leave/leave-filter-bar";
import type {
  LeaveFilterValue,
  LeaveRequestFormValue,
  LeaveRequestRecord,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import {
  createDefaultLeaveRequestFormValue,
  DEFAULT_LEAVE_FILTER_VALUE,
  LEAVE_AFTER_REQUEST_PREVIEW,
  LEAVE_AVAILABLE_DAYS,
  LEAVE_STATUS_OPTIONS,
  MOCK_LEAVE_APPLICANT,
  MOCK_LEAVE_REQUESTS,
  renumberLeaveRequests,
  toLeaveRequestFormValue,
  toLeaveRequestRecord,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import { LeaveRequestModal } from "@/components/features/attendance/request-leave/leave-request-modal";
import { LeaveRequestTable } from "@/components/features/attendance/request-leave/leave-request-table";
import type { LoginLocale } from "@/lib/i18n/login-content";

type RequestLeavePageProps = {
  locale: LoginLocale;
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

function matchesDateRange(record: LeaveRequestRecord, filters: LeaveFilterValue) {
  if (filters.startDateFrom && record.startDate < filters.startDateFrom) {
    return false;
  }

  if (filters.startDateTo && record.startDate > filters.startDateTo) {
    return false;
  }

  return true;
}

export function RequestLeavePage({ locale: _locale }: RequestLeavePageProps) {
  const [requests, setRequests] = useState(MOCK_LEAVE_REQUESTS);
  const [draftFilters, setDraftFilters] = useState(DEFAULT_LEAVE_FILTER_VALUE);
  const [activeFilters, setActiveFilters] = useState(DEFAULT_LEAVE_FILTER_VALUE);
  const [dialogState, setDialogState] = useState<LeaveDialogState>({
    open: false,
    mode: "create",
    value: createDefaultLeaveRequestFormValue(),
  });
  const [cancelTarget, setCancelTarget] = useState<LeaveRequestRecord | null>(null);

  const filteredRequests = useMemo(
    () =>
      requests.filter((record) => {
        const matchesStatus =
          activeFilters.status === "All" || record.status === activeFilters.status;
        return matchesStatus && matchesDateRange(record, activeFilters);
      }),
    [activeFilters, requests]
  );

  const closeDialog = () => {
    setDialogState({
      open: false,
      mode: "create",
      value: createDefaultLeaveRequestFormValue(),
    });
  };

  const openCreateDialog = () => {
    setDialogState({
      open: true,
      mode: "create",
      value: createDefaultLeaveRequestFormValue(),
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
    if (record.status === "Draft") {
      openEditDialog(record);
      return;
    }

    openViewDialog(record);
  };

  const handleSave = (
    nextValue: LeaveRequestFormValue,
    nextStatus: "Draft" | "Requested"
  ) => {
    setRequests((current) => {
      const existingRecord = nextValue.id
        ? current.find((record) => record.id === nextValue.id)
        : null;
      const createdAt = existingRecord?.createdAt ?? "2026-04-19 16:45";
      const nextRecord = toLeaveRequestRecord(nextValue, 1, nextStatus, createdAt);
      const remainingRecords = current.filter(
        (record) => record.id !== nextRecord.id
      );

      return renumberLeaveRequests([nextRecord, ...remainingRecords]);
    });

    closeDialog();
  };

  const handleCancelRequest = (record: LeaveRequestRecord) => {
    setCancelTarget(record);
  };

  const handleConfirmCancelRequest = () => {
    if (!cancelTarget) {
      return;
    }

    setRequests((current) =>
      current.map((item) =>
        item.id === cancelTarget.id
          ? {
              ...item,
              status: "Cancelled" as const,
              approverStatus: "No Approver" as const,
            }
          : item
      )
    );

    setCancelTarget(null);
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
            availableDays={LEAVE_AVAILABLE_DAYS}
            afterRequestDays={LEAVE_AFTER_REQUEST_PREVIEW}
          />
        </div>

        <div className="mt-1 border-t border-slate-200 pt-1">
          <LeaveFilterBar
            value={draftFilters}
            statusOptions={LEAVE_STATUS_OPTIONS}
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
            onCancel={handleCancelRequest}
            onOpen={openRowDialog}
          />
        </div>
      </section>

      <LeaveRequestModal
        open={dialogState.open}
        mode={dialogState.mode}
        applicant={MOCK_LEAVE_APPLICANT}
        availableDays={LEAVE_AVAILABLE_DAYS}
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
    </div>
  );
}

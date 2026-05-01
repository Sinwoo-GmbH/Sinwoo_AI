"use client";

import type { ReactNode } from "react";
import { useEffect, useId, useMemo, useState } from "react";

import { ApprovalLineEditor } from "@/components/features/attendance/request-leave/approval-line-editor";
import { EmployeePickerModal } from "@/components/features/attendance/request-leave/employee-picker-modal";
import type {
  ApprovalStep,
  DeductionType,
  LeaveApplicantProfile,
  LeaveRequestFormValue,
  LeaveType,
  LeaveUnit,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import {
  calculateAfterRequest,
  calculateLeaveDays,
  cloneApprovalSteps,
  createEmptyApprovalStep,
  DEDUCTION_TYPE_OPTIONS,
  formatApprovalStepLabel,
  formatLeaveDays,
  LEAVE_TYPE_OPTIONS,
  LEAVE_UNIT_OPTIONS,
  MOCK_LEAVE_EMPLOYEES,
  MOCK_LEAVE_ORGANIZATIONS,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { Paperclip, X } from "lucide-react";

type LeaveRequestModalMode = "create" | "edit" | "view";

type LeaveRequestModalProps = {
  open: boolean;
  mode: LeaveRequestModalMode;
  applicant: LeaveApplicantProfile;
  availableDays: number;
  initialValue: LeaveRequestFormValue;
  onClose: () => void;
  onSave: (
    value: LeaveRequestFormValue,
    nextStatus: "Draft" | "Requested"
  ) => void;
};

type SectionPanelProps = {
  title: string;
  children: ReactNode;
  className?: string;
};

type ReadOnlyFieldProps = {
  label: string;
  value: string;
};

type ChoiceChipGroupProps<T extends string> = {
  label: string;
  value: T;
  options: readonly T[];
  disabled?: boolean;
  onChange: (nextValue: T) => void;
};

type PickerState =
  | {
      kind: "approver";
      stepId: string;
    }
  | {
      kind: "cc";
    }
  | null;

const inputClassName =
  "h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[10px] leading-4 text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5] disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500";

function ReadOnlyField({ label, value }: ReadOnlyFieldProps) {
  return (
    <div className="space-y-1">
      <p className="text-[9px] leading-3 text-slate-500">{label}</p>
      <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5 text-[10px] leading-4 text-slate-700">
        {value}
      </div>
    </div>
  );
}

function SectionPanel({ title, children, className }: SectionPanelProps) {
  return (
    <Card className={cn("rounded-[3px] border-slate-300 bg-white shadow-none", className)}>
      <CardHeader className="border-b border-slate-200 px-2.5 py-1.5">
        <CardTitle className="text-[10px] font-semibold leading-4 text-slate-700">
          {title}
        </CardTitle>
      </CardHeader>
      <CardContent className="px-2.5 pb-2.5 pt-2">{children}</CardContent>
    </Card>
  );
}

function ChoiceChipGroup<T extends string>({
  label,
  value,
  options,
  disabled = false,
  onChange,
}: ChoiceChipGroupProps<T>) {
  return (
    <div className="space-y-1">
      <p className="text-[9px] leading-3 text-slate-500">{label}</p>
      <div className="flex flex-wrap gap-1">
        {options.map((option) => (
          <button
            key={option}
            type="button"
            disabled={disabled}
            onClick={() => onChange(option)}
            className={cn(
              "min-h-[26px] rounded-[3px] border px-2 py-1 text-[10px] leading-4 transition-colors",
              value === option
                ? "border-[#31588f] bg-[#31588f] text-white"
                : "border-slate-300 bg-white text-slate-600 hover:bg-slate-50",
              disabled ? "cursor-not-allowed opacity-70" : ""
            )}
          >
            {option}
          </button>
        ))}
      </div>
    </div>
  );
}

export function LeaveRequestModal({
  open,
  mode,
  applicant,
  availableDays,
  initialValue,
  onClose,
  onSave,
}: LeaveRequestModalProps) {
  const attachmentInputId = useId();
  const [formValue, setFormValue] = useState<LeaveRequestFormValue>(initialValue);
  const [pickerState, setPickerState] = useState<PickerState>(null);
  const readOnly = mode === "view";

  useEffect(() => {
    if (!open) {
      return;
    }

    setFormValue(initialValue);
  }, [initialValue, open]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        if (pickerState) {
          setPickerState(null);
          return;
        }

        onClose();
      }
    };

    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [onClose, open, pickerState]);

  const days = useMemo(
    () => calculateLeaveDays(formValue.startDate, formValue.endDate, formValue.leaveUnit),
    [formValue.endDate, formValue.leaveUnit, formValue.startDate]
  );

  const afterRequestDays = useMemo(
    () => calculateAfterRequest(availableDays, formValue.deductionType, days),
    [availableDays, days, formValue.deductionType]
  );

  const modalTitle =
    mode === "create"
      ? "Create Leave Request"
      : mode === "edit"
        ? "Edit Leave Request"
        : "Leave Request Details";

  const updateField = <K extends keyof LeaveRequestFormValue>(
    key: K,
    nextValue: LeaveRequestFormValue[K]
  ) => {
    setFormValue((current) => {
      const nextState = { ...current, [key]: nextValue };

      if (key === "startDate" && typeof nextValue === "string" && current.endDate < nextValue) {
        nextState.endDate = nextValue;
      }

      if (key === "endDate" && typeof nextValue === "string" && current.startDate > nextValue) {
        nextState.startDate = nextValue;
      }

      return nextState;
    });
  };

  const updateApprovalSteps = (
    updater: (current: ApprovalStep[]) => ApprovalStep[]
  ) => {
    setFormValue((current) => ({
      ...current,
      approvalSteps: cloneApprovalSteps(updater(current.approvalSteps)),
    }));
  };

  const handleAddApprovalStage = () => {
    updateApprovalSteps((current) => [
      ...current,
      createEmptyApprovalStep(current.length + 1),
    ]);
  };

  const handleRemoveApprovalStage = (stepId: string) => {
    updateApprovalSteps((current) => {
      const nextSteps = current.filter((step) => step.id !== stepId);
      return nextSteps.length ? nextSteps : [createEmptyApprovalStep(1)];
    });
  };

  const handleRemoveStageApprover = (stepId: string, userId: string) => {
    updateApprovalSteps((current) =>
      current.map((step) =>
        step.id === stepId
          ? {
              ...step,
              users: step.users.filter((user) => user.id !== userId),
            }
          : step
      )
    );
  };

  const handleRemoveCc = (userId: string) => {
    updateField(
      "ccs",
      formValue.ccs.filter((user) => user.id !== userId)
    );
  };

  const currentPickerStep =
    pickerState?.kind === "approver"
      ? formValue.approvalSteps.find((step) => step.id === pickerState.stepId) ?? null
      : null;

  if (!open) {
    return null;
  }

  return (
    <>
      <div className="fixed inset-0 z-[70] flex items-center justify-center bg-slate-950/35 p-4">
        <div className="absolute inset-0" aria-hidden="true" onClick={onClose} />

        <Card className="relative z-[71] flex h-[min(84vh,840px)] w-full max-w-[1040px] flex-col overflow-hidden rounded-[4px] border-slate-300 bg-[#f7f8fa] shadow-[0_8px_18px_rgba(15,23,42,0.12)]">
          <CardHeader className="flex flex-row items-center justify-between border-b border-slate-300 px-3 py-2">
            <CardTitle className="text-[12px] font-semibold leading-4 text-slate-900">
              {modalTitle}
            </CardTitle>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="h-6 w-6 rounded-[3px] p-0 text-slate-500 hover:bg-slate-200"
            >
              <X className="h-3 w-3" />
            </Button>
          </CardHeader>

          <CardContent className="min-h-0 flex-1 overflow-y-auto px-3 py-2.5">
            <div className="space-y-2">
              <div className="grid gap-2 xl:grid-cols-[1.2fr_0.8fr]">
                <SectionPanel title="Applicant Info">
                  <div className="grid gap-1.5 md:grid-cols-3">
                    <ReadOnlyField label="Name" value={applicant.name} />
                    <ReadOnlyField label="Department" value={applicant.department} />
                    <ReadOnlyField label="Position" value={applicant.position} />
                  </div>
                </SectionPanel>

                <SectionPanel title="Leave Summary">
                  <div className="grid gap-1.5 sm:grid-cols-2">
                    <div className="rounded-[3px] border border-slate-300 bg-[#f8f9fb] px-2 py-1.5">
                      <p className="text-[9px] leading-3 text-slate-500">Available</p>
                      <p className="mt-0.5 text-[12px] font-semibold leading-4 text-slate-900">
                        {availableDays.toFixed(1)}
                      </p>
                    </div>
                    <div className="rounded-[3px] border border-blue-200 bg-[#eef3fb] px-2 py-1.5">
                      <p className="text-[9px] leading-3 text-slate-500">After Request</p>
                      <p className="mt-0.5 text-[12px] font-semibold leading-4 text-slate-900">
                        {afterRequestDays.toFixed(1)}
                      </p>
                    </div>
                  </div>
                </SectionPanel>
              </div>

              <SectionPanel title="Leave Info">
                <div className="grid gap-1.5 xl:grid-cols-12">
                  <div className="space-y-1 xl:col-span-4">
                    <p className="text-[9px] leading-3 text-slate-500">Leave Type</p>
                    <select
                      value={formValue.leaveType}
                      disabled={readOnly}
                      onChange={(event) =>
                        updateField("leaveType", event.target.value as LeaveType)
                      }
                      className={inputClassName}
                    >
                      {LEAVE_TYPE_OPTIONS.map((option) => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="xl:col-span-4">
                    <ChoiceChipGroup<DeductionType>
                      label="Deduction Type"
                      value={formValue.deductionType}
                      options={DEDUCTION_TYPE_OPTIONS}
                      disabled={readOnly}
                      onChange={(nextValue) => updateField("deductionType", nextValue)}
                    />
                  </div>

                  <div className="xl:col-span-4">
                    <ChoiceChipGroup<LeaveUnit>
                      label="Leave Unit"
                      value={formValue.leaveUnit}
                      options={LEAVE_UNIT_OPTIONS}
                      disabled={readOnly}
                      onChange={(nextValue) => updateField("leaveUnit", nextValue)}
                    />
                  </div>

                  <div className="space-y-1 xl:col-span-3">
                    <p className="text-[9px] leading-3 text-slate-500">Start Date</p>
                    <input
                      type="date"
                      value={formValue.startDate}
                      disabled={readOnly}
                      onChange={(event) => updateField("startDate", event.target.value)}
                      className={inputClassName}
                    />
                  </div>

                  <div className="space-y-1 xl:col-span-3">
                    <p className="text-[9px] leading-3 text-slate-500">End Date</p>
                    <input
                      type="date"
                      value={formValue.endDate}
                      disabled={readOnly}
                      onChange={(event) => updateField("endDate", event.target.value)}
                      className={inputClassName}
                    />
                  </div>

                  <div className="xl:col-span-2">
                    <ReadOnlyField label="Days" value={formatLeaveDays(days)} />
                  </div>

                  <div className="space-y-1 xl:col-span-4">
                    <p className="text-[9px] leading-3 text-slate-500">Attachment</p>
                    <div className="flex h-7 items-center justify-between gap-2 rounded-[3px] border border-dashed border-slate-300 bg-white px-2">
                      <div className="flex min-w-0 items-center gap-1.5 text-[10px] leading-4 text-slate-600">
                        <Paperclip className="h-3 w-3 shrink-0 text-slate-400" />
                        <span className="truncate">
                          {formValue.attachmentName ?? "No attachment selected"}
                        </span>
                      </div>
                      {!readOnly ? (
                        <div className="shrink-0">
                          <input
                            id={attachmentInputId}
                            type="file"
                            className="hidden"
                            onChange={(event) =>
                              updateField(
                                "attachmentName",
                                event.target.files?.[0]?.name ?? null
                              )
                            }
                          />
                          <label htmlFor={attachmentInputId}>
                            <span className="inline-flex h-[22px] cursor-pointer items-center justify-center rounded-[3px] border border-slate-300 bg-white px-2 text-[10px] font-medium text-slate-700 transition-colors hover:bg-slate-50">
                              Upload
                            </span>
                          </label>
                        </div>
                      ) : null}
                    </div>
                  </div>
                </div>
              </SectionPanel>

              <SectionPanel title="Approval Line">
                <ApprovalLineEditor
                  approvalSteps={formValue.approvalSteps}
                  ccs={formValue.ccs}
                  readOnly={readOnly}
                  onAddApprovalStage={handleAddApprovalStage}
                  onAddStageApprovers={(stepId) =>
                    setPickerState({ kind: "approver", stepId })
                  }
                  onRemoveApprovalStage={handleRemoveApprovalStage}
                  onRemoveStageApprover={handleRemoveStageApprover}
                  onAddCc={() => setPickerState({ kind: "cc" })}
                  onRemoveCc={handleRemoveCc}
                />
              </SectionPanel>
            </div>
          </CardContent>

          <div className="flex items-center justify-end gap-2 border-t border-slate-300 px-3 py-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onClose}
              className="h-7 rounded-[3px] border-slate-300 px-2.5 text-[10px] font-medium"
            >
              {readOnly ? "Close" : "Cancel"}
            </Button>
            {!readOnly ? (
              <>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => onSave(formValue, "Draft")}
                  className="h-7 rounded-[3px] border-slate-300 px-2.5 text-[10px] font-medium text-slate-700"
                >
                  Save Draft
                </Button>
                <Button
                  type="button"
                  size="sm"
                  onClick={() => onSave(formValue, "Requested")}
                  className="h-7 rounded-[3px] bg-[#2f5b96] px-2.5 text-[10px] font-medium text-white hover:bg-[#274d7e]"
                >
                  Submit Request
                </Button>
              </>
            ) : null}
          </div>
        </Card>
      </div>

      <EmployeePickerModal
        open={pickerState !== null}
        mode={pickerState?.kind === "cc" ? "cc" : "approver"}
        title={
          pickerState?.kind === "approver"
            ? `Select ${formatApprovalStepLabel(currentPickerStep?.order ?? 1)} Approvers`
            : "Select C.C."
        }
        organizations={MOCK_LEAVE_ORGANIZATIONS}
        employees={MOCK_LEAVE_EMPLOYEES}
        selectedUsers={
          pickerState?.kind === "cc"
            ? formValue.ccs
            : currentPickerStep?.users ?? []
        }
        onClose={() => setPickerState(null)}
        onApply={(users) => {
          if (pickerState?.kind === "cc") {
            updateField("ccs", users);
          } else if (pickerState?.kind === "approver") {
            updateApprovalSteps((current) =>
              current.map((step) =>
                step.id === pickerState.stepId
                  ? { ...step, users: users.map((user) => ({ ...user })) }
                  : step
              )
            );
          }
          setPickerState(null);
        }}
      />
    </>
  );
}

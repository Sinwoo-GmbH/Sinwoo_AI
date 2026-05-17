"use client";

import type { ReactNode } from "react";
import { useCallback, useEffect, useId, useMemo, useRef, useState } from "react";

import { AprvLineEditor } from "@/features/requests/leave/aprv-line-editor";
import { EmpPickerModal } from "@/features/requests/leave/emp-picker-modal";
import type {
  AprvStep,
  DeductionType,
  LeaveApplProfile,
  LeaveOrgNode,
  LeavePart,
  LeaveReqFormValue,
  LeaveType,
  LeaveUnit,
} from "@/features/requests/leave/leave-mock-data";
import {
  allowedLeaveUnits,
  autoDeductionType,
  calculateAfterRequest,
  calculateLeaveDays,
  cloneAprvSteps,
  createEmptyAprvStep,
  DEDUCTION_TYPE_OPTS,
  formatAprvStepLabel,
  formatLeaveDays,
  LEAVE_TYPE_OPTS,
  LEAVE_UNIT_OPTS,
  MOCK_LEAVE_EMPS,
  MOCK_LEAVE_ORGS,
} from "@/features/requests/leave/leave-mock-data";
import type { LeaveCalcResponse } from "@/lib/api/leave-contract";
import { Button } from "@/components/ui/button";
import { Card, CardCnt, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "@/components/ui/toast";
import { cn } from "@/lib/utils";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import {
  deductionTypeLabel,
  getLeavePageMsgs,
  leaveTypeLabel,
  leaveUnitLabel,
} from "@/lib/i18n/leave-cnt";
import { Paperclip, X } from "lucide-react";

/* ── Validation ── */
type FormErrors = {
  leaveType?: string;
  deductionType?: string;
  leaveUnit?: string;
  startDate?: string;
  endDate?: string;
  reason?: string;
};

type LeaveReqModalMode = "create" | "edit" | "view";

type LeaveReqModalProps = {
  open: boolean;
  mode: LeaveReqModalMode;
  locale: LoginLocale;
  applicant: LeaveApplProfile;
  availableDays: number;
  organizations?: LeaveOrgNode[];
  emps?: LeavePart[];
  leaveTypeOpts?: readonly LeaveType[];
  deductionTypeOpts?: readonly DeductionType[];
  leaveUnitOpts?: readonly LeaveUnit[];
  initialValue: LeaveReqFormValue;
  onClose: () => void;
  onSave: (
    value: LeaveReqFormValue,
    nextStatus: "Draft" | "Requested"
  ) => void;
  onCalculate?: (params: {
    leaveId: string | null;
    leaveType: string;
    deductionType: string;
    leaveUnit: string;
    startDate: string;
    endDate: string;
  }) => Promise<LeaveCalcResponse | null>;
  /** Edit 모드에서 호출 — 저장된 휴가 신청을 실제로 삭제 */
  onDelete?: (leaveId: string) => Promise<void> | void;
  /** 백엔드 canDelete 플래그 — true일 때만 모달 내 Delete 버튼 노출 */
  canDelete?: boolean | null;
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
  displayLabel?: (opt: T) => string;
  disabled?: boolean;
  required?: boolean;
  error?: string;
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

const inputErrorClassName =
  "border-red-400 focus:border-red-400 focus:ring-red-200";

function FieldLabel({ label, required = false }: { label: string; required?: boolean }) {
  return (
    <p className="text-[9px] leading-3 text-slate-500">
      {label}
      {required && <span className="ml-0.5 text-red-500">*</span>}
    </p>
  );
}

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="mt-0.5 text-[9px] leading-3 text-red-500">{message}</p>;
}

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
      <CardCnt className="px-2.5 pb-2.5 pt-2">{children}</CardCnt>
    </Card>
  );
}

function ChoiceChipGroup<T extends string>({
  label,
  value,
  options,
  displayLabel,
  disabled = false,
  required = false,
  error,
  onChange,
}: ChoiceChipGroupProps<T>) {
  return (
    <div className="space-y-1">
      <FieldLabel label={label} required={required} />
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
              disabled ? "cursor-not-allowed opacity-70" : "",
              error && !value ? "border-red-400" : ""
            )}
          >
            {displayLabel ? displayLabel(option) : option}
          </button>
        ))}
      </div>
      <FieldError message={error} />
    </div>
  );
}

export function LeaveReqModal({
  open,
  mode,
  locale,
  applicant,
  availableDays,
  organizations = MOCK_LEAVE_ORGS,
  emps = MOCK_LEAVE_EMPS,
  leaveTypeOpts = LEAVE_TYPE_OPTS,
  deductionTypeOpts = DEDUCTION_TYPE_OPTS,
  leaveUnitOpts = LEAVE_UNIT_OPTS,
  initialValue,
  onClose,
  onSave,
  onCalculate,
  onDelete,
  canDelete = false,
}: LeaveReqModalProps) {
  const L = getLeavePageMsgs(locale);
  const attachmentInputId = useId();
  const [formValue, setFormValue] = useState<LeaveReqFormValue>(initialValue);
  const [pickerState, setPickerState] = useState<PickerState>(null);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);
  const [noApproverConfirmOpen, setNoApproverConfirmOpen] = useState(false);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [serverDays, setServerDays] = useState<number | null>(null);
  const [serverAfterDays, setServerAfterDays] = useState<number | null>(null);
  const calcTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const readOnly = mode === "view";

  /* ── Draggable ── */
  const [dragPos, setDragPos] = useState({ x: 0, y: 0 });
  const draggingRef = useRef(false);
  const dragStartRef = useRef({ x: 0, y: 0, posX: 0, posY: 0 });

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    draggingRef.current = true;
    dragStartRef.current = {
      x: e.clientX,
      y: e.clientY,
      posX: dragPos.x,
      posY: dragPos.y,
    };
    e.preventDefault();
  }, [dragPos]);

  useEffect(() => {
    if (!open) return;

    const handleMouseMove = (e: MouseEvent) => {
      if (!draggingRef.current) return;
      const dx = e.clientX - dragStartRef.current.x;
      const dy = e.clientY - dragStartRef.current.y;
      setDragPos({
        x: dragStartRef.current.posX + dx,
        y: dragStartRef.current.posY + dy,
      });
    };

    const handleMouseUp = () => {
      draggingRef.current = false;
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [open]);

  const validate = useCallback(
    (values: LeaveReqFormValue): FormErrors => {
      const errs: FormErrors = {};

      if (!values.leaveType) errs.leaveType = L.valLeaveType;
      if (!values.deductionType) errs.deductionType = L.valDeductionType;
      if (!values.leaveUnit) errs.leaveUnit = L.valLeaveUnit;
      if (!values.startDate) errs.startDate = L.valStartDate;
      if (!values.endDate) errs.endDate = L.valEndDate;

      if (values.startDate && values.endDate && values.startDate > values.endDate) {
        errs.endDate = L.valEndDateAfterStart;
      }

      return errs;
    },
    [L]
  );

  useEffect(() => {
    if (!open) {
      return;
    }

    // 휴가단위 기본값 — 백엔드 옵션에 현재 값이 없으면 종일(Full Day/FD)에 해당하는 첫 옵션으로 자동 보정
    let nextValue = initialValue;
    if (leaveUnitOpts.length > 0 && !leaveUnitOpts.includes(initialValue.leaveUnit)) {
      const allowed = allowedLeaveUnits<LeaveUnit>(initialValue.leaveType, leaveUnitOpts);
      const fallback = (allowed[0] ?? leaveUnitOpts[0]) as LeaveUnit;
      nextValue = { ...initialValue, leaveUnit: fallback };
    }

    setFormValue(nextValue);
    setErrors({});
    setSubmitted(false);
    setServerDays(null);
    setServerAfterDays(null);
    setDragPos({ x: 0, y: 0 });
  }, [initialValue, open, leaveUnitOpts]);

  /* ── Backend calculate (debounced) ── */
  useEffect(() => {
    if (!open || !onCalculate) return;
    if (!formValue.startDate || !formValue.endDate) return;
    if (formValue.startDate > formValue.endDate) return;

    if (calcTimerRef.current) clearTimeout(calcTimerRef.current);
    calcTimerRef.current = setTimeout(async () => {
      const result = await onCalculate({
        leaveId: formValue.id,
        leaveType: formValue.leaveType,
        deductionType: formValue.deductionType,
        leaveUnit: formValue.leaveUnit,
        startDate: formValue.startDate,
        endDate: formValue.endDate,
      });
      if (result) {
        setServerDays(result.days);
        setServerAfterDays(result.afterRequestDays);
      }
    }, 400);

    return () => {
      if (calcTimerRef.current) clearTimeout(calcTimerRef.current);
    };
  }, [
    open,
    onCalculate,
    formValue.id,
    formValue.leaveType,
    formValue.deductionType,
    formValue.leaveUnit,
    formValue.startDate,
    formValue.endDate,
  ]);

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

  const localDays = useMemo(
    () => calculateLeaveDays(formValue.startDate, formValue.endDate, formValue.leaveUnit),
    [formValue.endDate, formValue.leaveUnit, formValue.startDate]
  );

  const localAfterRequestDays = useMemo(
    () => calculateAfterRequest(availableDays, formValue.deductionType, localDays),
    [availableDays, localDays, formValue.deductionType]
  );

  /* Prefer server-calculated days (includes holidays); fall back to local (weekends only) */
  const days = serverDays ?? localDays;
  const afterRequestDays = serverAfterDays ?? localAfterRequestDays;

  const modalTitle =
    mode === "create"
      ? L.modalCreate
      : mode === "edit"
        ? L.modalEdit
        : L.modalView;

  const updateField = <K extends keyof LeaveReqFormValue>(
    key: K,
    nextValue: LeaveReqFormValue[K]
  ) => {
    setFormValue((current) => {
      const nextState = { ...current, [key]: nextValue };

      if (key === "startDate" && typeof nextValue === "string" && current.endDate < nextValue) {
        nextState.endDate = nextValue;
      }

      if (key === "endDate" && typeof nextValue === "string" && current.startDate > nextValue) {
        nextState.startDate = nextValue;
      }

      // 휴가유형 변경 시 차감유형 자동 + 휴가단위는 무조건 종일(Full Day/FD)로 리셋
      if (key === "leaveType" && typeof nextValue === "string") {
        const nextLeaveType = nextValue as LeaveType;
        nextState.deductionType = autoDeductionType(nextLeaveType);
        const allowed = allowedLeaveUnits<LeaveUnit>(nextLeaveType, leaveUnitOpts);
        // 항상 첫 허용 옵션(=종일)으로 초기화
        nextState.leaveUnit = (allowed[0] ?? "Full Day") as LeaveUnit;
      }

      return nextState;
    });

    /* Clear field error on change after submit attempt */
    if (submitted && errors[key as keyof FormErrors]) {
      setErrors((prev) => {
        const next = { ...prev };
        delete next[key as keyof FormErrors];
        return next;
      });
    }
  };

  const updateApprovalSteps = (
    updater: (current: AprvStep[]) => AprvStep[]
  ) => {
    setFormValue((current) => ({
      ...current,
      approvalSteps: cloneAprvSteps(updater(current.approvalSteps)),
    }));
  };

  const handleAddApprovalStage = () => {
    updateApprovalSteps((current) => [
      ...current,
      createEmptyAprvStep(current.length + 1),
    ]);
  };

  const handleRemoveApprovalStage = (stepId: string) => {
    updateApprovalSteps((current) => {
      const nextSteps = current.filter((step) => step.id !== stepId);
      return nextSteps.length ? nextSteps : [createEmptyAprvStep(1)];
    });
  };

  const handleRemoveStageApprover = (stepId: string, usrId: string) => {
    updateApprovalSteps((current) =>
      current.map((step) =>
        step.id === stepId
          ? {
              ...step,
              usrs: step.usrs.filter((user) => user.id !== usrId),
            }
          : step
      )
    );
  };

  const handleRemoveCc = (usrId: string) => {
    updateField(
      "ccs",
      formValue.ccs.filter((user) => user.id !== usrId)
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

        <Card
          className="relative z-[71] flex h-[min(84vh,840px)] w-full max-w-[1040px] flex-col overflow-hidden rounded-[4px] border-slate-300 bg-[#f7f8fa] shadow-[0_8px_18px_rgba(15,23,42,0.12)]"
          style={{ transform: `translate(${dragPos.x}px, ${dragPos.y}px)` }}
        >
          <CardHeader
            className="flex flex-row items-center justify-between border-b border-slate-300 px-3 py-2 cursor-move select-none"
            onMouseDown={handleMouseDown}
          >
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

          <CardCnt className="min-h-0 flex-1 overflow-y-auto px-3 py-2.5">
            <div className="space-y-2">
              <div className="grid gap-2 xl:grid-cols-[1.2fr_0.8fr]">
                <SectionPanel title={L.secApplicant}>
                  <div className="grid gap-1.5 md:grid-cols-3">
                    <ReadOnlyField label={L.fldName} value={applicant.name} />
                    <ReadOnlyField label={L.fldDept} value={applicant.dept} />
                    <ReadOnlyField label={L.fldPosition} value={applicant.position} />
                  </div>
                </SectionPanel>

                <SectionPanel title={L.secLeaveSummary}>
                  <div className="grid gap-1.5 sm:grid-cols-2">
                    <div className="rounded-[3px] border border-slate-300 bg-[#f8f9fb] px-2 py-1.5">
                      <p className="text-[9px] leading-3 text-slate-500">{L.available}</p>
                      <p className="mt-0.5 text-[12px] font-semibold leading-4 text-slate-900">
                        {availableDays.toFixed(1)}
                      </p>
                    </div>
                    <div className="rounded-[3px] border border-blue-200 bg-[#eef3fb] px-2 py-1.5">
                      <p className="text-[9px] leading-3 text-slate-500">{L.afterRequest}</p>
                      <p className="mt-0.5 text-[12px] font-semibold leading-4 text-slate-900">
                        {afterRequestDays.toFixed(1)}
                      </p>
                    </div>
                  </div>
                </SectionPanel>
              </div>

              <SectionPanel title={L.secLeaveInfo}>
                <div className="grid gap-1.5 xl:grid-cols-12">
                  <div className="space-y-1 xl:col-span-4">
                    <FieldLabel label={L.fldLeaveType} required />
                    <select
                      value={formValue.leaveType}
                      disabled={readOnly}
                      onChange={(event) =>
                        updateField("leaveType", event.target.value as LeaveType)
                      }
                      className={cn(inputClassName, errors.leaveType && inputErrorClassName)}
                    >
                      {leaveTypeOpts.map((option) => (
                        <option key={option} value={option}>
                          {leaveTypeLabel(locale, option)}
                        </option>
                      ))}
                    </select>
                    <FieldError message={errors.leaveType} />
                  </div>

                  <div className="xl:col-span-4">
                    <ReadOnlyField
                      label={L.fldDeductionType}
                      value={deductionTypeLabel(locale, formValue.deductionType)}
                    />
                  </div>

                  <div className="xl:col-span-4">
                    <ChoiceChipGroup<LeaveUnit>
                      label={L.fldLeaveUnit}
                      value={formValue.leaveUnit}
                      options={allowedLeaveUnits<LeaveUnit>(formValue.leaveType, leaveUnitOpts)}
                      displayLabel={(opt) => leaveUnitLabel(locale, opt)}
                      disabled={readOnly}
                      required
                      error={errors.leaveUnit}
                      onChange={(nextValue) => updateField("leaveUnit", nextValue)}
                    />
                  </div>

                  <div className="space-y-1 xl:col-span-3">
                    <FieldLabel label={L.fldStartDate} required />
                    <input
                      type="date"
                      value={formValue.startDate}
                      max={formValue.endDate || undefined}
                      disabled={readOnly}
                      onChange={(event) => updateField("startDate", event.target.value)}
                      className={cn(inputClassName, errors.startDate && inputErrorClassName)}
                    />
                    <FieldError message={errors.startDate} />
                  </div>

                  <div className="space-y-1 xl:col-span-3">
                    <FieldLabel label={L.fldEndDate} required />
                    <input
                      type="date"
                      value={formValue.endDate}
                      min={formValue.startDate || undefined}
                      disabled={readOnly}
                      onChange={(event) => updateField("endDate", event.target.value)}
                      className={cn(inputClassName, errors.endDate && inputErrorClassName)}
                    />
                    <FieldError message={errors.endDate} />
                  </div>

                  <div className="xl:col-span-2">
                    <ReadOnlyField label={L.fldDays} value={formatLeaveDays(days)} />
                  </div>

                  <div className="space-y-1 xl:col-span-4">
                    <p className="text-[9px] leading-3 text-slate-500">{L.fldAttachment}</p>
                    <div className="flex h-7 items-center justify-between gap-2 rounded-[3px] border border-dashed border-slate-300 bg-white px-2">
                      <div className="flex min-w-0 items-center gap-1.5 text-[10px] leading-4 text-slate-600">
                        <Paperclip className="h-3 w-3 shrink-0 text-slate-400" />
                        <span className="truncate">
                          {formValue.attachmentName ?? L.fldNoAttachment}
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
                              {L.fldUpload}
                            </span>
                          </label>
                        </div>
                      ) : null}
                    </div>
                  </div>

                  <div className="space-y-1 xl:col-span-12">
                    <FieldLabel label={L.fldReason} />
                    <textarea
                      value={formValue.reason}
                      disabled={readOnly}
                      rows={3}
                      onChange={(event) => updateField("reason", event.target.value)}
                      className={cn(
                        inputClassName,
                        "h-auto min-h-[58px] resize-y py-1.5",
                        errors.reason && inputErrorClassName
                      )}
                    />
                    <FieldError message={errors.reason} />
                  </div>
                </div>
              </SectionPanel>

              <SectionPanel title={L.secApprovalLine}>
                <AprvLineEditor
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
          </CardCnt>

          <div className="flex items-center justify-between gap-2 border-t border-slate-300 px-3 py-2">
            <div>
              {!readOnly && mode === "edit" && formValue.id && onDelete && canDelete ? (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setDeleteConfirmOpen(true)}
                  className="h-7 rounded-[3px] border-rose-300 bg-rose-50 px-2.5 text-[10px] font-medium text-rose-700 hover:bg-rose-100"
                >
                  {L.actDelete}
                </Button>
              ) : null}
            </div>
            <div className="flex items-center gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onClose}
              className="h-7 rounded-[3px] border-slate-300 px-2.5 text-[10px] font-medium"
            >
              {readOnly ? L.btnClose : L.btnCancel}
            </Button>
            {!readOnly ? (
              <>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setErrors({});
                    onSave(formValue, "Draft");
                  }}
                  className="h-7 rounded-[3px] border-slate-300 px-2.5 text-[10px] font-medium text-slate-700"
                >
                  {L.btnSaveDraft}
                </Button>
                <Button
                  type="button"
                  size="sm"
                  onClick={() => {
                    const errs = validate(formValue);
                    setErrors(errs);
                    setSubmitted(true);
                    if (Object.keys(errs).length > 0) {
                      toast.error(L.valToast);
                      return;
                    }
                    const hasApprover = formValue.approvalSteps.some((s) => s.usrs.length > 0);
                    if (!hasApprover) {
                      setNoApproverConfirmOpen(true);
                      return;
                    }
                    onSave(formValue, "Requested");
                  }}
                  className="h-7 rounded-[3px] bg-[#2f5b96] px-2.5 text-[10px] font-medium text-white hover:bg-[#274d7e]"
                >
                  {L.btnSubmit}
                </Button>
              </>
            ) : null}
            </div>
          </div>
        </Card>
      </div>

      <EmpPickerModal
        open={pickerState !== null}
        mode={pickerState?.kind === "cc" ? "cc" : "approver"}
        title={
          pickerState?.kind === "approver"
            ? `Select ${formatAprvStepLabel(currentPickerStep?.order ?? 1)} Approvers`
            : "Select C.C."
        }
        organizations={organizations}
        emps={emps}
        selectedUsrs={
          pickerState?.kind === "cc"
            ? formValue.ccs
            : currentPickerStep?.usrs ?? []
        }
        onClose={() => setPickerState(null)}
        onApply={(usrs) => {
          if (pickerState?.kind === "cc") {
            updateField("ccs", usrs);
          } else if (pickerState?.kind === "approver") {
            updateApprovalSteps((current) =>
              current.map((step) =>
                step.id === pickerState.stepId
                  ? { ...step, usrs: usrs.map((user) => ({ ...user })) }
                  : step
              )
            );
          }
          setPickerState(null);
        }}
      />

      {deleteConfirmOpen ? (
        <div className="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/40 p-4">
          <div className="absolute inset-0" aria-hidden="true" onClick={() => setDeleteConfirmOpen(false)} />
          <Card className="relative z-[91] w-full max-w-[480px] rounded-[6px] border-slate-300 bg-white shadow-[0_16px_40px_rgba(15,23,42,0.18)]">
            <CardHeader className="border-b border-rose-100 bg-[#fdf2f2] px-5 py-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#C53030]">
                  <svg className="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                    <path d="M10 11v6M14 11v6" />
                  </svg>
                </div>
                <CardTitle className="text-[16px] font-semibold leading-6 text-slate-900">
                  {L.confirmDeleteTitle}
                </CardTitle>
              </div>
            </CardHeader>
            <CardCnt className="px-5 py-5">
              <p className="text-[14px] leading-6 text-slate-800">
                {L.confirmDeleteDesc}
              </p>
            </CardCnt>
            <div className="flex items-center justify-end gap-2.5 border-t border-slate-200 px-5 py-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setDeleteConfirmOpen(false)}
                className="h-9 rounded-[4px] border-slate-300 px-5 text-[13px] font-medium text-slate-700"
              >
                {L.btnCancel}
              </Button>
              <Button
                type="button"
                onClick={async () => {
                  setDeleteConfirmOpen(false);
                  if (formValue.id && onDelete) {
                    await onDelete(formValue.id);
                  }
                }}
                className="h-9 rounded-[4px] bg-[#C53030] px-5 text-[13px] font-medium text-white hover:bg-[#a82828]"
              >
                {L.confirmDeleteBtn}
              </Button>
            </div>
          </Card>
        </div>
      ) : null}

      {noApproverConfirmOpen ? (
        <div className="fixed inset-0 z-[90] flex items-center justify-center bg-slate-950/40 p-4">
          <div className="absolute inset-0" aria-hidden="true" onClick={() => setNoApproverConfirmOpen(false)} />
          <Card className="relative z-[91] w-full max-w-[480px] rounded-[6px] border-slate-300 bg-white shadow-[0_16px_40px_rgba(15,23,42,0.18)]">
            <CardHeader className="border-b border-blue-100 bg-[#f0f4fb] px-5 py-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[#23468F]">
                  <svg className="h-5 w-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                </div>
                <CardTitle className="text-[16px] font-semibold leading-6 text-slate-900">
                  {L.confirmAutoConfirmTitle}
                </CardTitle>
              </div>
            </CardHeader>
            <CardCnt className="px-5 py-5">
              <p className="text-[14px] leading-6 text-slate-800">
                {L.infoAutoConfirm}
              </p>
            </CardCnt>
            <div className="flex items-center justify-end gap-2.5 border-t border-slate-200 px-5 py-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setNoApproverConfirmOpen(false)}
                className="h-9 rounded-[4px] border-slate-300 px-5 text-[13px] font-medium text-slate-700"
              >
                {L.btnCancel}
              </Button>
              <Button
                type="button"
                onClick={() => {
                  setNoApproverConfirmOpen(false);
                  onSave(formValue, "Requested");
                }}
                className="h-9 rounded-[4px] bg-[#23468F] px-5 text-[13px] font-medium text-white hover:bg-[#1d3a75]"
              >
                {L.confirmAutoConfirmBtn}
              </Button>
            </div>
          </Card>
        </div>
      ) : null}
    </>
  );
}

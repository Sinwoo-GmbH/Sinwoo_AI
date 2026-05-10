"use client";

import type {
  AprvStep,
  LeavePart,
} from "@/features/requests/leave/leave-mock-data";
import { formatAprvStepLabel } from "@/features/requests/leave/leave-mock-data";
import { SelectedUsrChip } from "@/features/requests/leave/selected-usr-chip";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Plus, Trash2 } from "lucide-react";

type AprvLineEditorProps = {
  approvalSteps: AprvStep[];
  ccs: LeavePart[];
  readOnly?: boolean;
  onAddApprovalStage: () => void;
  onAddStageApprovers: (stepId: string) => void;
  onRemoveApprovalStage: (stepId: string) => void;
  onRemoveStageApprover: (stepId: string, usrId: string) => void;
  onAddCc: () => void;
  onRemoveCc: (usrId: string) => void;
};

type AprvStageCardProps = {
  step: AprvStep;
  readOnly?: boolean;
  canRemoveStage: boolean;
  onAddApprovers: () => void;
  onRemoveStage: () => void;
  onRemoveUsr: (usrId: string) => void;
};

type ParticipantGroupProps = {
  title?: string;
  tone: "approver" | "cc";
  usrs: LeavePart[];
  readOnly?: boolean;
  actionLabel: string;
  onAdd: () => void;
  onRemove: (usrId: string) => void;
};

function EmptyState({ label }: { label: string }) {
  return (
    <div className="rounded-[3px] border border-dashed border-slate-300 bg-[#fafafa] px-2 py-1 text-[9px] text-slate-500">
      {label}
    </div>
  );
}

function AprvStageCard({
  step,
  readOnly = false,
  canRemoveStage,
  onAddApprovers,
  onRemoveStage,
  onRemoveUsr,
}: AprvStageCardProps) {
  return (
    <div className="grid gap-1.5 rounded-[3px] border border-slate-300 bg-white px-2 py-1.5 xl:grid-cols-[56px_minmax(0,1fr)_auto] xl:items-start">
      <div className="pt-[2px] text-[9px] font-semibold leading-3.5 text-slate-600">
        {formatAprvStepLabel(step.order)}
      </div>

      <div className="space-y-1">
        {step.usrs.length ? (
          <div className="flex flex-wrap gap-1">
            {step.usrs.map((user) => (
            <SelectedUsrChip
              key={`${step.id}-${user.id}`}
              user={user}
              tone="approver"
              compact
              onRemove={readOnly ? undefined : () => onRemoveUsr(user.id)}
            />
            ))}
          </div>
        ) : (
          <EmptyState label="No approvers assigned to this step." />
        )}
      </div>

      {!readOnly ? (
        <div className="flex items-center gap-1 xl:justify-end">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onAddApprovers}
            className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
          >
            <Plus className="mr-1 h-2 w-2" />
            Add
          </Button>
          {canRemoveStage ? (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onRemoveStage}
              className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] text-slate-500 hover:bg-slate-50"
            >
              <Trash2 className="h-2 w-2" />
            </Button>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}

function ParticipantGroup({
  title,
  tone,
  usrs,
  readOnly = false,
  actionLabel,
  onAdd,
  onRemove,
}: ParticipantGroupProps) {
  return (
    <div
      className={cn(
        "grid gap-1.5 rounded-[3px] border border-slate-300 bg-white px-2 py-1.5 xl:items-start",
        title
          ? "xl:grid-cols-[40px_minmax(0,1fr)_auto]"
          : "xl:grid-cols-[minmax(0,1fr)_auto]"
      )}
    >
      {title ? (
        <div className="pt-[2px] text-[9px] font-semibold leading-3.5 text-slate-600">
          {title}
        </div>
      ) : null}

      <div className="space-y-1">
        {usrs.length ? (
          <div className="flex flex-wrap gap-1">
            {usrs.map((user) => (
            <SelectedUsrChip
              key={user.id}
              user={user}
              tone={tone}
              compact
              onRemove={readOnly ? undefined : () => onRemove(user.id)}
            />
            ))}
          </div>
        ) : (
          <EmptyState label="No usrs selected." />
        )}
      </div>

      {!readOnly ? (
        <div className="flex items-center gap-1 xl:justify-end">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onAdd}
            className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
          >
            <Plus className="mr-1 h-2 w-2" />
            {actionLabel}
          </Button>
        </div>
      ) : null}
    </div>
  );
}

export function AprvLineEditor({
  approvalSteps,
  ccs,
  readOnly = false,
  onAddApprovalStage,
  onAddStageApprovers,
  onRemoveApprovalStage,
  onRemoveStageApprover,
  onAddCc,
  onRemoveCc,
}: AprvLineEditorProps) {
  return (
    <div className="flex flex-col gap-1.5 xl:flex-row xl:items-start">
      <div className="min-w-0 space-y-1.5 xl:w-1/2">
        <div className="flex items-center justify-between gap-2">
          <div className="text-[10px] font-semibold leading-4 text-slate-700">
            Approvers
          </div>
          {!readOnly ? (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onAddApprovalStage}
              className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
            >
              <Plus className="mr-1 h-2 w-2" />
              Add Step
            </Button>
          ) : null}
        </div>

        <div className="space-y-1">
          {approvalSteps.map((step) => (
            <AprvStageCard
              key={step.id}
              step={step}
              readOnly={readOnly}
              canRemoveStage={approvalSteps.length > 1}
              onAddApprovers={() => onAddStageApprovers(step.id)}
              onRemoveStage={() => onRemoveApprovalStage(step.id)}
              onRemoveUsr={(usrId) => onRemoveStageApprover(step.id, usrId)}
            />
          ))}
        </div>
      </div>

      <div className="w-full xl:w-1/2 xl:shrink-0">
        <div className="space-y-1.5">
          <div className="flex items-center justify-between gap-2">
            <div className="text-[10px] font-semibold leading-4 text-slate-700">
              C.C.
            </div>
            {!readOnly ? (
              <div
                aria-hidden="true"
                className="h-5 rounded-[3px] px-1.5 text-[9px] opacity-0"
              >
                Add Step
              </div>
            ) : null}
          </div>

          <ParticipantGroup
            tone="cc"
            usrs={ccs}
            readOnly={readOnly}
            actionLabel="Add"
            onAdd={onAddCc}
            onRemove={onRemoveCc}
          />
        </div>
      </div>
    </div>
  );
}

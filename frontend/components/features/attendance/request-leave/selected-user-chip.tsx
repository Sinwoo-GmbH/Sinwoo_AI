"use client";

import type { LeaveParticipant } from "@/components/features/attendance/request-leave/leave-mock-data";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { X } from "lucide-react";

type SelectedUserChipProps = {
  user: LeaveParticipant;
  tone?: "approver" | "cc";
  onRemove?: () => void;
  compact?: boolean;
};

export function SelectedUserChip({
  user,
  tone = "approver",
  onRemove,
  compact = false,
}: SelectedUserChipProps) {
  return (
    <div
      className={cn(
        "flex items-center justify-between gap-2 rounded-[3px] border",
        compact ? "px-1.5 py-0.5" : "px-2 py-1",
        tone === "approver"
          ? "border-slate-300 bg-white"
          : "border-slate-300 bg-[#f6f8fc]"
      )}
    >
      <div className="min-w-0">
        <p
          className={cn(
            "truncate font-semibold text-slate-800",
            compact ? "text-[9px] leading-3.5" : "text-[10px] leading-4"
          )}
        >
          {user.name}
        </p>
        {!compact ? (
          <p className="truncate text-[9px] leading-3 text-slate-500">
            {user.department} / {user.position}
          </p>
        ) : null}
      </div>
      {onRemove ? (
        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={onRemove}
          className={cn(
            "rounded-[3px] p-0 text-slate-400 hover:bg-slate-100 hover:text-slate-700",
            compact ? "h-4 w-4" : "h-5 w-5"
          )}
        >
          <X className={cn(compact ? "h-2 w-2" : "h-2.5 w-2.5")} />
        </Button>
      ) : null}
    </div>
  );
}

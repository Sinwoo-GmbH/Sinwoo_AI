"use client";

import type {
  ApproverStatus,
  LeaveStatus,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

type LeaveStatusBadgeProps = {
  status: LeaveStatus;
  className?: string;
};

type LeaveApproverStatusBadgeProps = {
  status: ApproverStatus;
  className?: string;
};

const leaveStatusToneMap: Record<LeaveStatus, string> = {
  Draft: "border border-slate-300 bg-[#f4f4f5] text-slate-700",
  Requested: "border border-blue-200 bg-[#edf4ff] text-[#30578e]",
  Approved: "border border-emerald-200 bg-[#eef8f1] text-[#2f6d43]",
  Rejected: "border border-rose-200 bg-[#fff1f3] text-[#9a4050]",
  Cancelled: "border border-amber-200 bg-[#faf6ed] text-[#8b5a1f]",
  "Admin Cancelled": "border border-slate-300 bg-[#efefef] text-slate-700",
};

const approverStatusToneMap: Record<Exclude<ApproverStatus, `Approved by ${number}`>, string> = {
  "No Approver": "border border-slate-300 bg-[#f4f4f5] text-slate-600",
  Waiting: "border border-blue-200 bg-[#edf4ff] text-[#30578e]",
  Rejected: "border border-rose-200 bg-[#fff1f3] text-[#9a4050]",
};

function approverStatusTone(status: ApproverStatus) {
  if (status.startsWith("Approved by ")) {
    return "border border-emerald-200 bg-[#eef8f1] text-[#2f6d43]";
  }
  return approverStatusToneMap[status as Exclude<ApproverStatus, `Approved by ${number}`>];
}

export function LeaveStatusBadge({ status, className }: LeaveStatusBadgeProps) {
  return (
    <Badge
      className={cn(
        "rounded-[3px] px-1.5 py-[1px] text-[9px] font-medium leading-3",
        leaveStatusToneMap[status],
        className
      )}
    >
      {status}
    </Badge>
  );
}

export function LeaveApproverStatusBadge({
  status,
  className,
}: LeaveApproverStatusBadgeProps) {
  return (
    <Badge
      className={cn(
        "rounded-[3px] px-1.5 py-[1px] text-[9px] font-medium leading-3",
        approverStatusTone(status),
        className
      )}
    >
      {status}
    </Badge>
  );
}

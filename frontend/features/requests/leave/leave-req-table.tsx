"use client";

import { useEffect, useMemo, useState } from "react";

import type { LeaveReqRec } from "@/features/requests/leave/leave-mock-data";
import {
  formatLeaveDays,
  toDeductionTableLabel,
} from "@/features/requests/leave/leave-mock-data";
import {
  LeaveApproverStatusBadge,
  LeaveStatusBadge,
} from "@/features/requests/leave/leave-status-badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Check, ChevronLeft, ChevronRight, Eye, PencilLine, Slash, XCircle } from "lucide-react";

type LeaveReqTableProps = {
  rows: LeaveReqRec[];
  onEdit: (rec: LeaveReqRec) => void;
  onView: (rec: LeaveReqRec) => void;
  onCancel: (rec: LeaveReqRec) => void;
  onApprove: (rec: LeaveReqRec) => void;
  onReject: (rec: LeaveReqRec) => void;
  onOpen: (rec: LeaveReqRec) => void;
};

type PageSizeOpt = 15 | 50 | 100 | 200 | "all";

const PAGE_SIZE_OPTS: readonly PageSizeOpt[] = [15, 50, 100, 200, "all"] as const;

function LeaveRowActs({
  rec,
  onEdit,
  onView,
  onCancel,
  onApprove,
  onReject,
}: {
  rec: LeaveReqRec;
  onEdit: (rec: LeaveReqRec) => void;
  onView: (rec: LeaveReqRec) => void;
  onCancel: (rec: LeaveReqRec) => void;
  onApprove: (rec: LeaveReqRec) => void;
  onReject: (rec: LeaveReqRec) => void;
}) {
  if (rec.canApprove || rec.canReject) {
    return (
      <div className="flex items-center gap-1">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => onView(rec)}
          className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
        >
          <Eye className="mr-1 h-2 w-2" />
          View
        </Button>
        {rec.canApprove ? (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => onApprove(rec)}
            className="h-5 rounded-[3px] border-emerald-300 bg-emerald-50 px-1.5 text-[9px] font-medium text-emerald-700 hover:bg-emerald-100"
          >
            <Check className="mr-1 h-2 w-2" />
            Approve
          </Button>
        ) : null}
        {rec.canReject ? (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => onReject(rec)}
            className="h-5 rounded-[3px] border-rose-300 bg-rose-50 px-1.5 text-[9px] font-medium text-rose-700 hover:bg-rose-100"
          >
            <XCircle className="mr-1 h-2 w-2" />
            Reject
          </Button>
        ) : null}
      </div>
    );
  }

  if (rec.canEdit || rec.status === "Draft") {
    return (
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={() => onEdit(rec)}
        className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
      >
        <PencilLine className="mr-1 h-2 w-2" />
        Edit
      </Button>
    );
  }

  if (rec.canCancel) {
    return (
      <div className="flex items-center gap-1">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => onView(rec)}
          className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
        >
          <Eye className="mr-1 h-2 w-2" />
          View
        </Button>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => onCancel(rec)}
          className="h-5 rounded-[3px] border-slate-300 bg-[#faf7f2] px-1.5 text-[9px] font-medium text-[#8b5a1f] hover:bg-[#f6efe3]"
        >
          <Slash className="mr-1 h-2 w-2" />
          Cancel
        </Button>
      </div>
    );
  }

  return (
    <Button
      type="button"
      variant="outline"
      size="sm"
      onClick={() => onView(rec)}
      className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
    >
      <Eye className="mr-1 h-2 w-2" />
      View
    </Button>
  );
}

export function LeaveReqTable({
  rows,
  onEdit,
  onView,
  onCancel,
  onApprove,
  onReject,
  onOpen,
}: LeaveReqTableProps) {
  const [pageSize, setPageSize] = useState<PageSizeOpt>(15);
  const [page, setPage] = useState(1);

  const totalPages =
    pageSize === "all" ? 1 : Math.max(1, Math.ceil(rows.length / pageSize));

  useEffect(() => {
    setPage(1);
  }, [pageSize]);

  useEffect(() => {
    setPage((current) => Math.min(current, totalPages));
  }, [totalPages]);

  const visibleRows = useMemo(() => {
    if (pageSize === "all") {
      return rows;
    }

    const startIndex = (page - 1) * pageSize;
    return rows.slice(startIndex, startIndex + pageSize);
  }, [page, pageSize, rows]);

  const visibleRangeStart = rows.length
    ? pageSize === "all"
      ? 1
      : (page - 1) * pageSize + 1
    : 0;
  const visibleRangeEnd =
    pageSize === "all"
      ? rows.length
      : Math.min(page * pageSize, rows.length);

  return (
    <div className="flex h-full min-h-0 flex-col overflow-hidden rounded-[3px] border border-slate-300 bg-white">
      <div className="flex min-h-0 flex-1 flex-col overflow-hidden">
        <div className="overflow-x-auto overflow-y-hidden">
        <table className="min-w-full border-collapse text-[11px]">
          <thead className="sticky top-0 z-10 bg-[#f3f4f6]">
            <tr className="border-b border-slate-300 text-left text-[9px] text-slate-500">
              {[
                "No",
                "Leave Type",
                "Deduction Type",
                "Start Date",
                "End Date",
                "Days",
                "Approver Status",
                "Status",
                "Created At",
                "Actions",
              ].map((label) => (
                <th key={label} className="whitespace-nowrap px-2 py-1.5 font-medium">
                  {label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {visibleRows.length ? (
              visibleRows.map((rec) => (
                <tr
                  key={rec.id}
                  onDoubleClick={() => onOpen(rec)}
                  className={cn(
                    "cursor-pointer border-b border-slate-200 bg-white transition-colors hover:bg-slate-50",
                    rec.status === "Draft" ? "bg-[#f9fbff]" : ""
                  )}
                >
                  <td className="px-2 py-1 text-[9px] leading-3.5 text-slate-600">{rec.no}</td>
                  <td className="px-2 py-1 text-[9px] font-medium leading-3.5 text-slate-800">
                    {rec.leaveType}
                  </td>
                  <td className="px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {toDeductionTableLabel(rec.deductionType)}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {rec.startDate}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {rec.endDate}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-700">
                    {formatLeaveDays(rec.days)}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1">
                    <LeaveApproverStatusBadge status={rec.approverStatus} />
                  </td>
                  <td className="whitespace-nowrap px-2 py-1">
                    <LeaveStatusBadge status={rec.status} />
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {rec.createdAt}
                  </td>
                  <td className="px-2 py-1">
                    <LeaveRowActs
                      rec={rec}
                      onEdit={onEdit}
                      onView={onView}
                      onCancel={onCancel}
                      onApprove={onApprove}
                      onReject={onReject}
                    />
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={10} className="px-3 py-8 text-center text-[10px] text-slate-500">
                  No leave requests match the current filters.
                </td>
              </tr>
            )}
          </tbody>
        </table>
        </div>
      </div>

      <div className="flex items-center justify-between border-t border-slate-300 bg-[#f8f9fb] px-2 py-1.5">
        <div className="flex items-center gap-2 text-[9px] text-slate-500">
          <span className="font-medium text-slate-600">Rows</span>
          <select
            value={pageSize}
            onChange={(event) => {
              setPage(1);
              setPageSize(
                event.target.value === "all"
                  ? "all"
                  : (Number(event.target.value) as Exclude<PageSizeOpt, "all">)
              );
            }}
            className="h-6 rounded-[3px] border border-slate-300 bg-white px-1.5 text-[9px] text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
          >
            {PAGE_SIZE_OPTS.map((option) => (
              <option key={option} value={option}>
                {option === "all" ? "전체" : option}
              </option>
            ))}
          </select>
          <span>
            {visibleRangeStart}-{visibleRangeEnd} / {rows.length}
          </span>
        </div>

        <div className="flex items-center gap-1 text-[9px] text-slate-500">
          <span className="mr-1">
            {page} / {totalPages}
          </span>
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((current) => Math.max(current - 1, 1))}
            className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] text-slate-700"
          >
            <ChevronLeft className="h-2.5 w-2.5" />
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={page >= totalPages}
            onClick={() => setPage((current) => Math.min(current + 1, totalPages))}
            className="h-5 rounded-[3px] border-slate-300 px-1.5 text-[9px] text-slate-700"
          >
            <ChevronRight className="h-2.5 w-2.5" />
          </Button>
        </div>
      </div>
    </div>
  );
}

"use client";

import { useEffect, useMemo, useState } from "react";

import type { LeaveRequestRecord } from "@/components/features/attendance/request-leave/leave-mock-data";
import {
  formatLeaveDays,
  toDeductionTableLabel,
} from "@/components/features/attendance/request-leave/leave-mock-data";
import {
  LeaveApproverStatusBadge,
  LeaveStatusBadge,
} from "@/components/features/attendance/request-leave/leave-status-badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ChevronLeft, ChevronRight, Eye, PencilLine, Slash } from "lucide-react";

type LeaveRequestTableProps = {
  rows: LeaveRequestRecord[];
  onEdit: (record: LeaveRequestRecord) => void;
  onView: (record: LeaveRequestRecord) => void;
  onCancel: (record: LeaveRequestRecord) => void;
  onOpen: (record: LeaveRequestRecord) => void;
};

type PageSizeOption = 15 | 50 | 100 | 200 | "all";

const PAGE_SIZE_OPTIONS: readonly PageSizeOption[] = [15, 50, 100, 200, "all"] as const;

function LeaveRowActions({
  record,
  onEdit,
  onView,
  onCancel,
}: {
  record: LeaveRequestRecord;
  onEdit: (record: LeaveRequestRecord) => void;
  onView: (record: LeaveRequestRecord) => void;
  onCancel: (record: LeaveRequestRecord) => void;
}) {
  if (record.status === "Draft") {
    return (
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={() => onEdit(record)}
        className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
      >
        <PencilLine className="mr-1 h-2 w-2" />
        Edit
      </Button>
    );
  }

  if (record.status === "Requested") {
    return (
      <div className="flex items-center gap-1">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => onView(record)}
          className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
        >
          <Eye className="mr-1 h-2 w-2" />
          View
        </Button>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => onCancel(record)}
          className="h-5 rounded-[3px] border-slate-300 bg-[#faf7f2] px-1.5 text-[9px] font-medium text-[#8b5a1f] hover:bg-[#f6efe3]"
        >
          <Slash className="mr-1 h-2 w-2" />
          Cancel
        </Button>
      </div>
    );
  }

  if (
    record.status === "Approved" ||
    record.status === "Rejected" ||
    record.status === "Cancelled" ||
    record.status === "Admin Cancelled"
  ) {
    return (
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={() => onView(record)}
        className="h-5 rounded-[3px] border-slate-300 bg-white px-1.5 text-[9px] font-medium text-slate-700 hover:bg-slate-50"
      >
        <Eye className="mr-1 h-2 w-2" />
        View
      </Button>
    );
  }

  return <span className="text-[10px] text-slate-400">-</span>;
}

export function LeaveRequestTable({
  rows,
  onEdit,
  onView,
  onCancel,
  onOpen,
}: LeaveRequestTableProps) {
  const [pageSize, setPageSize] = useState<PageSizeOption>(15);
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
              visibleRows.map((record) => (
                <tr
                  key={record.id}
                  onDoubleClick={() => onOpen(record)}
                  className={cn(
                    "cursor-pointer border-b border-slate-200 bg-white transition-colors hover:bg-slate-50",
                    record.status === "Draft" ? "bg-[#f9fbff]" : ""
                  )}
                >
                  <td className="px-2 py-1 text-[9px] leading-3.5 text-slate-600">{record.no}</td>
                  <td className="px-2 py-1 text-[9px] font-medium leading-3.5 text-slate-800">
                    {record.leaveType}
                  </td>
                  <td className="px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {toDeductionTableLabel(record.deductionType)}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {record.startDate}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {record.endDate}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-700">
                    {formatLeaveDays(record.days)}
                  </td>
                  <td className="whitespace-nowrap px-2 py-1">
                    <LeaveApproverStatusBadge status={record.approverStatus} />
                  </td>
                  <td className="whitespace-nowrap px-2 py-1">
                    <LeaveStatusBadge status={record.status} />
                  </td>
                  <td className="whitespace-nowrap px-2 py-1 text-[9px] leading-3.5 text-slate-600">
                    {record.createdAt}
                  </td>
                  <td className="px-2 py-1">
                    <LeaveRowActions
                      record={record}
                      onEdit={onEdit}
                      onView={onView}
                      onCancel={onCancel}
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
                  : (Number(event.target.value) as Exclude<PageSizeOption, "all">)
              );
            }}
            className="h-6 rounded-[3px] border border-slate-300 bg-white px-1.5 text-[9px] text-slate-700 outline-none transition focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
          >
            {PAGE_SIZE_OPTIONS.map((option) => (
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

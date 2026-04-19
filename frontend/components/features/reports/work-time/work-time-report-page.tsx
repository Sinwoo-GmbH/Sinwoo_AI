"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { Button } from "@/components/ui/button";
import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspaceFilterBar } from "@/components/workspace/workspace-filter-bar";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import {
  ATTENDANCE_REPORT_API_PATH,
  type AttendanceWorkTimeFilterOptionsRes,
  type AttendanceWorkTimeHistoryListRes,
} from "@/lib/api/attendance-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceWorkTimeHistoryMessages } from "@/lib/i18n/workspace-content";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type WorkTimeReportPageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  mode: "client" | "admin";
  onUnauthorized: () => void;
  onLoadingChange?: (loading: boolean) => void;
};

type FilterState = {
  yearMonth: string;
  empNm: string;
  deptNm: string;
  keyword: string;
};

function getCurrentYearMonth() {
  return new Date().toISOString().slice(0, 7);
}

function toQueryString(filters: FilterState, locale: LoginLocale) {
  const params = new URLSearchParams();
  if (filters.yearMonth) params.set("yearMonth", filters.yearMonth);
  if (filters.empNm) params.set("empNm", filters.empNm);
  if (filters.deptNm) params.set("deptNm", filters.deptNm);
  if (filters.keyword) params.set("keyword", filters.keyword);
  params.set("lang", locale);
  return params.toString();
}

function formatWorkTime(workMinuteCnt: number) {
  const hours = Math.floor((workMinuteCnt ?? 0) / 60);
  const minutes = (workMinuteCnt ?? 0) % 60;
  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = fileName;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.URL.revokeObjectURL(url);
}

function extractFileName(contentDisposition: string | null, fallbackName: string) {
  if (!contentDisposition) return fallbackName;
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }
  const basicMatch = contentDisposition.match(/filename=\"?([^\";]+)\"?/i);
  return basicMatch?.[1] ?? fallbackName;
}

export function WorkTimeReportPage({
  accessToken,
  locale,
  mode,
  onUnauthorized,
  onLoadingChange,
}: WorkTimeReportPageProps) {
  const text = getWorkspaceWorkTimeHistoryMessages(locale);
  const initialFilters = useMemo<FilterState>(
    () => ({
      yearMonth: getCurrentYearMonth(),
      empNm: "",
      deptNm: "",
      keyword: "",
    }),
    []
  );
  const [filters, setFilters] = useState<FilterState>(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState<FilterState>(initialFilters);
  const [options, setOptions] = useState<AttendanceWorkTimeFilterOptionsRes | null>(null);
  const [history, setHistory] = useState<AttendanceWorkTimeHistoryListRes | null>(null);

  const runWithLoading = useCallback(
    async <T,>(task: () => Promise<T>) => {
      onLoadingChange?.(true);
      try {
        return await task();
      } finally {
        onLoadingChange?.(false);
      }
    },
    [onLoadingChange]
  );

  const fetchFilterOptions = useCallback(async () => {
    if (!accessToken) {
      onUnauthorized();
      return;
    }

    const response = await fetch(`${API_BASE_URL}${ATTENDANCE_REPORT_API_PATH}/filter-options`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      cache: "no-store",
    });

    if (response.status === 401) {
      onUnauthorized();
      return;
    }

    if (!response.ok) {
      throw new Error("Failed to load work time filter options");
    }

    const data = (await response.json()) as AttendanceWorkTimeFilterOptionsRes;
    setOptions(data);
  }, [accessToken, onUnauthorized]);

  const fetchHistory = useCallback(
    async (nextFilters: FilterState) => {
      if (!accessToken) {
        onUnauthorized();
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}${ATTENDANCE_REPORT_API_PATH}/history?${toQueryString(nextFilters, locale)}`,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          cache: "no-store",
        }
      );

      if (response.status === 401) {
        onUnauthorized();
        return;
      }

      if (!response.ok) {
        throw new Error("Failed to load work time history");
      }

      const data = (await response.json()) as AttendanceWorkTimeHistoryListRes;
      setHistory(data);
    },
    [accessToken, locale, onUnauthorized]
  );

  const runExport = useCallback(
    async (format: "excel" | "pdf") => {
      if (!accessToken) {
        onUnauthorized();
        return;
      }

      await runWithLoading(async () => {
        const response = await fetch(
          `${API_BASE_URL}${ATTENDANCE_REPORT_API_PATH}/history/export/${format}?${toQueryString(appliedFilters, locale)}`,
          {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
          }
        );

        if (response.status === 401) {
          onUnauthorized();
          return;
        }

        if (!response.ok) {
          throw new Error(`Failed to export work time history as ${format}`);
        }

        const blob = await response.blob();
        const fallbackName = `work-time-history-${appliedFilters.yearMonth}.${format === "excel" ? "xlsx" : "pdf"}`;
        downloadBlob(blob, extractFileName(response.headers.get("content-disposition"), fallbackName));
      });
    },
    [accessToken, appliedFilters, locale, onUnauthorized, runWithLoading]
  );

  useEffect(() => {
    void runWithLoading(async () => {
      await fetchFilterOptions();
      await fetchHistory(initialFilters);
    });
  }, [fetchFilterOptions, fetchHistory, initialFilters, runWithLoading]);

  useEffect(() => {
    if (!history) return;
    void runWithLoading(async () => {
      await fetchHistory(appliedFilters);
    });
  }, [appliedFilters, fetchHistory, history, locale, runWithLoading]);

  const ownOnlyYn = history?.ownOnlyYn ?? options?.ownOnlyYn ?? false;

  return (
    <div id="workspace-work-time-history-page" className="space-y-2.5">
      <WorkspacePageHeader
        strip
        id="workspace-work-time-history-header-card"
        title={text.title}
        titleId="workspace-work-time-history-title"
      />

      <WorkspaceFilterBar
        compact
        id="workspace-work-time-history-filter-bar"
        title={text.filtersTitle}
        titleId="workspace-work-time-history-filter-title"
      >
        <div
          id="workspace-work-time-history-filters"
          className="grid gap-2 xl:grid-cols-[150px_180px_180px_minmax(220px,1fr)_auto_auto_auto]"
        >
          <label id="workspace-work-time-history-month-field" className="space-y-1">
            <span className="text-[11px] font-medium text-slate-500">{text.month}</span>
            <input
              id="workspace-work-time-history-month"
              type="month"
              value={filters.yearMonth}
              onChange={(event) => setFilters((current) => ({ ...current, yearMonth: event.target.value }))}
              className="h-9 w-full rounded-xl border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
            />
          </label>

          {!ownOnlyYn ? (
            <label id="workspace-work-time-history-employee-field" className="space-y-1">
              <span className="text-[11px] font-medium text-slate-500">{text.employee}</span>
              <select
                id="workspace-work-time-history-employee"
                value={filters.empNm}
                onChange={(event) => setFilters((current) => ({ ...current, empNm: event.target.value }))}
                className="h-9 w-full rounded-xl border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
              >
                <option value="">{text.allEmployees}</option>
                {options?.empList.map((item) => (
                  <option key={item.refId} value={item.refNm}>
                    {item.refNm}
                  </option>
                ))}
              </select>
            </label>
          ) : null}

          {!ownOnlyYn ? (
            <label id="workspace-work-time-history-department-field" className="space-y-1">
              <span className="text-[11px] font-medium text-slate-500">{text.department}</span>
              <select
                id="workspace-work-time-history-department"
                value={filters.deptNm}
                onChange={(event) => setFilters((current) => ({ ...current, deptNm: event.target.value }))}
                className="h-9 w-full rounded-xl border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
              >
                <option value="">{text.allDepartments}</option>
                {options?.deptList.map((item) => (
                  <option key={item.refId} value={item.refNm}>
                    {item.refNm}
                  </option>
                ))}
              </select>
            </label>
          ) : null}

          <label id="workspace-work-time-history-keyword-field" className="space-y-1">
            <span className="text-[11px] font-medium text-slate-500">{text.keyword}</span>
            <input
              id="workspace-work-time-history-keyword"
              type="text"
              value={filters.keyword}
              placeholder={text.keywordPlaceholder}
              onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))}
              className="h-9 w-full rounded-xl border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
            />
          </label>

          <div id="workspace-work-time-history-search-wrap" className="flex items-end">
            <Button
              id="workspace-work-time-history-search"
              type="button"
              className="h-9 rounded-xl bg-[#233A7A] px-3 text-xs"
              onClick={() => setAppliedFilters(filters)}
            >
              {text.search}
            </Button>
          </div>

          <div id="workspace-work-time-history-reset-wrap" className="flex items-end">
            <Button
              id="workspace-work-time-history-reset"
              type="button"
              variant="outline"
              className="h-9 rounded-xl px-3 text-xs"
              onClick={() => {
                setFilters(initialFilters);
                setAppliedFilters(initialFilters);
              }}
            >
              {text.reset}
            </Button>
          </div>

          <div id="workspace-work-time-history-export-wrap" className="flex items-end gap-1.5">
            <Button
              id="workspace-work-time-history-export-excel"
              type="button"
              variant="outline"
              className="h-9 rounded-xl px-3 text-xs"
              onClick={() => void runExport("excel")}
            >
              {text.exportExcel}
            </Button>
            <Button
              id="workspace-work-time-history-export-pdf"
              type="button"
              variant="outline"
              className="h-9 rounded-xl px-3 text-xs"
              onClick={() => void runExport("pdf")}
            >
              {text.exportPdf}
            </Button>
          </div>
        </div>
      </WorkspaceFilterBar>

      <WorkspaceSectionPanel
        id="workspace-work-time-history-table-card"
        title={text.resultsTitle}
        titleId="workspace-work-time-history-table-title"
        actionsId="workspace-work-time-history-table-actions"
        className="border-slate-200/90 shadow-[0_16px_32px_rgba(148,163,184,0.08)]"
        headerClassName="px-3.5 py-2.5"
        actions={
          <div id="workspace-work-time-history-total" className="text-[11px] font-medium text-slate-500">
            {text.total} {history?.totCnt ?? 0} {text.rows}
          </div>
        }
        contentClassName="px-3.5 py-3.5 pt-2.5"
      >
        {history?.itemList?.length ? (
          <WorkspaceContentContainer id="workspace-work-time-history-table-container" className="bg-slate-50/55">
            <div id="workspace-work-time-history-table-wrap" className="overflow-x-auto">
              <table id="workspace-work-time-history-table" className="min-w-full border-separate border-spacing-y-2">
                <thead>
                  <tr className="text-left text-xs uppercase tracking-[0.2em] text-slate-400">
                    <th className="px-3 py-2">{text.date}</th>
                    <th className="px-3 py-2">{text.employee}</th>
                    <th className="px-3 py-2">{text.department}</th>
                    <th className="px-3 py-2">{text.status}</th>
                    <th className="px-3 py-2">{text.checkIn}</th>
                    <th className="px-3 py-2">{text.checkOut}</th>
                    <th className="px-3 py-2">{text.workTime}</th>
                  </tr>
                </thead>
                <tbody>
                  {history.itemList.map((item, index) => (
                    <tr
                      id={`workspace-work-time-history-row-${index + 1}`}
                      key={`${item.attndId}-${item.attndDt}-${index}`}
                      className="rounded-2xl border border-slate-200 bg-white text-sm text-slate-700 shadow-[0_8px_16px_rgba(148,163,184,0.05)]"
                    >
                      <td className="rounded-l-2xl px-3 py-3">{item.attndDt}</td>
                      <td className="px-3 py-3 font-medium text-slate-900">{item.empNm}</td>
                      <td className="px-3 py-3">{item.deptNm ?? "-"}</td>
                      <td className="px-3 py-3">{item.attndStsNm}</td>
                      <td className="px-3 py-3">{item.chkinTm ?? "-"}</td>
                      <td className="px-3 py-3">{item.chkoutTm ?? "-"}</td>
                      <td className="rounded-r-2xl px-3 py-3">{formatWorkTime(item.workMinuteCnt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </WorkspaceContentContainer>
        ) : (
          <div id="workspace-work-time-history-empty" className="rounded-[24px] border border-dashed border-slate-200 bg-slate-50 px-5 py-10 text-center">
            <div id="workspace-work-time-history-empty-title" className="text-base font-semibold text-slate-700">
              {text.noDataTitle}
            </div>
            <div id="workspace-work-time-history-empty-description" className="mt-2 text-sm text-slate-500">
              {text.noDataDescription}
            </div>
          </div>
        )}
      </WorkspaceSectionPanel>
    </div>
  );
}

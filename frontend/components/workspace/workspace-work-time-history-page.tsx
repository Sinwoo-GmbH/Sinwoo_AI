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
  type AttendanceWorkTimeHistoryQuery,
} from "@/lib/api/attendance-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceWorkTimeHistoryMessages } from "@/lib/i18n/workspace-content";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Props = {
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

const uiText = {
  en: {
    eyebrow: "reports",
    title: "Work time history",
    description: "Review monthly work records, filter by employee or department, and export the result for reporting usage.",
    currentScope: "Scope",
    ownOnly: "Own records only",
    scopedAll: "All employees in scope",
    month: "Target month",
    employee: "Employee",
    department: "Department",
    keyword: "Keyword",
    allEmployees: "All employees",
    allDepartments: "All departments",
    keywordPlaceholder: "Name, department, status, or time",
    search: "Search",
    reset: "Reset",
    exportExcel: "Export Excel",
    exportPdf: "Export PDF",
    total: "Total",
    rows: "rows",
    filtersTitle: "Filters",
    filtersDescription: "Narrow the month and employee scope before exporting or reviewing work records.",
    resultsTitle: "History records",
    date: "Date",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Work time",
    noDataTitle: "No records found",
    noDataDescription: "Adjust the filters or switch the month to review another work history range.",
  },
  de: {
    eyebrow: "berichte",
    title: "Arbeitszeitverlauf",
    description: "Monatliche Arbeitszeitdaten prüfen, nach Mitarbeiter oder Abteilung filtern und für Berichte exportieren.",
    currentScope: "Bereich",
    ownOnly: "Nur eigene Einträge",
    scopedAll: "Alle Mitarbeiter im Bereich",
    month: "Monat",
    employee: "Mitarbeiter",
    department: "Abteilung",
    keyword: "Stichwort",
    allEmployees: "Alle Mitarbeiter",
    allDepartments: "Alle Abteilungen",
    keywordPlaceholder: "Name, Abteilung, Status oder Zeit",
    search: "Suchen",
    reset: "Zurücksetzen",
    exportExcel: "Excel exportieren",
    exportPdf: "PDF exportieren",
    total: "Gesamt",
    rows: "Zeilen",
    filtersTitle: "Filter",
    filtersDescription: "Monat und Mitarbeiterbereich eingrenzen, bevor Arbeitszeitdaten geprüft oder exportiert werden.",
    resultsTitle: "Verlaufsdaten",
    date: "Datum",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Arbeitszeit",
    noDataTitle: "Keine Datensätze gefunden",
    noDataDescription: "Passen Sie die Filter an oder wechseln Sie den Monat, um einen anderen Zeitraum zu prüfen.",
  },
  ko: {
    eyebrow: "리포트",
    title: "근태 이력",
    description: "월간 근태 기록을 직원·부서별로 조회하고, 보고용으로 Excel 또는 PDF로 내보낼 수 있습니다.",
    currentScope: "조회 범위",
    ownOnly: "본인 기록만",
    scopedAll: "권한 범위 내 전체 직원",
    month: "대상 월",
    employee: "직원",
    department: "부서",
    keyword: "키워드",
    allEmployees: "전체 직원",
    allDepartments: "전체 부서",
    keywordPlaceholder: "이름, 부서, 상태, 시간",
    search: "조회",
    reset: "초기화",
    exportExcel: "Excel 내보내기",
    exportPdf: "PDF 내보내기",
    total: "총",
    rows: "건",
    filtersTitle: "필터",
    filtersDescription: "대상 월과 직원/부서 범위를 조정한 뒤 근태 기록을 확인하거나 내보낼 수 있습니다.",
    resultsTitle: "기록 목록",
    date: "일자",
    status: "상태",
    checkIn: "출근",
    checkOut: "퇴근",
    workTime: "근무시간",
    noDataTitle: "조회된 기록이 없습니다",
    noDataDescription: "필터를 조정하거나 대상 월을 바꿔 다른 기간을 확인해 주세요.",
  },
} satisfies Record<LoginLocale, Record<string, string>>;

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

export function WorkspaceWorkTimeHistoryPage({
  accessToken,
  locale,
  mode,
  onUnauthorized,
  onLoadingChange,
}: Props) {
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
    <div id="workspace-work-time-history-page" className="space-y-4">
      <WorkspacePageHeader
        id="workspace-work-time-history-header-card"
        eyebrow={text.eyebrow}
        eyebrowId="workspace-work-time-history-eyebrow"
        title={text.title}
        titleId="workspace-work-time-history-title"
        description={text.description}
        descriptionId="workspace-work-time-history-description"
        actionsId="workspace-work-time-history-header-actions"
        actions={
          <div id="workspace-work-time-history-scope-row" className="flex flex-wrap items-center gap-2">
            <span id="workspace-work-time-history-scope-label" className="text-xs uppercase tracking-[0.22em] text-slate-400">
              {text.currentScope}
            </span>
            <span
              id="workspace-work-time-history-scope-value"
              className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-medium text-slate-600"
            >
              {ownOnlyYn ? text.ownOnly : text.scopedAll}
            </span>
            <span
              id="workspace-work-time-history-mode-badge"
              className="rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-xs font-medium text-[#18397E]"
            >
              {mode === "admin" ? text.modeAdmin : text.modeClient}
            </span>
          </div>
        }
      />

      <WorkspaceFilterBar
        id="workspace-work-time-history-filter-bar"
        title={text.filtersTitle}
        titleId="workspace-work-time-history-filter-title"
        description={text.filtersDescription}
        descriptionId="workspace-work-time-history-filter-description"
      >
        <div id="workspace-work-time-history-filters" className="grid gap-3 xl:grid-cols-[1.1fr_1fr_1fr_1.4fr_auto_auto_auto]">
            <label id="workspace-work-time-history-month-field" className="space-y-1.5">
              <span className="text-xs font-medium text-slate-500">{text.month}</span>
              <input
                id="workspace-work-time-history-month"
                type="month"
                value={filters.yearMonth}
                onChange={(event) => setFilters((current) => ({ ...current, yearMonth: event.target.value }))}
                className="h-10 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
              />
            </label>

            {!ownOnlyYn ? (
              <label id="workspace-work-time-history-employee-field" className="space-y-1.5">
                <span className="text-xs font-medium text-slate-500">{text.employee}</span>
                <select
                  id="workspace-work-time-history-employee"
                  value={filters.empNm}
                  onChange={(event) => setFilters((current) => ({ ...current, empNm: event.target.value }))}
                  className="h-10 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
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
              <label id="workspace-work-time-history-department-field" className="space-y-1.5">
                <span className="text-xs font-medium text-slate-500">{text.department}</span>
                <select
                  id="workspace-work-time-history-department"
                  value={filters.deptNm}
                  onChange={(event) => setFilters((current) => ({ ...current, deptNm: event.target.value }))}
                  className="h-10 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
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

            <label id="workspace-work-time-history-keyword-field" className="space-y-1.5">
              <span className="text-xs font-medium text-slate-500">{text.keyword}</span>
              <input
                id="workspace-work-time-history-keyword"
                type="text"
                value={filters.keyword}
                placeholder={text.keywordPlaceholder}
                onChange={(event) => setFilters((current) => ({ ...current, keyword: event.target.value }))}
                className="h-10 w-full rounded-2xl border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none transition-colors focus:border-[#233A7A]"
              />
            </label>

            <div id="workspace-work-time-history-search-wrap" className="flex items-end">
              <Button
                id="workspace-work-time-history-search"
                type="button"
                className="h-10 rounded-2xl bg-[#233A7A] px-4 text-sm"
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
                className="h-10 rounded-2xl px-4 text-sm"
                onClick={() => {
                  setFilters(initialFilters);
                  setAppliedFilters(initialFilters);
                }}
              >
                {text.reset}
              </Button>
            </div>

            <div id="workspace-work-time-history-export-wrap" className="flex items-end gap-2">
              <Button
                id="workspace-work-time-history-export-excel"
                type="button"
                variant="outline"
                className="h-10 rounded-2xl px-4 text-sm"
                onClick={() => void runExport("excel")}
              >
                {text.exportExcel}
              </Button>
              <Button
                id="workspace-work-time-history-export-pdf"
                type="button"
                variant="outline"
                className="h-10 rounded-2xl px-4 text-sm"
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
        actions={
          <div id="workspace-work-time-history-total" className="text-sm font-medium text-slate-500">
            {text.total} {history?.totCnt ?? 0} {text.rows}
          </div>
        }
        contentClassName="pt-5"
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

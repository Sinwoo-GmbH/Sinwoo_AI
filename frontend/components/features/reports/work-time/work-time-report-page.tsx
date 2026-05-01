"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  ATTENDANCE_API_PATH,
  ATTENDANCE_REPORT_API_PATH,
  type AttendanceCalendarDayRes,
  type AttendanceWidgetRes,
  type AttendanceWorkTimeFilterOptionRes,
  type AttendanceWorkTimeFilterOptionsRes,
  type AttendanceWorkTimeHistoryListRes,
  type AttendanceWorkTimeHistoryRowRes,
} from "@/lib/api/attendance-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceWorkTimeHistoryMessages } from "@/lib/i18n/workspace-content";
import { cn } from "@/lib/utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type WorkTimeReportPageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  mode: "client" | "admin";
  onUnauthorized: () => void;
  onLoadingChange?: (loading: boolean) => void;
};

type MatrixRow = {
  key: string;
  no: number;
  empNo: string;
  empNm: string;
  deptNm: string;
  dayMap: Map<string, AttendanceWorkTimeHistoryRowRes>;
};

function getCurrentYearMonth() {
  return new Date().toISOString().slice(0, 7);
}

function toHistoryQueryString(yearMonth: string, locale: LoginLocale) {
  const params = new URLSearchParams();
  params.set("yearMonth", yearMonth);
  params.set("lang", locale);
  return params.toString();
}

function toExportQueryString(yearMonth: string, keyword: string, locale: LoginLocale) {
  const params = new URLSearchParams();
  params.set("yearMonth", yearMonth);
  if (keyword.trim()) {
    params.set("keyword", keyword.trim());
  }
  params.set("lang", locale);
  return params.toString();
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

function generateMonthDays(yearMonth: string): AttendanceCalendarDayRes[] {
  const [year, month] = yearMonth.split("-").map(Number);
  if (!year || !month) return [];

  const dayCount = new Date(year, month, 0).getDate();
  return Array.from({ length: dayCount }, (_, index) => {
    const dayNo = index + 1;
    const dt = `${yearMonth}-${String(dayNo).padStart(2, "0")}`;
    const date = new Date(`${dt}T00:00:00`);
    const dayOfWeek = date.getDay();
    return {
      dt,
      dayNo,
      inMonthYn: true,
      todayYn: false,
      weekendYn: dayOfWeek === 0 || dayOfWeek === 6,
      holidayYn: false,
      holidayNm: null,
      attndStsCd: "NONE",
      chkinTm: null,
      chkoutTm: null,
    };
  });
}

function normalizeText(value: string) {
  return value.trim().toLowerCase();
}

function resolveHistoryKey(row: AttendanceWorkTimeHistoryRowRes) {
  if (typeof row.empId === "number") {
    return `emp:${row.empId}`;
  }
  if (typeof row.usrId === "number") {
    return `usr:${row.usrId}`;
  }
  return `name:${row.empNm}`;
}

function resolveOptionKey(option: AttendanceWorkTimeFilterOptionRes) {
  return `emp:${option.refId}`;
}

function isSaturday(day: AttendanceCalendarDayRes) {
  return new Date(`${day.dt}T00:00:00`).getDay() === 6;
}

function isSunday(day: AttendanceCalendarDayRes) {
  return new Date(`${day.dt}T00:00:00`).getDay() === 0;
}

function resolveMatrixMarker(row: AttendanceWorkTimeHistoryRowRes | undefined, day: AttendanceCalendarDayRes) {
  if (day.holidayYn) return "";
  if (!row?.attndStsCd) return "";
  if (row.attndStsCd === "LEAVE") return "U";
  if (row.attndStsCd === "BUSINESS_TRIP") return "B";
  return "";
}

function resolveCellTitle(row: AttendanceWorkTimeHistoryRowRes | undefined, day: AttendanceCalendarDayRes) {
  const parts: string[] = [];
  if (day.holidayYn && day.holidayNm) {
    parts.push(day.holidayNm);
  }
  if (row?.attndStsNm) {
    parts.push(row.attndStsNm);
  }
  if (row?.chkinTm || row?.chkoutTm) {
    parts.push([row.chkinTm, row.chkoutTm].filter(Boolean).join(" - "));
  }
  return parts.join(" / ");
}

function rowMatchesKeyword(row: MatrixRow, keyword: string) {
  if (!keyword) return true;
  const employeeText = [row.empNo, row.empNm, row.deptNm].join(" ").toLowerCase();
  if (employeeText.includes(keyword)) return true;

  for (const entry of row.dayMap.values()) {
    const dayText = [
      entry.attndDt,
      entry.attndStsNm,
      entry.chkinTm,
      entry.chkoutTm,
      entry.empNo,
      entry.empNm,
      entry.deptNm,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
    if (dayText.includes(keyword)) {
      return true;
    }
  }

  return false;
}

export function WorkTimeReportPage({
  accessToken,
  locale,
  mode,
  onUnauthorized,
  onLoadingChange,
}: WorkTimeReportPageProps) {
  const text = getWorkspaceWorkTimeHistoryMessages(locale);
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [keyword, setKeyword] = useState("");
  const [options, setOptions] = useState<AttendanceWorkTimeFilterOptionsRes | null>(null);
  const [history, setHistory] = useState<AttendanceWorkTimeHistoryListRes | null>(null);
  const [calendarDays, setCalendarDays] = useState<AttendanceCalendarDayRes[]>([]);

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

    const payload = (await response.json()) as AttendanceWorkTimeFilterOptionsRes;
    setOptions(payload);
  }, [accessToken, onUnauthorized]);

  const fetchHistory = useCallback(
    async (targetYearMonth: string) => {
      if (!accessToken) {
        onUnauthorized();
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}${ATTENDANCE_REPORT_API_PATH}/history?${toHistoryQueryString(targetYearMonth, locale)}`,
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

      const payload = (await response.json()) as AttendanceWorkTimeHistoryListRes;
      setHistory(payload);
    },
    [accessToken, locale, onUnauthorized]
  );

  const fetchMonthMeta = useCallback(
    async (targetYearMonth: string) => {
      if (!accessToken) {
        onUnauthorized();
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}${ATTENDANCE_API_PATH}/my/widget?yearMonth=${encodeURIComponent(targetYearMonth)}`,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Accept-Language": locale,
          },
          cache: "no-store",
        }
      );

      if (response.status === 401) {
        onUnauthorized();
        return;
      }

      if (!response.ok) {
        setCalendarDays(generateMonthDays(targetYearMonth));
        return;
      }

      const payload = (await response.json()) as AttendanceWidgetRes;
      setCalendarDays(payload.dayList.filter((item) => item.inMonthYn));
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
          `${API_BASE_URL}${ATTENDANCE_REPORT_API_PATH}/history/export/${format}?${toExportQueryString(yearMonth, keyword, locale)}`,
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
        const fallbackName = `work-time-${yearMonth}.${format === "excel" ? "xlsx" : "pdf"}`;
        downloadBlob(blob, extractFileName(response.headers.get("content-disposition"), fallbackName));
      });
    },
    [accessToken, keyword, locale, onUnauthorized, runWithLoading, yearMonth]
  );

  useEffect(() => {
    void runWithLoading(async () => {
      await fetchFilterOptions();
    });
  }, [fetchFilterOptions, runWithLoading]);

  useEffect(() => {
    void runWithLoading(async () => {
      await Promise.all([fetchHistory(yearMonth), fetchMonthMeta(yearMonth)]);
    });
  }, [fetchHistory, fetchMonthMeta, runWithLoading, yearMonth]);

  const visibleDays = useMemo(
    () => (calendarDays.length ? calendarDays : generateMonthDays(yearMonth)),
    [calendarDays, yearMonth]
  );

  const matrixRows = useMemo(() => {
    const rowsByKey = new Map<
      string,
      Omit<MatrixRow, "no"> & {
        sortKey: string;
      }
    >();

    if (options?.empList?.length) {
      for (const option of options.empList) {
        const key = resolveOptionKey(option);
        rowsByKey.set(key, {
          key,
          empNo: option.refCd ?? "",
          empNm: option.refNm ?? "",
          deptNm: option.refSubNm ?? "",
          dayMap: new Map(),
          sortKey: `${option.refNm ?? ""}|${option.refCd ?? ""}`,
        });
      }
    }

    for (const entry of history?.itemList ?? []) {
      const key = resolveHistoryKey(entry);
      const current = rowsByKey.get(key) ?? {
        key,
        empNo: entry.empNo ?? "",
        empNm: entry.empNm ?? "",
        deptNm: entry.deptNm ?? "",
        dayMap: new Map<string, AttendanceWorkTimeHistoryRowRes>(),
        sortKey: `${entry.empNm ?? ""}|${entry.empNo ?? ""}`,
      };

      current.empNo = current.empNo || entry.empNo || "";
      current.empNm = current.empNm || entry.empNm || "";
      current.deptNm = current.deptNm || entry.deptNm || "";
      current.dayMap.set(entry.attndDt, entry);
      rowsByKey.set(key, current);
    }

    if (!rowsByKey.size && history?.itemList?.length) {
      for (const entry of history.itemList) {
        const key = resolveHistoryKey(entry);
        rowsByKey.set(key, {
          key,
          empNo: entry.empNo ?? "",
          empNm: entry.empNm ?? "",
          deptNm: entry.deptNm ?? "",
          dayMap: new Map([[entry.attndDt, entry]]),
          sortKey: `${entry.empNm ?? ""}|${entry.empNo ?? ""}`,
        });
      }
    }

    const normalizedKeyword = normalizeText(keyword);

    return Array.from(rowsByKey.values())
      .sort((left, right) => left.sortKey.localeCompare(right.sortKey))
      .filter((row) => rowMatchesKeyword({ ...row, no: 0 }, normalizedKeyword))
      .map((row, index) => ({
        key: row.key,
        no: index + 1,
        empNo: row.empNo,
        empNm: row.empNm,
        deptNm: row.deptNm,
        dayMap: row.dayMap,
      }));
  }, [history, keyword, options]);

  const scopeLabel =
    history?.ownOnlyYn || options?.ownOnlyYn ? text.ownOnly : text.scopedAll;

  return (
    <div
      id="workspace-work-time-history-page"
      className="flex h-full min-h-0 flex-col rounded-[4px] border border-slate-300 bg-[#f8f9fb]"
    >
      <div className="border-b border-slate-300 px-3 py-2">
        <div className="flex flex-col gap-2 xl:flex-row xl:items-start xl:justify-between">
          <div className="min-w-0">
            <div className="text-[9px] uppercase tracking-[0.12em] text-slate-400">
              {text.eyebrow}
            </div>
            <h1
              id="workspace-work-time-history-title"
              className="mt-0.5 text-[14px] font-semibold leading-4 text-slate-900"
            >
              Working Time List
            </h1>
            <p className="mt-0.5 text-[10px] leading-4 text-slate-500">
              {text.description}
            </p>
          </div>

          <div className="grid gap-1 sm:grid-cols-3 xl:min-w-[420px]">
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">
                {text.currentScope}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-800">
                {mode === "admin" ? text.modeAdmin : text.modeClient}
              </div>
            </div>
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">
                {text.employee}
              </div>
              <div className="mt-0.5 truncate text-[11px] font-semibold leading-4 text-slate-800">
                {scopeLabel}
              </div>
            </div>
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">
                {text.total}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-800">
                {matrixRows.length} {text.rows}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="border-b border-slate-300 bg-[#f3f5f8] px-3 py-2">
        <div className="flex flex-col gap-2 xl:flex-row xl:items-end">
          <div className="flex shrink-0 items-end gap-1">
            <Button
              id="workspace-work-time-history-export-excel"
              type="button"
              size="sm"
              className="h-7 rounded-[3px] bg-[#2f5b96] px-2.5 text-[10px] font-medium text-white hover:bg-[#274d7e]"
              onClick={() => void runExport("excel")}
            >
              Excel
            </Button>
            <Button
              id="workspace-work-time-history-export-pdf"
              type="button"
              variant="outline"
              size="sm"
              className="h-7 rounded-[3px] border-slate-300 bg-white px-2.5 text-[10px] font-medium text-slate-700 hover:bg-slate-50"
              onClick={() => void runExport("pdf")}
            >
              PDF
            </Button>
          </div>

          <div className="grid flex-1 gap-3 sm:grid-cols-2 xl:grid-cols-[170px_minmax(0,1fr)]">
            <label className="space-y-1">
              <span className="text-[9px] leading-3 text-slate-500">{text.month}</span>
              <input
                id="workspace-work-time-history-month"
                type="month"
                value={yearMonth}
                onChange={(event) => setYearMonth(event.target.value)}
                className="h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[11px] leading-4 text-slate-700 outline-none transition-colors focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
              />
            </label>

            <label className="space-y-1">
              <span className="text-[9px] leading-3 text-slate-500">{text.keyword}</span>
              <input
                id="workspace-work-time-history-keyword"
                type="text"
                value={keyword}
                placeholder={text.keywordPlaceholder}
                onChange={(event) => setKeyword(event.target.value)}
                className="h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[11px] leading-4 text-slate-700 outline-none transition-colors focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
              />
            </label>
          </div>
        </div>
      </div>

      <div className="min-h-0 flex-1 p-2">
        <div className="flex h-full min-h-0 flex-col overflow-hidden rounded-[3px] border border-slate-300 bg-white">
          <div className="flex h-8 items-center justify-between border-b border-slate-300 bg-[#eef1f4] px-3 text-[10px] text-slate-500">
            <div className="font-medium text-slate-600">{text.resultsTitle}</div>
            <div className="whitespace-nowrap">
              {mode === "admin" ? text.modeAdmin : text.modeClient} · {scopeLabel}
            </div>
          </div>

          <div id="workspace-work-time-history-table-wrap" className="min-h-0 flex-1 overflow-auto">
            <table
              id="workspace-work-time-history-table"
              className="min-w-max border-collapse text-[11px] text-slate-700"
            >
              <thead className="sticky top-0 z-30">
                <tr className="bg-[#eef1f4] text-[9px] font-semibold uppercase tracking-[0.04em] text-slate-600">
                  <th
                    className="sticky left-0 z-30 min-w-[48px] border border-slate-300 bg-[#eef1f4] px-2 py-1.5 text-center"
                    style={{ left: 0 }}
                  >
                    No
                  </th>
                  <th
                    className="sticky z-30 min-w-[92px] border border-slate-300 bg-[#eef1f4] px-2 py-1.5 text-center"
                    style={{ left: 48 }}
                  >
                    ID
                  </th>
                  <th
                    className="sticky z-30 min-w-[160px] border border-slate-300 bg-[#eef1f4] px-2 py-1.5 text-left"
                    style={{ left: 140 }}
                  >
                    Name
                  </th>
                  <th
                    className="sticky z-30 min-w-[180px] border border-slate-300 bg-[#eef1f4] px-2 py-1.5 text-left"
                    style={{ left: 300 }}
                  >
                    Dept Name
                  </th>
                  {visibleDays.map((day) => (
                    <th
                      key={day.dt}
                      className={cn(
                        "min-w-[40px] border border-slate-300 px-1 py-1.5 text-center",
                        day.holidayYn
                          ? "bg-[#d96b6b] text-white"
                          : isSunday(day)
                            ? "bg-[#f6ebe6]"
                            : isSaturday(day)
                              ? "bg-[#e4ebf7]"
                              : "bg-[#eef1f4]"
                      )}
                    >
                      {String(day.dayNo).padStart(2, "0")}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {matrixRows.length ? (
                  matrixRows.map((row) => (
                    <tr key={row.key} className="odd:bg-white even:bg-slate-50/30 hover:bg-slate-50/60">
                      <td
                        className="sticky z-20 border border-slate-300 bg-inherit px-2 py-1.5 text-center text-[10px] leading-4"
                        style={{ left: 0 }}
                      >
                        {row.no}
                      </td>
                      <td
                        className="sticky z-20 border border-slate-300 bg-inherit px-2 py-1.5 text-center text-[10px] leading-4 text-[#215db0]"
                        style={{ left: 48 }}
                      >
                        {row.empNo || "-"}
                      </td>
                      <td
                        className="sticky z-20 border border-slate-300 bg-inherit px-2 py-1.5 text-[10px] leading-4"
                        style={{ left: 140 }}
                      >
                        {row.empNm || "-"}
                      </td>
                      <td
                        className="sticky z-20 border border-slate-300 bg-inherit px-2 py-1.5 text-[10px] leading-4"
                        style={{ left: 300 }}
                      >
                        {row.deptNm || "-"}
                      </td>
                      {visibleDays.map((day) => {
                        const entry = row.dayMap.get(day.dt);
                        const marker = resolveMatrixMarker(entry, day);
                        return (
                          <td
                            key={`${row.key}-${day.dt}`}
                            title={resolveCellTitle(entry, day)}
                            className={cn(
                              "h-[30px] min-w-[40px] border border-slate-300 px-1 text-center align-middle text-[10px] font-semibold leading-4",
                              day.holidayYn
                                ? "bg-[#d96b6b] text-white"
                                : isSunday(day)
                                  ? "bg-[#f6ebe6]"
                                  : isSaturday(day)
                                    ? "bg-[#e4ebf7]"
                                    : "bg-white"
                            )}
                          >
                            {marker}
                          </td>
                        );
                      })}
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td
                      colSpan={4 + visibleDays.length}
                      className="px-6 py-12 text-center text-[11px] text-slate-500"
                    >
                      <div className="text-[13px] font-semibold text-slate-700">
                        {text.noDataTitle}
                      </div>
                      <div className="mt-1.5 leading-5">{text.noDataDescription}</div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

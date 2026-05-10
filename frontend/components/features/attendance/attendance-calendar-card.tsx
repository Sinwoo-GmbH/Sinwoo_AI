"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, Loader2, LogIn, LogOut } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader } from "@/components/ui/card";
import type { CommonCodeListRes } from "@/lib/api/common-code-contract";
import { COMMON_CODE_API_PATH } from "@/lib/api/common-code-contract";
import type {
  AttendanceCalendarDayRes,
  AttendanceManualEntryRequest,
  AttendanceWidgetRes,
} from "@/lib/api/attendance-contract";
import { ATTENDANCE_API_PATH } from "@/lib/api/attendance-contract";
import { formatAttendanceTimeForDisplay } from "@/lib/attendance-time";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceAttendanceMessages } from "@/lib/i18n/workspace-content";
import { cn } from "@/lib/utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type AttendanceCalendarCardProps = {
  accessToken: string | null;
  locale: LoginLocale;
  onUnauthorized: () => void;
  onLoadingChange: (loading: boolean) => void;
};

function toLocaleTag(locale: LoginLocale) {
  if (locale === "de") return "de-DE";
  if (locale === "ko") return "ko-KR";
  return "en-US";
}

function shiftYearMonth(value: string, delta: number) {
  const [year, month] = value.split("-").map(Number);
  if (!year || !month) {
    return value;
  }
  const next = new Date(Date.UTC(year, month - 1 + delta, 1));
  const nextYear = next.getUTCFullYear();
  const nextMonth = String(next.getUTCMonth() + 1).padStart(2, "0");
  return `${nextYear}-${nextMonth}`;
}

function formatMonthLabel(yearMonth: string, locale: LoginLocale) {
  if (!yearMonth) {
    return "";
  }

  const [year, month] = yearMonth.split("-").map(Number);
  if (!year || !month) {
    return yearMonth;
  }

  return new Intl.DateTimeFormat(toLocaleTag(locale), {
    year: "numeric",
    month: "long",
  }).format(new Date(Date.UTC(year, month - 1, 1)));
}

function attendanceDot(colorClassName: string) {
  return <span className={cn("block h-1.5 w-1.5 rounded-full", colorClassName)} />;
}

function resolveCodeDisplayName(code: string | null | undefined, namesByCode: Record<string, string>) {
  if (!code) return "";
  return namesByCode[code] || code;
}

function isStatusDay(day: AttendanceCalendarDayRes, statusCd?: string | null) {
  return Boolean(statusCd) && day.attndStsCd === statusCd;
}

export function AttendanceCalendarCard({
  accessToken,
  locale,
  onUnauthorized,
  onLoadingChange,
}: AttendanceCalendarCardProps) {
  const [yearMonth, setYearMonth] = useState("");
  const [widget, setWidget] = useState<AttendanceWidgetRes | null>(null);
  const [loading, setLoading] = useState(true);
  const [manualSaving, setManualSaving] = useState<"check-in" | "check-out" | null>(null);
  const [manualDate, setManualDate] = useState("");
  const [manualCheckIn, setManualCheckIn] = useState("");
  const [manualCheckOut, setManualCheckOut] = useState("");
  const [attndFlagNmByCd, setAttndFlagNmByCd] = useState<Record<string, string>>({});

  const messages = getWorkspaceAttendanceMessages(locale);
  const weekdayLabels = messages.weekdays as string[];
  const monthLabel = useMemo(() => formatMonthLabel(yearMonth, locale), [locale, yearMonth]);
  const widgetPolicy = widget?.policy ?? null;
  const leaveStsCd = widgetPolicy?.leaveStsCd ?? "";
  const bizTripStsCd = widgetPolicy?.bizTripStsCd ?? "";
  const leaveChipLabel = resolveCodeDisplayName(leaveStsCd, attndFlagNmByCd);
  const bizTripChipLabel = resolveCodeDisplayName(bizTripStsCd, attndFlagNmByCd);

  const loadWidget = useCallback(
    async (targetYearMonth?: string) => {
      if (!accessToken) {
        setLoading(false);
        setWidget(null);
        return;
      }

      setLoading(true);
      onLoadingChange(true);

      try {
        const query = targetYearMonth
          ? `?yearMonth=${encodeURIComponent(targetYearMonth)}`
          : "";
        const response = await fetch(`${API_BASE_URL}${ATTENDANCE_API_PATH}/my/widget${query}`, {
          method: "GET",
          headers: {
            Accept: "application/json",
            Authorization: `Bearer ${accessToken}`,
            "Accept-Language": locale,
          },
          cache: "no-store",
        });

        if (response.status === 401) {
          onUnauthorized();
          return;
        }

        if (!response.ok) {
          throw new Error(`Attendance widget failed: ${response.status}`);
        }

        const payload = (await response.json()) as AttendanceWidgetRes;
        setWidget(payload);
        setYearMonth(payload.yearMonth);
      } catch {
        // Keep the sub-widget quiet. We deliberately avoid a large error box here.
      } finally {
        setLoading(false);
        onLoadingChange(false);
      }
    },
    [accessToken, locale, onLoadingChange, onUnauthorized]
  );

  useEffect(() => {
    void loadWidget(yearMonth || undefined);
  }, [loadWidget, yearMonth]);

  useEffect(() => {
    if (!widget) return;

    setManualDate((current) => current || widget.today.attndDt || "");
    setManualCheckIn((current) => current || widget.policy?.dfltChkinTm || "");
    setManualCheckOut((current) => current || widget.policy?.dfltChkoutTm || "");
  }, [widget]);

  useEffect(() => {
    if (!widgetPolicy?.attndFlagGrpCd) {
      setAttndFlagNmByCd({});
      return;
    }

    let cancelled = false;

    const loadCommonCodes = async () => {
      try {
        const response = await fetch(
          `${API_BASE_URL}${COMMON_CODE_API_PATH}?grpCd=${encodeURIComponent(widgetPolicy.attndFlagGrpCd)}`,
          {
            method: "GET",
            headers: {
              Accept: "application/json",
              "Accept-Language": locale,
              ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
            },
            cache: "no-store",
          }
        );

        if (!response.ok) {
          throw new Error(`Common code fetch failed: ${response.status}`);
        }

        const payload = (await response.json()) as CommonCodeListRes;
        if (cancelled) return;

        setAttndFlagNmByCd(
          Object.fromEntries(
            payload.itemList.map((item) => [item.cd, item.dspCdNm || item.cdNmEn || item.cd])
          )
        );
      } catch {
        if (!cancelled) {
          setAttndFlagNmByCd({});
        }
      }
    };

    void loadCommonCodes();

    return () => {
      cancelled = true;
    };
  }, [accessToken, locale, widgetPolicy?.attndFlagGrpCd]);

  const saveManualEntry = useCallback(
    async (type: "check-in" | "check-out") => {
      if (!accessToken || !manualDate) return;

      const payload: AttendanceManualEntryRequest =
        type === "check-in"
          ? {
              attndDt: manualDate,
              chkinTm: manualCheckIn || null,
              chkoutTm: null,
            }
          : {
              attndDt: manualDate,
              chkinTm: null,
              chkoutTm: manualCheckOut || null,
            };

      if (type === "check-in" && !manualCheckIn) return;
      if (type === "check-out" && !manualCheckOut) return;

      setManualSaving(type);
      onLoadingChange(true);

      try {
        const response = await fetch(`${API_BASE_URL}${ATTENDANCE_API_PATH}/my/manual-entry`, {
          method: "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
            "Accept-Language": locale,
          },
          body: JSON.stringify(payload),
        });

        if (response.status === 401) {
          onUnauthorized();
          return;
        }

        if (!response.ok) {
          throw new Error(`Attendance manual entry failed: ${response.status}`);
        }

        const nextMonth = manualDate.slice(0, 7);
        if (nextMonth && nextMonth !== yearMonth) {
          setYearMonth(nextMonth);
          await loadWidget(nextMonth);
        } else {
          await loadWidget(yearMonth || undefined);
        }
      } catch {
        // Keep the sub-widget minimal; no large inline error panel.
      } finally {
        setManualSaving(null);
        onLoadingChange(false);
      }
    },
    [
      accessToken,
      loadWidget,
      locale,
      manualCheckIn,
      manualCheckOut,
      manualDate,
      onLoadingChange,
      onUnauthorized,
      yearMonth,
    ]
  );

  return (
    <Card id="workspace-rules-card" className="border-[#D7E2F5]">
      <CardHeader className="pb-1">
        <div className="flex items-start justify-between gap-3">
          <div>
            <CardDescription
              id="workspace-rules-eyebrow"
              className="flex items-center gap-1.5 uppercase tracking-[0.22em] text-[#4F72C8]"
            >
              <CalendarDays className="h-3.5 w-3.5" />
              {messages.eyebrow as string}
            </CardDescription>
          </div>
          <div
            id="workspace-attendance-month-nav"
            className="flex items-center gap-1 rounded-2xl border border-slate-200 bg-slate-50 px-1.5 py-1"
          >
            <Button
              id="workspace-attendance-month-prev"
              type="button"
              variant="ghost"
              size="icon"
              className="h-7 w-7 rounded-xl text-slate-500 hover:bg-white"
              disabled={!yearMonth}
              onClick={() => setYearMonth((current) => shiftYearMonth(current, -1))}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <div className="px-1.5 text-[12px] font-medium text-slate-700">{monthLabel}</div>
            <Button
              id="workspace-attendance-month-next"
              type="button"
              variant="ghost"
              size="icon"
              className="h-7 w-7 rounded-xl text-slate-500 hover:bg-white"
              disabled={!yearMonth}
              onClick={() => setYearMonth((current) => shiftYearMonth(current, 1))}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-1.5 pt-0">
        <div
          id="workspace-attendance-summary"
          className="grid gap-1.5 rounded-[14px] border border-slate-200/80 bg-slate-50/85 px-2 py-1.5 lg:grid-cols-[0.9fr_1fr_1fr]"
        >
          <label id="workspace-attendance-manual-date-wrap" className="grid gap-1 text-[11px] text-slate-500">
            <span>{messages.manualDate as string}</span>
            <input
              id="workspace-attendance-manual-date"
              type="date"
              value={manualDate}
              onChange={(event) => setManualDate(event.target.value)}
              className="h-6 rounded-xl border border-slate-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#4F72C8]"
            />
          </label>

          <div
            id="workspace-attendance-check-in-section"
            className="grid gap-1 rounded-xl border border-blue-200 bg-blue-50/60 px-2 py-1.5"
          >
            <span className="text-[11px] font-medium text-blue-700">{messages.manualCheckIn as string}</span>
            <div className="flex items-center gap-1.5">
              <input
                id="workspace-attendance-manual-check-in"
                type="time"
                value={manualCheckIn}
                onChange={(event) => setManualCheckIn(event.target.value)}
                className="h-6 min-w-0 flex-1 rounded-xl border border-blue-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#4F72C8]"
              />
              <Button
                id="workspace-attendance-manual-save-in"
                type="button"
                onClick={() => void saveManualEntry("check-in")}
                disabled={manualSaving !== null || !manualDate || !manualCheckIn}
                className="h-6 rounded-xl bg-[#23468F] px-2 text-[10px] text-white hover:bg-[#1D3975]"
              >
                {manualSaving === "check-in" ? (
                  <Loader2 className="mr-1 h-3 w-3 animate-spin" />
                ) : (
                  <LogIn className="mr-1 h-3 w-3" />
                )}
                {messages.manualSaveIn as string}
              </Button>
            </div>
          </div>

          <div
            id="workspace-attendance-check-out-section"
            className="grid gap-1 rounded-xl border border-rose-200 bg-rose-50/60 px-2 py-1.5"
          >
            <span className="text-[11px] font-medium text-rose-700">{messages.manualCheckOut as string}</span>
            <div className="flex items-center gap-1.5">
              <input
                id="workspace-attendance-manual-check-out"
                type="time"
                value={manualCheckOut}
                onChange={(event) => setManualCheckOut(event.target.value)}
                className="h-6 min-w-0 flex-1 rounded-xl border border-rose-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#E35D6A]"
              />
              <Button
                id="workspace-attendance-manual-save-out"
                type="button"
                variant="outline"
                onClick={() => void saveManualEntry("check-out")}
                disabled={manualSaving !== null || !manualDate || !manualCheckOut}
                className="h-6 rounded-xl border-rose-200 bg-white px-2 text-[10px] text-rose-700 hover:bg-rose-100"
              >
                {manualSaving === "check-out" ? (
                  <Loader2 className="mr-1 h-3 w-3 animate-spin" />
                ) : (
                  <LogOut className="mr-1 h-3 w-3" />
                )}
                {messages.manualSaveOut as string}
              </Button>
            </div>
          </div>
        </div>

        {loading ? (
          <div
            id="workspace-attendance-loading"
            className="flex min-h-[140px] items-center justify-center rounded-[14px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500"
          >
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            {messages.loading as string}
          </div>
        ) : (
          <div
            id="workspace-attendance-calendar"
            className="rounded-[14px] border border-slate-200/80 bg-white px-2 py-1.5"
          >
            <div
              id="workspace-attendance-weekdays"
              className="mb-1 grid grid-cols-7 gap-1 text-center text-[10px] font-medium uppercase tracking-[0.12em] text-slate-400"
            >
              {weekdayLabels.map((weekday) => (
                <div key={weekday}>{weekday}</div>
              ))}
            </div>
            <div id="workspace-attendance-calendar-grid" className="grid grid-cols-7 gap-1">
              {widget?.dayList.map((day) => {
                const checkInTime = formatAttendanceTimeForDisplay(day.chkinTm);
                const checkOutTime = formatAttendanceTimeForDisplay(day.chkoutTm);

                return (
                  <div
                    id={`workspace-attendance-day-${day.dt}`}
                    key={day.dt}
                    className={cn(
                      "min-h-[38px] rounded-[12px] border px-1.5 py-1 text-left transition-colors",
                      day.inMonthYn ? "border-slate-200 bg-white" : "border-transparent bg-slate-50/70 text-slate-300",
                      day.todayYn ? "ring-1 ring-[#4F72C8]" : "",
                      day.holidayYn ? "border-rose-200 bg-rose-50/70" : ""
                    )}
                  >
                    <div className="flex items-center justify-between">
                      <span className={cn("text-xs font-semibold", day.todayYn ? "text-[#18397E]" : "text-slate-700")}>
                        {day.dayNo}
                      </span>
                      {day.holidayYn ? <span className="h-1.5 w-1.5 rounded-full bg-rose-500" /> : null}
                    </div>

                    <div className="mt-1 space-y-0.5">
                      {checkInTime ? (
                        <div className="flex items-center gap-1 text-[10px] font-medium text-[#18397E]">
                          {attendanceDot("bg-blue-500")}
                          <span>{checkInTime}</span>
                        </div>
                      ) : null}

                      {checkOutTime ? (
                        <div className="flex items-center gap-1 text-[10px] font-medium text-slate-700">
                          {attendanceDot("bg-rose-500")}
                          <span>{checkOutTime}</span>
                        </div>
                      ) : null}

                      {isStatusDay(day, leaveStsCd) ? (
                        <div
                          id={`workspace-attendance-leave-${day.dt}`}
                          className="inline-flex rounded-lg bg-violet-100 px-1.5 py-0.5 text-[10px] font-medium text-violet-700"
                        >
                          {leaveChipLabel}
                        </div>
                      ) : null}

                      {isStatusDay(day, bizTripStsCd) ? (
                        <div
                          id={`workspace-attendance-trip-${day.dt}`}
                          className="inline-flex rounded-lg bg-amber-100 px-1.5 py-0.5 text-[10px] font-medium text-amber-700"
                        >
                          {bizTripChipLabel}
                        </div>
                      ) : null}

                      {!checkInTime && !isStatusDay(day, leaveStsCd) && day.holidayYn && day.holidayNm ? (
                        <div className="line-clamp-2 text-[10px] leading-4 text-rose-600">{day.holidayNm}</div>
                      ) : null}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

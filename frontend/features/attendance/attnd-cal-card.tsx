"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, Loader2, LogIn, LogOut } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardCnt, CardDesc, CardHeader } from "@/components/ui/card";
import type { CommonCdListResponse } from "@/lib/api/common-cd-contract";
import { COMMON_CD_API_PATH } from "@/lib/api/common-cd-contract";
import type {
  AttndCalDayResponse,
  AttndManualEntryRequest,
  AttndWidgetResponse,
} from "@/lib/api/attnd-contract";
import { ATTND_API_PATH } from "@/lib/api/attnd-contract";
import { formatAttndTimeForDisplay } from "@/lib/utils/attnd-time";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspAttndMsgs } from "@/lib/i18n/wsp-cnt";
import { cn } from "@/lib/utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type AttndCalCardProps = {
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

function resolveCdDspNm(code: string | null | undefined, namesByCd: Record<string, string>) {
  if (!code) return "";
  return namesByCd[code] || code;
}

function isStatusDay(day: AttndCalDayResponse, statusCd?: string | null) {
  return Boolean(statusCd) && day.attndStsCd === statusCd;
}

export function AttndCalCard({
  accessToken,
  locale,
  onUnauthorized,
  onLoadingChange,
}: AttndCalCardProps) {
  const [yearMonth, setYearMonth] = useState("");
  const [widget, setWidget] = useState<AttndWidgetResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [manualSaving, setManualSaving] = useState<"check-in" | "check-out" | null>(null);
  const [manualDate, setManualDate] = useState("");
  const [manualCheckIn, setManualCheckIn] = useState("");
  const [manualCheckOut, setManualCheckOut] = useState("");
  const [attndFlagNmByCd, setAttndFlagNmByCd] = useState<Record<string, string>>({});

  const msgs = getWspAttndMsgs(locale);
  const weekdayLabels = msgs.weekdays as string[];
  const monthLabel = useMemo(() => formatMonthLabel(yearMonth, locale), [locale, yearMonth]);
  const widgetPolicy = widget?.policy ?? null;
  const leaveStsCd = widgetPolicy?.leaveStsCd ?? "";
  const bizTripStsCd = widgetPolicy?.bizTripStsCd ?? "";
  const leaveChipLabel = resolveCdDspNm(leaveStsCd, attndFlagNmByCd);
  const bizTripChipLabel = resolveCdDspNm(bizTripStsCd, attndFlagNmByCd);

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
        const response = await fetch(`${API_BASE_URL}${ATTND_API_PATH}/my/widget${query}`, {
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

        const payload = (await response.json()) as AttndWidgetResponse;
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

    const loadCommonCds = async () => {
      try {
        const response = await fetch(
          `${API_BASE_URL}${COMMON_CD_API_PATH}?grpCd=${encodeURIComponent(widgetPolicy.attndFlagGrpCd)}`,
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

        const payload = (await response.json()) as CommonCdListResponse;
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

    void loadCommonCds();

    return () => {
      cancelled = true;
    };
  }, [accessToken, locale, widgetPolicy?.attndFlagGrpCd]);

  const saveManualEntry = useCallback(
    async (type: "check-in" | "check-out") => {
      if (!accessToken || !manualDate) return;

      const payload: AttndManualEntryRequest =
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
        const response = await fetch(`${API_BASE_URL}${ATTND_API_PATH}/my/manual-entry`, {
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
    <Card id="wsp-rules-card" className="border-[#D7E2F5]">
      <CardHeader className="pb-1">
        <div className="flex items-start justify-between gap-3">
          <div>
            <CardDesc
              id="wsp-rules-eyebrow"
              className="flex items-center gap-1.5 uppercase tracking-[0.22em] text-[#4F72C8]"
            >
              <CalendarDays className="h-3.5 w-3.5" />
              {msgs.eyebrow as string}
            </CardDesc>
          </div>
          <div
            id="wsp-attnd-month-nav"
            className="flex items-center gap-1 rounded-2xl border border-slate-200 bg-slate-50 px-1.5 py-1"
          >
            <Button
              id="wsp-attnd-month-prev"
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
              id="wsp-attnd-month-next"
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

      <CardCnt className="space-y-1.5 pt-0">
        <div
          id="wsp-attnd-sum"
          className="grid gap-1.5 rounded-[14px] border border-slate-200/80 bg-slate-50/85 px-2 py-1.5 lg:grid-cols-[0.9fr_1fr_1fr]"
        >
          <label id="wsp-attnd-mnl-date-wrap" className="grid gap-1 text-[11px] text-slate-500">
            <span>{msgs.manualDate as string}</span>
            <input
              id="wsp-attnd-mnl-date"
              type="date"
              value={manualDate}
              onChange={(event) => setManualDate(event.target.value)}
              className="h-6 rounded-xl border border-slate-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#4F72C8]"
            />
          </label>

          <div
            id="wsp-attnd-chkin-sect"
            className="grid gap-1 rounded-xl border border-blue-200 bg-blue-50/60 px-2 py-1.5"
          >
            <span className="text-[11px] font-medium text-blue-700">{msgs.manualCheckIn as string}</span>
            <div className="flex items-center gap-1.5">
              <input
                id="wsp-attnd-mnl-chkin"
                type="time"
                value={manualCheckIn}
                onChange={(event) => setManualCheckIn(event.target.value)}
                className="h-6 min-w-0 flex-1 rounded-xl border border-blue-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#4F72C8]"
              />
              <Button
                id="wsp-attnd-mnl-save-in"
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
                {msgs.manualSaveIn as string}
              </Button>
            </div>
          </div>

          <div
            id="wsp-attnd-chkout-sect"
            className="grid gap-1 rounded-xl border border-rose-200 bg-rose-50/60 px-2 py-1.5"
          >
            <span className="text-[11px] font-medium text-rose-700">{msgs.manualCheckOut as string}</span>
            <div className="flex items-center gap-1.5">
              <input
                id="wsp-attnd-mnl-chkout"
                type="time"
                value={manualCheckOut}
                onChange={(event) => setManualCheckOut(event.target.value)}
                className="h-6 min-w-0 flex-1 rounded-xl border border-rose-200 bg-white px-2 text-[11px] text-slate-700 outline-none focus:border-[#E35D6A]"
              />
              <Button
                id="wsp-attnd-mnl-save-out"
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
                {msgs.manualSaveOut as string}
              </Button>
            </div>
          </div>
        </div>

        {loading ? (
          <div
            id="wsp-attnd-loading"
            className="flex min-h-[140px] items-center justify-center rounded-[14px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500"
          >
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            {msgs.loading as string}
          </div>
        ) : (
          <div
            id="wsp-attnd-cal"
            className="rounded-[14px] border border-slate-200/80 bg-white px-2 py-1.5"
          >
            <div
              id="wsp-attnd-weekdays"
              className="mb-1 grid grid-cols-7 gap-1 text-center text-[10px] font-medium uppercase tracking-[0.12em] text-slate-400"
            >
              {weekdayLabels.map((weekday) => (
                <div key={weekday}>{weekday}</div>
              ))}
            </div>
            <div id="wsp-attnd-cal-grid" className="grid grid-cols-7 gap-1">
              {widget?.dayList.map((day) => {
                const checkInTime = formatAttndTimeForDisplay(day.chkinTm);
                const checkOutTime = formatAttndTimeForDisplay(day.chkoutTm);

                return (
                  <div
                    id={`wsp-attnd-day-${day.dt}`}
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
                          id={`wsp-attnd-leave-${day.dt}`}
                          className="inline-flex rounded-lg bg-violet-100 px-1.5 py-0.5 text-[10px] font-medium text-violet-700"
                        >
                          {leaveChipLabel}
                        </div>
                      ) : null}

                      {isStatusDay(day, bizTripStsCd) ? (
                        <div
                          id={`wsp-attnd-trip-${day.dt}`}
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
      </CardCnt>
    </Card>
  );
}

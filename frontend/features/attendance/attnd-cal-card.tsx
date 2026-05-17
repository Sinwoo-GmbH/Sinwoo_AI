"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Clock,
  Loader2,
  Save,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardCnt, CardDesc, CardHeader } from "@/components/ui/card";
import type { WrkTmListResponse, WrkTmResponse } from "@/lib/api/wrk-tm-contract";
import { WRK_TM_API } from "@/lib/api/wrk-tm-contract";
import type { RgnHolListResponse, RgnHolResponse } from "@/lib/api/hol-contract";
import { CO_HOL_API, RGN_HOL_API } from "@/lib/api/hol-contract";
import type { CoHolListResponse, CoHolResponse } from "@/lib/api/hol-contract";
import { formatAttndTimeForDisplay } from "@/lib/utils/attnd-time";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspAttndMsgs } from "@/lib/i18n/wsp-cnt";
import { toast } from "@/components/ui/toast";
import { cn } from "@/lib/utils";

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/* ── types ──────────────────────────────────────────────── */

type CalDay = {
  dt: string;
  dayNo: number;
  inMonth: boolean;
  today: boolean;
  weekend: boolean;
  saturday: boolean;
  sunday: boolean;
  holiday: boolean;
  holidayNm: string | null;
  wrkTm: WrkTmResponse | null;
};

type AttndCalCardProps = {
  accessToken: string | null;
  locale: LoginLocale;
  onUnauthorized: () => void;
  onLoadingChange: (loading: boolean) => void;
  /** MyWorkTimePage 등 외부에서 날짜 선택 콜백 */
  onDaySelect?: (dt: string, wrkTm: WrkTmResponse | null) => void;
  selectedDt?: string | null;
};

/* ── helpers ─────────────────────────────────────────────── */

function toLocaleTag(locale: LoginLocale) {
  if (locale === "de") return "de-DE";
  if (locale === "ko") return "ko-KR";
  return "en-US";
}

function todayStr() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function nowTimeShort() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function ymFromStr(ym: string) {
  const [y, m] = ym.split("-").map(Number);
  return { y: y ?? 2026, m: m ?? 1 };
}

function shiftYm(ym: string, delta: number) {
  const { y, m } = ymFromStr(ym);
  const d = new Date(Date.UTC(y, m - 1 + delta, 1));
  return `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, "0")}`;
}

function fmtMonthLabel(ym: string, locale: LoginLocale) {
  const { y, m } = ymFromStr(ym);
  return new Intl.DateTimeFormat(toLocaleTag(locale), { year: "numeric", month: "long" }).format(
    new Date(Date.UTC(y, m - 1, 1)),
  );
}

function buildCalGrid(
  ym: string,
  wrkTms: WrkTmResponse[],
  rgnHols: RgnHolResponse[],
  coHols: CoHolResponse[],
): CalDay[] {
  const { y, m } = ymFromStr(ym);
  const today = todayStr();

  const wtByDt = new Map<string, WrkTmResponse>();
  for (const wt of wrkTms) wtByDt.set(wt.workDt, wt);

  const holByDt = new Map<string, string>();
  for (const h of rgnHols) holByDt.set(h.holidayDt, h.holidayNm);
  for (const h of coHols) {
    const s = new Date(h.strDt);
    const e = new Date(h.endDt);
    for (let d = new Date(s); d <= e; d.setDate(d.getDate() + 1)) {
      const ds = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      holByDt.set(ds, h.holidayNm);
    }
  }

  const firstDay = new Date(Date.UTC(y, m - 1, 1));
  const lastDay = new Date(Date.UTC(y, m, 0));

  let startDow = firstDay.getUTCDay() - 1; // Mon=0
  if (startDow < 0) startDow = 6;

  const days: CalDay[] = [];

  const pushDay = (d: Date, inMonth: boolean) => {
    const ds = `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, "0")}-${String(d.getUTCDate()).padStart(2, "0")}`;
    const dow = d.getUTCDay();
    days.push({
      dt: ds,
      dayNo: d.getUTCDate(),
      inMonth,
      today: ds === today,
      weekend: dow === 0 || dow === 6,
      saturday: dow === 6,
      sunday: dow === 0,
      holiday: holByDt.has(ds),
      holidayNm: holByDt.get(ds) ?? null,
      wrkTm: wtByDt.get(ds) ?? null,
    });
  };

  // prev month fill
  for (let i = startDow - 1; i >= 0; i--) {
    pushDay(new Date(Date.UTC(y, m - 1, -i)), false);
  }
  // current month
  for (let d = 1; d <= lastDay.getUTCDate(); d++) {
    pushDay(new Date(Date.UTC(y, m - 1, d)), true);
  }
  // next month fill
  while (days.length < 42) {
    const idx = days.length - startDow - lastDay.getUTCDate();
    pushDay(new Date(Date.UTC(y, m, idx + 1)), false);
  }

  return days;
}

/* ── component ──────────────────────────────────────────── */

export function AttndCalCard({
  accessToken,
  locale,
  onUnauthorized,
  onLoadingChange,
  onDaySelect,
  selectedDt: externalSelectedDt,
}: AttndCalCardProps) {
  const today = todayStr();
  const initYm = today.slice(0, 7);

  const [ym, setYm] = useState(initYm);
  const [loading, setLoading] = useState(true);
  // saving state는 handleClockIn / handleClockOut 내부에서 관리
  const [wrkTms, setWrkTms] = useState<WrkTmResponse[]>([]);
  const [rgnHols, setRgnHols] = useState<RgnHolResponse[]>([]);
  const [coHols, setCoHols] = useState<CoHolResponse[]>([]);

  // 선택된 날짜 (외부 prop 또는 내부 state)
  const [internalSelectedDt, setInternalSelectedDt] = useState<string>(today);
  const selectedDt = externalSelectedDt ?? internalSelectedDt;

  // 편집 폼 (선택한 날짜의 출퇴근)
  const [editStrTm, setEditStrTm] = useState("");
  const [editEndTm, setEditEndTm] = useState("");

  const msgs = getWspAttndMsgs(locale);
  const weekdayLabels = msgs.weekdays;
  const monthLabel = useMemo(() => fmtMonthLabel(ym, locale), [ym, locale]);

  const fetchJson = useCallback(
    async <T,>(path: string, init?: RequestInit): Promise<T | null> => {
      if (!accessToken) { onUnauthorized(); return null; }
      const headers = new Headers(init?.headers);
      headers.set("Authorization", `Bearer ${accessToken}`);
      headers.set("Accept", "application/json");
      if (init?.body) headers.set("Content-Type", "application/json");
      const res = await fetch(`${API}${path}`, { ...init, headers });
      if (res.status === 401) { onUnauthorized(); return null; }
      if (res.status === 204) return null;
      if (!res.ok) throw new Error(`${res.status}`);
      const text = await res.text();
      if (!text || text === "null") return null;
      return JSON.parse(text) as T;
    },
    [accessToken, onUnauthorized],
  );

  /* ── 월 데이터 로드 (각 API 개별 try/catch — 하나 실패해도 나머지 표시) ── */
  const loadMonth = useCallback(
    async (targetYm: string) => {
      setLoading(true);
      onLoadingChange(true);
      try {
        const { y, m } = ymFromStr(targetYm);
        const from = `${y}-${String(m).padStart(2, "0")}-01`;
        const lastDate = new Date(Date.UTC(y, m, 0)).getUTCDate();
        const to = `${y}-${String(m).padStart(2, "0")}-${String(lastDate).padStart(2, "0")}`;

        // 개별 try/catch: 하나 실패해도 나머지 데이터는 정상 표시
        const wtPromise = fetchJson<WrkTmListResponse>(`${WRK_TM_API}/my?from=${from}&to=${to}`)
          .catch(() => null);
        const rhPromise = fetchJson<RgnHolListResponse>(`${RGN_HOL_API}/period?from=${from}&to=${to}`)
          .catch(() => null);
        const chPromise = fetchJson<CoHolListResponse>(`${CO_HOL_API}/period?yr=${y}&from=${from}&to=${to}`)
          .catch(() => null);

        const [wtRes, rhRes, chRes] = await Promise.all([wtPromise, rhPromise, chPromise]);

        setWrkTms(wtRes?.itemList ?? []);
        setRgnHols(rhRes?.itemList ?? []);
        setCoHols(chRes?.itemList ?? []);
      } catch {
        /* 전체 실패 시에도 빈 상태 유지 */
      } finally {
        setLoading(false);
        onLoadingChange(false);
      }
    },
    [fetchJson, onLoadingChange],
  );

  useEffect(() => { void loadMonth(ym); }, [loadMonth, ym]);

  const calDays = useMemo(() => buildCalGrid(ym, wrkTms, rgnHols, coHols), [ym, wrkTms, rgnHols, coHols]);

  // 날짜 클릭 핸들러 — 서버에서 해당일 출퇴근 데이터를 직접 조회
  const handleDayClick = useCallback(async (day: CalDay) => {
    if (!day.inMonth) return;
    setInternalSelectedDt(day.dt);

    // API로 해당 날짜 출퇴근 조회
    try {
      const wt = await fetchJson<WrkTmResponse>(`${WRK_TM_API}/date?workDt=${day.dt}`);
      setEditStrTm(wt?.strTm ? wt.strTm.slice(0, 5) : "");
      setEditEndTm(wt?.endTm ? wt.endTm.slice(0, 5) : "");
      onDaySelect?.(day.dt, wt);
    } catch {
      // 데이터 없으면 빈 폼
      setEditStrTm("");
      setEditEndTm("");
      onDaySelect?.(day.dt, null);
    }
  }, [fetchJson, onDaySelect]);

  // Now 버튼
  const handleNowStr = () => setEditStrTm(nowTimeShort());
  const handleNowEnd = () => setEditEndTm(nowTimeShort());

  // 출근 저장
  const [savingIn, setSavingIn] = useState(false);
  const handleClockIn = useCallback(async () => {
    if (!selectedDt || !editStrTm) return;
    setSavingIn(true);
    onLoadingChange(true);
    try {
      await fetchJson(`${WRK_TM_API}/clock-in`, {
        method: "POST",
        body: JSON.stringify({ workDt: selectedDt, strTm: `${editStrTm}:00` }),
      });
      await loadMonth(ym);
    } catch {
      toast.error(msgs.errSaveFailed);
    } finally { setSavingIn(false); onLoadingChange(false); }
  }, [selectedDt, editStrTm, fetchJson, loadMonth, ym, onLoadingChange, msgs]);

  // 퇴근 저장
  const [savingOut, setSavingOut] = useState(false);
  const handleClockOut = useCallback(async () => {
    if (!selectedDt || !editEndTm) return;
    // 출근 시간이 있으면 퇴근 < 출근 체크
    if (editStrTm && editEndTm <= editStrTm) {
      toast.error(msgs.errEndBeforeStr);
      return;
    }
    setSavingOut(true);
    onLoadingChange(true);
    try {
      await fetchJson(`${WRK_TM_API}/clock-out`, {
        method: "POST",
        body: JSON.stringify({ workDt: selectedDt, endTm: `${editEndTm}:00` }),
      });
      await loadMonth(ym);
    } catch {
      toast.error(msgs.errSaveFailed);
    } finally { setSavingOut(false); onLoadingChange(false); }
  }, [selectedDt, editStrTm, editEndTm, fetchJson, loadMonth, ym, onLoadingChange, msgs]);

  return (
    <Card className="border-[#D7E2F5]">
      <CardHeader className="pb-1">
        <div className="flex items-start justify-between gap-3">
          <CardDesc className="flex items-center gap-1.5 uppercase tracking-[0.22em] text-[#4F72C8]">
            <CalendarDays className="h-3.5 w-3.5" />
            {msgs.eyebrow}
          </CardDesc>
          <div className="flex items-center gap-1.5">
            {ym !== initYm && (
              <Button
                type="button"
                variant="outline"
                onClick={() => { setYm(initYm); setInternalSelectedDt(today); onDaySelect?.(today, null); }}
                className="h-7 rounded-xl border-slate-200 px-2 text-[11px] font-medium text-slate-600 hover:bg-slate-100"
              >
                {msgs.today}
              </Button>
            )}
            <div className="flex items-center gap-1 rounded-2xl border border-slate-200 bg-slate-50 px-1.5 py-1">
              <Button type="button" variant="ghost" size="icon" className="h-7 w-7 rounded-xl text-slate-500 hover:bg-white" onClick={() => setYm((c) => shiftYm(c, -1))}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <div className="px-1.5 text-[12px] font-medium text-slate-700">{monthLabel}</div>
              <Button type="button" variant="ghost" size="icon" className="h-7 w-7 rounded-xl text-slate-500 hover:bg-white" onClick={() => setYm((c) => shiftYm(c, 1))}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardCnt className="space-y-1.5 pt-0">
        {/* ── 선택된 날짜 출퇴근 편집 ── */}
        <div className="rounded-[14px] border border-slate-200/80 bg-slate-50/85 px-2.5 py-2">
          {/* 선택 날짜 표시 */}
          <div className="mb-1.5 text-[11px] font-semibold text-slate-600">
            {selectedDt === today ? `📅 ${msgs.today} (${selectedDt})` : `📅 ${selectedDt}`}
          </div>

          {/* Check-in / Check-out */}
          <div className="grid gap-2 lg:grid-cols-2">
            {/* Check-in */}
            <div className="rounded-xl border border-blue-200 bg-blue-50/60 px-2 py-1.5">
              <span className="text-[10px] font-medium text-blue-700">{msgs.manualCheckIn}</span>
              <div className="mt-1 flex items-center gap-1">
                <input
                  type="time"
                  value={editStrTm}
                  onChange={(e) => setEditStrTm(e.target.value)}
                  className="h-6 min-w-0 flex-1 rounded-lg border border-blue-200 bg-white px-1.5 text-[11px] text-slate-700 outline-none focus:border-[#4F72C8]"
                />
                <Button type="button" onClick={handleNowStr} className="h-6 shrink-0 rounded-lg bg-slate-500 px-1.5 text-[10px] text-white hover:bg-slate-600">
                  <Clock className="mr-0.5 h-3 w-3" /> Now
                </Button>
              </div>
              <div className="mt-1.5 flex justify-end">
                <Button
                  type="button"
                  onClick={() => void handleClockIn()}
                  disabled={savingIn || !editStrTm}
                  className="h-6 rounded-lg bg-[#23468F] px-2.5 text-[10px] text-white hover:bg-[#1D3975]"
                >
                  {savingIn ? <Loader2 className="mr-1 h-3 w-3 animate-spin" /> : <Save className="mr-1 h-3 w-3" />}
                  {msgs.manualSaveIn}
                </Button>
              </div>
            </div>

            {/* Check-out */}
            <div className="rounded-xl border border-rose-200 bg-rose-50/60 px-2 py-1.5">
              <span className="text-[10px] font-medium text-rose-700">{msgs.manualCheckOut}</span>
              <div className="mt-1 flex items-center gap-1">
                <input
                  type="time"
                  value={editEndTm}
                  onChange={(e) => setEditEndTm(e.target.value)}
                  className="h-6 min-w-0 flex-1 rounded-lg border border-rose-200 bg-white px-1.5 text-[11px] text-slate-700 outline-none focus:border-[#E35D6A]"
                />
                <Button type="button" onClick={handleNowEnd} className="h-6 shrink-0 rounded-lg bg-slate-500 px-1.5 text-[10px] text-white hover:bg-slate-600">
                  <Clock className="mr-0.5 h-3 w-3" /> Now
                </Button>
              </div>
              <div className="mt-1.5 flex justify-end">
                <Button
                  type="button"
                  onClick={() => void handleClockOut()}
                  disabled={savingOut || !editEndTm}
                  className="h-6 rounded-lg bg-[#8B2332] px-2.5 text-[10px] text-white hover:bg-[#722030]"
                >
                  {savingOut ? <Loader2 className="mr-1 h-3 w-3 animate-spin" /> : <Save className="mr-1 h-3 w-3" />}
                  {msgs.manualSaveOut}
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* ── Calendar grid ── */}
        {loading ? (
          <div className="flex min-h-[140px] items-center justify-center rounded-[14px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            {msgs.loading}
          </div>
        ) : (
          <div className="rounded-[14px] border border-slate-200/80 bg-white px-2 py-1.5">
            {/* weekday header: 토=파란 일=빨간 */}
            <div className="mb-1 grid grid-cols-7 gap-1 text-center text-[10px] font-medium uppercase tracking-[0.12em]">
              {weekdayLabels.map((w, i) => (
                <div key={w} className={cn(
                  i === 5 ? "text-blue-500" : i === 6 ? "text-rose-500" : "text-slate-400",
                )}>{w}</div>
              ))}
            </div>
            <div className="grid grid-cols-7 gap-1">
              {calDays.map((day) => {
                const checkIn = formatAttndTimeForDisplay(day.wrkTm?.strTm);
                const checkOut = formatAttndTimeForDisplay(day.wrkTm?.endTm);
                const isSelected = selectedDt === day.dt;

                return (
                  <button
                    type="button"
                    key={day.dt}
                    onClick={() => handleDayClick(day)}
                    disabled={!day.inMonth}
                    className={cn(
                      "min-h-[42px] rounded-[12px] border px-1 py-1 text-left transition-colors",
                      day.inMonth ? "border-slate-200 bg-white hover:bg-slate-50 cursor-pointer" : "border-transparent bg-slate-50/70 cursor-default",
                      day.today && "ring-1 ring-[#4F72C8]",
                      // 공휴일 배경
                      day.holiday && day.inMonth && "border-rose-200 bg-rose-50/70",
                      // 토요일 배경
                      day.saturday && day.inMonth && !day.holiday && "bg-blue-50/50 border-blue-100",
                      // 일요일 배경
                      day.sunday && day.inMonth && !day.holiday && "bg-rose-50/40 border-rose-100",
                      // 선택된 날짜
                      isSelected && "ring-2 ring-[#23468F] bg-blue-50/50",
                    )}
                  >
                    <div className="flex items-center justify-between">
                      <span className={cn(
                        "text-xs font-semibold",
                        !day.inMonth ? "text-slate-300"
                          : day.holiday ? "text-rose-600"
                          : day.sunday ? "text-rose-500"
                          : day.saturday ? "text-blue-500"
                          : day.today ? "text-[#18397E]"
                          : "text-slate-700",
                      )}>
                        {day.dayNo}
                      </span>
                      {day.holiday && day.inMonth && <span className="h-1.5 w-1.5 rounded-full bg-rose-500" />}
                    </div>
                    <div className="mt-0.5 space-y-0.5">
                      {/* 출근 시간 */}
                      {checkIn && (
                        <div className="flex items-center gap-0.5 text-[8px] font-medium text-[#18397E]">
                          <span className="block h-1 w-1 rounded-full bg-blue-500" />
                          <span>{checkIn}</span>
                        </div>
                      )}
                      {/* 퇴근 시간 */}
                      {checkOut && (
                        <div className="flex items-center gap-0.5 text-[8px] font-medium text-slate-600">
                          <span className="block h-1 w-1 rounded-full bg-rose-500" />
                          <span>{checkOut}</span>
                        </div>
                      )}
                      {/* 공휴일명 (출근 기록 없을 때만) */}
                      {!checkIn && day.holiday && day.holidayNm && day.inMonth && (
                        <div className="line-clamp-2 text-[8px] leading-[10px] text-rose-600">{day.holidayNm}</div>
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}
      </CardCnt>
    </Card>
  );
}

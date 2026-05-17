"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { FileText, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import type { WrkTmListResponse, WrkTmResponse } from "@/lib/api/wrk-tm-contract";
import { WRK_TM_API } from "@/lib/api/wrk-tm-contract";
import { formatAttndTimeForDisplay } from "@/lib/utils/attnd-time";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspWorkTimeHistMsgs } from "@/lib/i18n/wsp-cnt";
import type { WspMode } from "@/lib/utils/wsp/platform-shell-data";

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type WorkTimeRptPageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  mode: WspMode;
  onUnauthorized: () => void;
  onLoadingChange?: (loading: boolean) => void;
};

function currentYm() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

function ymRange(ym: string) {
  const [y, m] = ym.split("-").map(Number);
  if (!y || !m) return { from: ym + "-01", to: ym + "-28" };
  const last = new Date(Date.UTC(y, m, 0)).getUTCDate();
  return { from: `${ym}-01`, to: `${ym}-${String(last).padStart(2, "0")}` };
}

function fmtMin(min: number | null) {
  if (min == null) return "-";
  const h = Math.floor(min / 60);
  const m = min % 60;
  return `${h}h ${m}m`;
}

export function WorkTimeRptPage({
  accessToken,
  locale,
  mode,
  onUnauthorized,
  onLoadingChange,
}: WorkTimeRptPageProps) {
  const text = getWspWorkTimeHistMsgs(locale);
  const [yearMonth, setYearMonth] = useState(currentYm);
  const [keyword, setKeyword] = useState("");
  const [items, setItems] = useState<WrkTmResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const runWithLoading = useCallback(
    async <T,>(task: () => Promise<T>) => {
      setLoading(true);
      onLoadingChange?.(true);
      try {
        return await task();
      } finally {
        setLoading(false);
        onLoadingChange?.(false);
      }
    },
    [onLoadingChange],
  );

  const load = useCallback(
    async (targetYm: string) => {
      if (!accessToken) { onUnauthorized(); return; }
      await runWithLoading(async () => {
        const { from, to } = ymRange(targetYm);
        const res = await fetch(`${API}${WRK_TM_API}/all?from=${from}&to=${to}`, {
          headers: { Authorization: `Bearer ${accessToken}`, Accept: "application/json" },
        });
        if (res.status === 401) { onUnauthorized(); return; }
        if (!res.ok) throw new Error(`${res.status}`);
        const payload = (await res.json()) as WrkTmListResponse;
        setItems(payload.items ?? []);
      });
    },
    [accessToken, onUnauthorized, runWithLoading],
  );

  useEffect(() => { void load(yearMonth); }, [load, yearMonth]);

  const filteredItems = useMemo(() => {
    if (!keyword.trim()) return items;
    const kw = keyword.toLowerCase();
    return items.filter((r) =>
      [r.workDt, String(r.empId), r.rmk ?? ""].some((v) => v.toLowerCase().includes(kw)),
    );
  }, [items, keyword]);

  const totalMin = useMemo(() => filteredItems.reduce((s, r) => s + (r.workMin ?? 0), 0), [filteredItems]);

  const scopeLabel = text.scopedAll;

  return (
    <div className="flex h-full min-h-0 flex-col rounded-[4px] border border-slate-300 bg-[#f8f9fb]">
      {/* Header */}
      <div className="border-b border-slate-300 px-3 py-2">
        <div className="flex flex-col gap-2 xl:flex-row xl:items-start xl:justify-between">
          <div className="min-w-0">
            <div className="text-[9px] uppercase tracking-[0.12em] text-slate-400">{text.eyebrow}</div>
            <h1 className="mt-0.5 text-[14px] font-semibold leading-4 text-slate-900">{text.title}</h1>
            <p className="mt-0.5 text-[10px] leading-4 text-slate-500">{text.desc}</p>
          </div>
          <div className="grid gap-1 sm:grid-cols-3 xl:min-w-[420px]">
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">{text.currentScope}</div>
              <div className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-800">
                {mode === "admin" ? text.modeAdmin : text.modeClient}
              </div>
            </div>
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">{text.emp}</div>
              <div className="mt-0.5 truncate text-[11px] font-semibold leading-4 text-slate-800">{scopeLabel}</div>
            </div>
            <div className="rounded-[3px] border border-slate-300 bg-white px-2 py-1.5">
              <div className="text-[8px] uppercase tracking-[0.08em] text-slate-400">{text.total}</div>
              <div className="mt-0.5 text-[11px] font-semibold leading-4 text-slate-800">
                {filteredItems.length} {text.rows} · {fmtMin(totalMin)}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="border-b border-slate-300 bg-[#f3f5f8] px-3 py-2">
        <div className="grid flex-1 gap-3 sm:grid-cols-2 xl:grid-cols-[170px_minmax(0,1fr)]">
          <label className="space-y-1">
            <span className="text-[9px] leading-3 text-slate-500">{text.month}</span>
            <input
              type="month" value={yearMonth}
              onChange={(e) => setYearMonth(e.target.value)}
              className="h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[11px] leading-4 text-slate-700 outline-none focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
            />
          </label>
          <label className="space-y-1">
            <span className="text-[9px] leading-3 text-slate-500">{text.keyword}</span>
            <input
              type="text" value={keyword} placeholder={text.keywordPh}
              onChange={(e) => setKeyword(e.target.value)}
              className="h-7 w-full rounded-[3px] border border-slate-300 bg-white px-2 text-[11px] leading-4 text-slate-700 outline-none focus:border-[#7E9BD8] focus:ring-1 focus:ring-[#BCD0F5]"
            />
          </label>
        </div>
      </div>

      {/* Table */}
      <div className="min-h-0 flex-1 p-2">
        <div className="flex h-full min-h-0 flex-col overflow-hidden rounded-[3px] border border-slate-300 bg-white">
          <div className="flex h-8 items-center justify-between border-b border-slate-300 bg-[#eef1f4] px-3 text-[10px] text-slate-500">
            <div className="flex items-center gap-1.5 font-medium text-slate-600">
              <FileText className="h-3.5 w-3.5" />
              {text.resultsTitle}
            </div>
          </div>

          <div className="min-h-0 flex-1 overflow-auto">
            {loading ? (
              <div className="flex min-h-[120px] items-center justify-center text-sm text-slate-400">
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              </div>
            ) : filteredItems.length === 0 ? (
              <div className="px-6 py-12 text-center text-[11px] text-slate-500">
                <div className="text-[13px] font-semibold text-slate-700">{text.noDataTitle}</div>
                <div className="mt-1.5 leading-5">{text.noDataDesc}</div>
              </div>
            ) : (
              <table className="min-w-full border-collapse text-[11px] text-slate-700">
                <thead className="sticky top-0 z-10">
                  <tr className="bg-[#eef1f4] text-[9px] font-semibold uppercase tracking-[0.04em] text-slate-600">
                    <th className="border border-slate-300 px-2 py-1.5 text-center">No</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-left">{text.date}</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-center">EMP ID</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-center">{text.checkIn}</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-center">{text.checkOut}</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-center">{text.workTime}</th>
                    <th className="border border-slate-300 px-2 py-1.5 text-left">Remark</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredItems.map((r, idx) => (
                    <tr key={r.id} className="odd:bg-white even:bg-slate-50/30 hover:bg-slate-50/60">
                      <td className="border border-slate-300 px-2 py-1.5 text-center text-[10px]">{idx + 1}</td>
                      <td className="border border-slate-300 px-2 py-1.5 text-[10px]">{r.workDt}</td>
                      <td className="border border-slate-300 px-2 py-1.5 text-center text-[10px] text-[#215db0]">{r.empId}</td>
                      <td className="border border-slate-300 px-2 py-1.5 text-center text-[10px] text-blue-700">
                        {formatAttndTimeForDisplay(r.strTm)}
                      </td>
                      <td className="border border-slate-300 px-2 py-1.5 text-center text-[10px] text-rose-700">
                        {formatAttndTimeForDisplay(r.endTm)}
                      </td>
                      <td className="border border-slate-300 px-2 py-1.5 text-center text-[10px]">{fmtMin(r.workMin)}</td>
                      <td className="border border-slate-300 px-2 py-1.5 text-[10px] text-slate-500">{r.rmk ?? "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

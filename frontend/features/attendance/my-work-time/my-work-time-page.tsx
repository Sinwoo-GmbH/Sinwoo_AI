"use client";

import { useCallback, useState } from "react";
import { Clock, Loader2, Save, Trash2 } from "lucide-react";

import { AttndCalCard } from "@/features/attendance/attnd-cal-card";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { toast } from "@/components/ui/toast";
import { Card, CardCnt, CardHeader, CardTitle } from "@/components/ui/card";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import type { WrkTmResponse } from "@/lib/api/wrk-tm-contract";
import { WRK_TM_API } from "@/lib/api/wrk-tm-contract";
import { formatAttndTimeForDisplay } from "@/lib/utils/attnd-time";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspMyWorkTimeMsgs } from "@/lib/i18n/wsp-cnt";

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Props = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

const LABELS: Record<LoginLocale, {
  detailTitle: string; date: string; strTm: string; endTm: string; workMin: string;
  rmk: string; save: string; del: string; confirmDelTitle: string; confirmDel: string;
  confirmYes: string; confirmNo: string; noSel: string; minutes: string;
  errEndBeforeStr: string; errSaveFailed: string;
}> = {
  ko: {
    detailTitle: "근무 상세", date: "일자", strTm: "출근 시간", endTm: "퇴근 시간",
    workMin: "근무 시간(분)", rmk: "비고", save: "저장", del: "삭제",
    confirmDelTitle: "삭제 확인", confirmDel: "이 근무 기록을 삭제하시겠습니까?",
    confirmYes: "삭제", confirmNo: "취소", noSel: "캘린더에서 날짜를 선택하세요.", minutes: "분",
    errEndBeforeStr: "퇴근 시간은 출근 시간 이후여야 합니다.", errSaveFailed: "저장에 실패했습니다. 다시 시도해 주세요.",
  },
  en: {
    detailTitle: "Work Detail", date: "Date", strTm: "Start time", endTm: "End time",
    workMin: "Work time (min)", rmk: "Remark", save: "Save", del: "Delete",
    confirmDelTitle: "Confirm Delete", confirmDel: "Delete this work record?",
    confirmYes: "Delete", confirmNo: "Cancel", noSel: "Select a date from the calendar.", minutes: "min",
    errEndBeforeStr: "Check-out time must be after check-in time.", errSaveFailed: "Failed to save. Please try again.",
  },
  de: {
    detailTitle: "Arbeitsdetail", date: "Datum", strTm: "Startzeit", endTm: "Endzeit",
    workMin: "Arbeitszeit (Min)", rmk: "Bemerkung", save: "Speichern", del: "Löschen",
    confirmDelTitle: "Löschen bestätigen", confirmDel: "Diesen Eintrag löschen?",
    confirmYes: "Löschen", confirmNo: "Abbrechen", noSel: "Wählen Sie ein Datum im Kalender.", minutes: "Min",
    errEndBeforeStr: "Die Endzeit muss nach der Startzeit liegen.", errSaveFailed: "Speichern fehlgeschlagen. Bitte erneut versuchen.",
  },
};

export function MyWorkTimePage({ accessToken, locale, onLoadingChange, onUnauthorized }: Props) {
  const msgs = getWspMyWorkTimeMsgs(locale);
  const lb = LABELS[locale];

  const [selectedDt, setSelectedDt] = useState<string | null>(null);
  const [selectedWt, setSelectedWt] = useState<WrkTmResponse | null>(null);

  // form state
  const [fStrTm, setFStrTm] = useState("");
  const [fEndTm, setFEndTm] = useState("");
  const [fRmk, setFRmk] = useState("");
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [confirmDelOpen, setConfirmDelOpen] = useState(false);
  const [calKey, setCalKey] = useState(0); // force calendar reload

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

  const handleDaySelect = useCallback((dt: string, wt: WrkTmResponse | null) => {
    setSelectedDt(dt);
    setSelectedWt(wt);
    setFStrTm(wt?.strTm ? wt.strTm.slice(0, 5) : "");
    setFEndTm(wt?.endTm ? wt.endTm.slice(0, 5) : "");
    setFRmk(wt?.rmk ?? "");
  }, []);

  const handleSave = useCallback(async () => {
    if (!selectedDt) return;

    if (fStrTm && fEndTm && fEndTm <= fStrTm) {
      toast.error(lb.errEndBeforeStr);
      return;
    }

    setSaving(true);
    onLoadingChange(true);
    try {
      await fetchJson(`${WRK_TM_API}/save`, {
        method: "POST",
        body: JSON.stringify({
          workDt: selectedDt,
          strTm: fStrTm ? `${fStrTm}:00` : null,
          endTm: fEndTm ? `${fEndTm}:00` : null,
          rmk: fRmk || null,
        }),
      });
      setCalKey((k) => k + 1);
    } catch {
      toast.error(lb.errSaveFailed);
    } finally { setSaving(false); onLoadingChange(false); }
  }, [selectedDt, fStrTm, fEndTm, fRmk, fetchJson, onLoadingChange, lb]);

  const handleDeleteConfirm = useCallback(async () => {
    if (!selectedWt?.id) return;
    setConfirmDelOpen(false);
    setDeleting(true);
    onLoadingChange(true);
    try {
      await fetchJson(`${WRK_TM_API}/${selectedWt.id}`, { method: "DELETE" });
      setSelectedDt(null);
      setSelectedWt(null);
      setCalKey((k) => k + 1);
    } catch { /* silent */ } finally { setDeleting(false); onLoadingChange(false); }
  }, [selectedWt, fetchJson, onLoadingChange]);

  return (
    <div className="wsp-scrollbar h-full overflow-auto">
      <ConfirmDialog
        open={confirmDelOpen}
        title={lb.confirmDelTitle}
        message={lb.confirmDel}
        confirmLabel={lb.confirmYes}
        cancelLabel={lb.confirmNo}
        variant="danger"
        onConfirm={() => void handleDeleteConfirm()}
        onCancel={() => setConfirmDelOpen(false)}
      />
      <WspPageHdr strip title={msgs.title} />

      <div className="mt-2 grid gap-3 xl:grid-cols-[1.15fr_0.85fr]">
        <AttndCalCard
          key={calKey}
          accessToken={accessToken}
          locale={locale}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
          onDaySelect={handleDaySelect}
          selectedDt={selectedDt}
        />

        {/* ── Detail / Edit panel ── */}
        <Card className="border-slate-200/90 shadow-[0_10px_24px_rgba(148,163,184,0.06)]">
          <CardHeader className="px-3 py-2">
            <CardTitle className="flex items-center gap-1.5 text-[13px]">
              <Clock className="h-4 w-4 text-slate-400" />
              {lb.detailTitle}
            </CardTitle>
          </CardHeader>
          <CardCnt className="px-3 py-3 pt-1">
            {!selectedDt ? (
              <div className="flex min-h-[160px] items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50/70 text-sm text-slate-400">
                {lb.noSel}
              </div>
            ) : (
              <div className="space-y-3">
                {/* Date (readonly) */}
                <div>
                  <label className="mb-1 block text-[11px] font-medium text-slate-500">{lb.date}</label>
                  <div className="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-[13px] text-slate-700">
                    {selectedDt}
                  </div>
                </div>

                {/* Start time */}
                <div>
                  <label className="mb-1 block text-[11px] font-medium text-slate-500">{lb.strTm}</label>
                  <input
                    type="time" value={fStrTm} onChange={(e) => setFStrTm(e.target.value)}
                    className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none focus:border-[#4F72C8]"
                  />
                </div>

                {/* End time */}
                <div>
                  <label className="mb-1 block text-[11px] font-medium text-slate-500">{lb.endTm}</label>
                  <input
                    type="time" value={fEndTm} onChange={(e) => setFEndTm(e.target.value)}
                    className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] text-slate-700 outline-none focus:border-[#4F72C8]"
                  />
                </div>

                {/* Work minutes (readonly) */}
                {selectedWt?.workMin != null && (
                  <div>
                    <label className="mb-1 block text-[11px] font-medium text-slate-500">{lb.workMin}</label>
                    <div className="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-[13px] text-slate-700">
                      {selectedWt.workMin} {lb.minutes}
                    </div>
                  </div>
                )}

                {/* Remark */}
                <div>
                  <label className="mb-1 block text-[11px] font-medium text-slate-500">{lb.rmk}</label>
                  <textarea
                    value={fRmk} onChange={(e) => setFRmk(e.target.value)}
                    rows={2} maxLength={500}
                    className="w-full rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-[13px] text-slate-700 outline-none focus:border-[#4F72C8]"
                  />
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2 pt-1">
                  <Button
                    type="button" onClick={() => void handleSave()}
                    disabled={saving || (!fStrTm && !fEndTm)}
                    className="h-8 rounded-lg bg-[#23468F] px-3 text-[12px] text-white hover:bg-[#1D3975]"
                  >
                    {saving ? <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" /> : <Save className="mr-1 h-3.5 w-3.5" />}
                    {lb.save}
                  </Button>
                  {selectedWt && (
                    <Button
                      type="button" variant="outline" onClick={() => setConfirmDelOpen(true)}
                      disabled={deleting}
                      className="h-8 rounded-lg border-rose-200 px-3 text-[12px] text-rose-600 hover:bg-rose-50"
                    >
                      {deleting ? <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" /> : <Trash2 className="mr-1 h-3.5 w-3.5" />}
                      {lb.del}
                    </Button>
                  )}
                </div>
              </div>
            )}
          </CardCnt>
        </Card>
      </div>
    </div>
  );
}

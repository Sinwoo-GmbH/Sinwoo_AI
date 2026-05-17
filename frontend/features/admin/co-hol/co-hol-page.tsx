"use client";

import { useCallback, useEffect, useState } from "react";
import { CalendarDays, Loader2, Pencil, Plus, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardCnt, CardHeader, CardTitle } from "@/components/ui/card";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import type { CoHolListResponse, CoHolResponse, CreateCoHolRequest, UpdateCoHolRequest } from "@/lib/api/hol-contract";
import { CO_HOL_API } from "@/lib/api/hol-contract";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspCoHolMsgs } from "@/lib/i18n/wsp-cnt";

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Props = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

type FormState = {
  open: boolean;
  mode: "create" | "edit";
  editId: number | null;
  holidayNm: string;
  strDt: string;
  endDt: string;
  annualYn: string;
  applyYr: string;
};

const INIT_FORM: FormState = {
  open: false, mode: "create", editId: null,
  holidayNm: "", strDt: "", endDt: "", annualYn: "Y", applyYr: "0",
};

export function CoHolPage({ accessToken, locale, onLoadingChange, onUnauthorized }: Props) {
  const msgs = getWspCoHolMsgs(locale);

  const [items, setItems] = useState<CoHolResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<FormState>(INIT_FORM);

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
      return (await res.json()) as T;
    },
    [accessToken, onUnauthorized],
  );

  const loadAll = useCallback(async () => {
    setLoading(true);
    onLoadingChange(true);
    try {
      const res = await fetchJson<CoHolListResponse>(CO_HOL_API);
      setItems(res?.items ?? []);
    } catch { /* silent */ } finally { setLoading(false); onLoadingChange(false); }
  }, [fetchJson, onLoadingChange]);

  useEffect(() => { void loadAll(); }, [loadAll]);

  const openCreate = () => setForm({ ...INIT_FORM, open: true });

  const openEdit = (h: CoHolResponse) => setForm({
    open: true, mode: "edit", editId: h.id,
    holidayNm: h.holidayNm, strDt: h.strDt, endDt: h.endDt,
    annualYn: h.annualYn, applyYr: String(h.applyYr),
  });

  const closeForm = () => setForm(INIT_FORM);

  const handleSave = useCallback(async () => {
    const body: CreateCoHolRequest | UpdateCoHolRequest = {
      holidayNm: form.holidayNm,
      strDt: form.strDt,
      endDt: form.endDt,
      annualYn: form.annualYn,
      applyYr: Number(form.applyYr),
    };
    setSaving(true);
    onLoadingChange(true);
    try {
      if (form.mode === "create") {
        await fetchJson(CO_HOL_API, { method: "POST", body: JSON.stringify(body) });
      } else {
        await fetchJson(`${CO_HOL_API}/${form.editId}`, { method: "PUT", body: JSON.stringify(body) });
      }
      closeForm();
      await loadAll();
    } catch { /* silent */ } finally { setSaving(false); onLoadingChange(false); }
  }, [form, fetchJson, loadAll, onLoadingChange]);

  const handleDelete = useCallback(async (id: number) => {
    if (!window.confirm(msgs.confirmDel)) return;
    onLoadingChange(true);
    try {
      await fetchJson(`${CO_HOL_API}/${id}`, { method: "DELETE" });
      await loadAll();
    } catch { /* silent */ } finally { onLoadingChange(false); }
  }, [fetchJson, loadAll, msgs.confirmDel, onLoadingChange]);

  const formValid = form.holidayNm.trim() && form.strDt && form.endDt;

  return (
    <div className="space-y-2">
      <WspPageHdr strip title={msgs.title} />

      <Card className="border-slate-200/90">
        <CardHeader className="flex-row items-center justify-between px-3 py-2">
          <CardTitle className="flex items-center gap-1.5 text-[13px]">
            <CalendarDays className="h-4 w-4 text-slate-400" />
            {msgs.title}
          </CardTitle>
          <Button type="button" onClick={openCreate} className="h-7 rounded-lg bg-[#23468F] px-2.5 text-[11px] text-white hover:bg-[#1D3975]">
            <Plus className="mr-1 h-3.5 w-3.5" /> {msgs.add}
          </Button>
        </CardHeader>
        <CardCnt className="px-3 py-2 pt-0">
          {loading ? (
            <div className="flex min-h-[120px] items-center justify-center text-sm text-slate-400">
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            </div>
          ) : items.length === 0 ? (
            <div className="flex min-h-[120px] items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50/70 text-sm text-slate-400">
              {msgs.noData}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-[12px]">
                <thead>
                  <tr className="border-b border-slate-200 text-left text-[11px] font-medium text-slate-500">
                    <th className="px-2 py-1.5">{msgs.name}</th>
                    <th className="px-2 py-1.5">{msgs.strDt}</th>
                    <th className="px-2 py-1.5">{msgs.endDt}</th>
                    <th className="px-2 py-1.5">{msgs.annual}</th>
                    <th className="px-2 py-1.5">{msgs.applyYr}</th>
                    <th className="px-2 py-1.5 text-right" />
                  </tr>
                </thead>
                <tbody>
                  {items.map((h) => (
                    <tr key={h.id} className="border-b border-slate-100 hover:bg-slate-50/60">
                      <td className="px-2 py-1.5 font-medium text-slate-700">{h.holidayNm}</td>
                      <td className="px-2 py-1.5 text-slate-600">{h.strDt}</td>
                      <td className="px-2 py-1.5 text-slate-600">{h.endDt}</td>
                      <td className="px-2 py-1.5 text-slate-600">{h.annualYn === "Y" ? msgs.annualY : msgs.annualN}</td>
                      <td className="px-2 py-1.5 text-slate-600">{h.annualYn === "Y" ? "-" : h.applyYr}</td>
                      <td className="px-2 py-1.5 text-right">
                        <div className="flex items-center justify-end gap-1">
                          <button type="button" onClick={() => openEdit(h)} className="rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
                            <Pencil className="h-3.5 w-3.5" />
                          </button>
                          <button type="button" onClick={() => void handleDelete(h.id)} className="rounded p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-500">
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardCnt>
      </Card>

      {/* ── Inline form (slide-down) ── */}
      {form.open && (
        <Card className="border-[#4F72C8]/30 bg-blue-50/20">
          <CardHeader className="px-3 py-2">
            <CardTitle className="text-[13px]">{form.mode === "create" ? msgs.add : msgs.edit}</CardTitle>
          </CardHeader>
          <CardCnt className="px-3 py-2 pt-0">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="sm:col-span-2">
                <label className="mb-1 block text-[11px] font-medium text-slate-500">{msgs.name}</label>
                <input
                  type="text" value={form.holidayNm} maxLength={255}
                  onChange={(e) => setForm((f) => ({ ...f, holidayNm: e.target.value }))}
                  className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-medium text-slate-500">{msgs.strDt}</label>
                <input
                  type="date" value={form.strDt}
                  onChange={(e) => setForm((f) => ({ ...f, strDt: e.target.value }))}
                  className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-medium text-slate-500">{msgs.endDt}</label>
                <input
                  type="date" value={form.endDt}
                  onChange={(e) => setForm((f) => ({ ...f, endDt: e.target.value }))}
                  className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-medium text-slate-500">{msgs.annual}</label>
                <select
                  value={form.annualYn}
                  onChange={(e) => setForm((f) => ({ ...f, annualYn: e.target.value, applyYr: e.target.value === "Y" ? "0" : f.applyYr }))}
                  className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-[13px] outline-none focus:border-[#4F72C8]"
                >
                  <option value="Y">{msgs.annualY}</option>
                  <option value="N">{msgs.annualN}</option>
                </select>
              </div>
              {form.annualYn === "N" && (
                <div>
                  <label className="mb-1 block text-[11px] font-medium text-slate-500">{msgs.applyYr}</label>
                  <input
                    type="number" value={form.applyYr} min={2020} max={2099}
                    onChange={(e) => setForm((f) => ({ ...f, applyYr: e.target.value }))}
                    className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                  />
                </div>
              )}
            </div>
            <div className="mt-3 flex items-center gap-2">
              <Button
                type="button" onClick={() => void handleSave()}
                disabled={saving || !formValid}
                className="h-8 rounded-lg bg-[#23468F] px-3 text-[12px] text-white hover:bg-[#1D3975]"
              >
                {saving && <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" />}
                {msgs.save}
              </Button>
              <Button type="button" variant="outline" onClick={closeForm} className="h-8 rounded-lg px-3 text-[12px]">
                {msgs.cancel}
              </Button>
            </div>
          </CardCnt>
        </Card>
      )}
    </div>
  );
}

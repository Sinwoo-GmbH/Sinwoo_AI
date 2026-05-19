"use client";

import { useCallback, useEffect, useState } from "react";
import {
  Building2,
  ChevronDown,
  ChevronRight,
  FolderPlus,
  Loader2,
  Pencil,
  Plus,
  Trash2,
  Users,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardCnt, CardHeader, CardTitle } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { toast } from "@/components/ui/toast";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import type {
  DeptEmpCountResponse,
  DeptListResponse,
  DeptNodeResponse,
  DeptRequest,
  DeptResponse,
  DeptTreeResponse,
} from "@/lib/api/dept-contract";
import { DEPT_API_PATH } from "@/lib/api/dept-contract";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getDeptMgtMsgs, REGION_OPTIONS, regionLabel } from "@/lib/i18n/dept-cnt";

const API = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/* ── Types ──────────────────────────────────────────────── */

type Props = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

type FormMode = "idle" | "create" | "edit";

type FormState = {
  mode: FormMode;
  editId: number | null;
  parentId: number | null;
  parentNm: string;
  deptCd: string;
  deptNm: string;
  regionCd: string;
  vacCnt: string;
  vacInc: string;
  dspOrd: string;
  stsCd: string;
};

type FormErrors = {
  deptCd?: string;
  deptNm?: string;
};

const INIT_FORM: FormState = {
  mode: "idle",
  editId: null,
  parentId: null,
  parentNm: "",
  deptCd: "",
  deptNm: "",
  regionCd: "",
  vacCnt: "24.0",
  vacInc: "0.5",
  dspOrd: "",
  stsCd: "ACTIVE",
};

/* ── Component ──────────────────────────────────────────── */

export function DeptMgtPage({ accessToken, locale, onLoadingChange, onUnauthorized }: Props) {
  const L = getDeptMgtMsgs(locale);

  // data
  const [treeItems, setTreeItems] = useState<DeptNodeResponse[]>([]);
  const [flatItems, setFlatItems] = useState<DeptResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // selection
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());
  const [empCounts, setEmpCounts] = useState<Record<number, number>>({});

  // form
  const [form, setForm] = useState<FormState>(INIT_FORM);
  const [errors, setErrors] = useState<FormErrors>({});

  // confirm dialog
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState<number | null>(null);

  /* ── API helper ────────────────────────────────────────── */

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
      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(txt || `${res.status}`);
      }
      return (await res.json()) as T;
    },
    [accessToken, onUnauthorized],
  );

  /* ── Load data ─────────────────────────────────────────── */

  const loadAll = useCallback(async () => {
    setLoading(true);
    onLoadingChange(true);
    try {
      const [treeRes, listRes] = await Promise.all([
        fetchJson<DeptTreeResponse>(`${DEPT_API_PATH}/tree`),
        fetchJson<DeptListResponse>(DEPT_API_PATH),
      ]);
      setTreeItems(treeRes?.itemList ?? []);
      setFlatItems(listRes?.itemList ?? []);
      // auto-expand all on first load
      if (treeRes?.itemList) {
        const ids = new Set<number>();
        const walk = (nodes: DeptNodeResponse[]) => {
          for (const n of nodes) {
            if (n.childList.length > 0) { ids.add(n.deptId); walk(n.childList); }
          }
        };
        walk(treeRes.itemList);
        setExpandedIds(ids);
      }
    } catch { /* silent */ } finally { setLoading(false); onLoadingChange(false); }
  }, [fetchJson, onLoadingChange]);

  useEffect(() => { void loadAll(); }, [loadAll]);

  /* ── Load emp count for a dept ─────────────────────────── */

  const loadEmpCount = useCallback(async (deptId: number) => {
    try {
      const res = await fetchJson<DeptEmpCountResponse>(`${DEPT_API_PATH}/${deptId}/emp-count`);
      if (res) setEmpCounts((prev) => ({ ...prev, [deptId]: res.empCnt }));
    } catch { /* silent */ }
  }, [fetchJson]);

  /* ── Tree toggle / select ──────────────────────────────── */

  const toggleExpand = (id: number) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const selectDept = (dept: DeptNodeResponse | DeptResponse) => {
    const id = "deptId" in dept ? dept.deptId : (dept as DeptResponse).deptId;
    setSelectedId(id);
    void loadEmpCount(id);

    // find flat item for full details
    const flat = flatItems.find((f) => f.deptId === id);
    if (flat) {
      const parentFlat = flat.upDeptId ? flatItems.find((f) => f.deptId === flat.upDeptId) : null;
      setForm({
        mode: "edit",
        editId: flat.deptId,
        parentId: flat.upDeptId ?? null,
        parentNm: parentFlat?.deptNm ?? (flat.upDeptId ? `#${flat.upDeptId}` : ""),
        deptCd: flat.deptCd,
        deptNm: flat.deptNm,
        regionCd: flat.regionCd ?? "",
        vacCnt: flat.vacCnt != null ? String(flat.vacCnt) : "24.0",
        vacInc: flat.vacInc != null ? String(flat.vacInc) : "0.0",
        dspOrd: flat.dspOrd != null ? String(flat.dspOrd) : "",
        stsCd: flat.stsCd ?? "ACTIVE",
      });
      setErrors({});
    }
  };

  /* ── Form actions ──────────────────────────────────────── */

  const openCreateRoot = () => {
    setSelectedId(null);
    setForm({
      ...INIT_FORM,
      mode: "create",
      parentId: null,
      parentNm: "",
      dspOrd: String(flatItems.filter((f) => !f.upDeptId).length + 1),
    });
    setErrors({});
  };

  const openCreateChild = (parentDept: DeptNodeResponse | DeptResponse) => {
    const pid = "deptId" in parentDept ? parentDept.deptId : (parentDept as DeptResponse).deptId;
    const pNm = "deptNm" in parentDept ? parentDept.deptNm : "";
    setSelectedId(null);
    setForm({
      ...INIT_FORM,
      mode: "create",
      parentId: pid,
      parentNm: pNm,
      regionCd: ("regionCd" in parentDept && parentDept.regionCd) ? parentDept.regionCd : "",
      vacCnt: ("vacCnt" in parentDept && parentDept.vacCnt != null) ? String(parentDept.vacCnt) : "24.0",
      vacInc: ("vacInc" in parentDept && parentDept.vacInc != null) ? String(parentDept.vacInc) : "0.0",
    });
    setErrors({});
  };

  const closeForm = () => {
    setForm(INIT_FORM);
    setErrors({});
  };

  const updateField = (field: keyof FormState, value: string) => {
    setForm((f) => ({ ...f, [field]: value }));
    if (field in errors) setErrors((e) => ({ ...e, [field]: undefined }));
  };

  /* ── Validate ──────────────────────────────────────────── */

  const validate = (): boolean => {
    const errs: FormErrors = {};
    if (!form.deptCd.trim()) errs.deptCd = L.deptCdRequired;
    if (!form.deptNm.trim()) errs.deptNm = L.deptNmRequired;
    setErrors(errs);
    if (Object.keys(errs).length > 0) {
      toast.error(L.requiredFields);
      return false;
    }
    return true;
  };

  /* ── Save (Create / Update) ────────────────────────────── */

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    onLoadingChange(true);
    try {
      const body: DeptRequest = {
        tenantId: 1,
        coId: 1,
        deptCd: form.deptCd.trim().toUpperCase(),
        deptNm: form.deptNm.trim(),
        upDeptId: form.parentId,
        stsCd: form.stsCd || "ACTIVE",
        regionCd: form.regionCd || null,
        vacCnt: form.vacCnt ? Number(form.vacCnt) : null,
        vacInc: form.vacInc ? Number(form.vacInc) : null,
        dspOrd: form.dspOrd ? Number(form.dspOrd) : null,
      };

      if (form.mode === "create") {
        await fetchJson(DEPT_API_PATH, { method: "POST", body: JSON.stringify(body) });
        toast.success(L.toastCreated);
      } else if (form.mode === "edit" && form.editId) {
        await fetchJson(`${DEPT_API_PATH}/${form.editId}`, { method: "PUT", body: JSON.stringify(body) });
        toast.success(L.toastUpdated);
      }
      closeForm();
      await loadAll();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "";
      if (msg.includes("already exists")) {
        setErrors((e) => ({ ...e, deptCd: msg }));
      } else {
        toast.error(L.toastError);
      }
    } finally { setSaving(false); onLoadingChange(false); }
  };

  /* ── Delete ────────────────────────────────────────────── */

  const requestDelete = (id: number) => {
    setPendingDeleteId(id);
    setConfirmOpen(true);
  };

  const handleDelete = async () => {
    if (!pendingDeleteId) return;
    setConfirmOpen(false);
    onLoadingChange(true);
    try {
      await fetchJson(`${DEPT_API_PATH}/${pendingDeleteId}`, { method: "DELETE" });
      toast.success(L.toastDeleted);
      if (selectedId === pendingDeleteId) closeForm();
      await loadAll();
    } catch (err) {
      const msg = err instanceof Error ? err.message : "";
      if (msg.includes("child departments")) toast.error(L.cannotDelChild);
      else if (msg.includes("employee")) toast.error(L.cannotDelEmp);
      else toast.error(L.toastError);
    } finally { onLoadingChange(false); setPendingDeleteId(null); }
  };

  /* ── Tree node renderer ────────────────────────────────── */

  const renderTreeNode = (node: DeptNodeResponse, depth: number = 0) => {
    const isSelected = selectedId === node.deptId;
    const isExpanded = expandedIds.has(node.deptId);
    const hasChildren = node.childList.length > 0;

    return (
      <div key={node.deptId}>
        <div
          className={`group flex items-center gap-1 rounded-md px-2 py-1.5 cursor-pointer transition-colors ${
            isSelected
              ? "bg-[#23468F]/10 text-[#23468F]"
              : "hover:bg-slate-50 text-slate-700"
          }`}
          style={{ paddingLeft: `${depth * 20 + 8}px` }}
          onClick={() => selectDept(node)}
        >
          {/* expand/collapse */}
          {hasChildren ? (
            <button
              type="button"
              className="shrink-0 rounded p-0.5 hover:bg-slate-200/60"
              onClick={(e) => { e.stopPropagation(); toggleExpand(node.deptId); }}
            >
              {isExpanded
                ? <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
                : <ChevronRight className="h-3.5 w-3.5 text-slate-400" />
              }
            </button>
          ) : (
            <span className="inline-block w-[18px]" />
          )}

          {/* icon */}
          <Building2 className={`h-3.5 w-3.5 shrink-0 ${isSelected ? "text-[#23468F]" : "text-slate-400"}`} />

          {/* name */}
          <span className={`flex-1 truncate text-[12px] font-medium ${isSelected ? "text-[#23468F]" : ""}`}>
            {node.deptNm}
          </span>

          {/* code badge */}
          <span className="hidden group-hover:inline-block text-[10px] text-slate-400 font-mono">
            {node.deptCd}
          </span>

          {/* action buttons (hover) */}
          <div className="hidden group-hover:flex items-center gap-0.5">
            <button
              type="button"
              className="rounded p-1 text-slate-400 hover:bg-slate-200/60 hover:text-slate-600"
              title={L.addChild}
              onClick={(e) => { e.stopPropagation(); openCreateChild(node); }}
            >
              <FolderPlus className="h-3 w-3" />
            </button>
            <button
              type="button"
              className="rounded p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-500"
              title={L.del}
              onClick={(e) => { e.stopPropagation(); requestDelete(node.deptId); }}
            >
              <Trash2 className="h-3 w-3" />
            </button>
          </div>
        </div>

        {/* children */}
        {hasChildren && isExpanded && (
          <div>
            {node.childList.map((child) => renderTreeNode(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  /* ── Field label with required marker ──────────────────── */

  const FieldLabel = ({ label, required, error }: { label: string; required?: boolean; error?: string }) => (
    <label className="mb-1 block text-[11px] font-medium text-slate-500">
      {label}
      {required && <span className="ml-0.5 text-rose-500">*</span>}
      {error && <span className="ml-1 text-[10px] text-rose-500">{error}</span>}
    </label>
  );

  /* ── Render ────────────────────────────────────────────── */

  return (
    <div className="space-y-2">
      <WspPageHdr strip title={L.title} />

      <div className="grid gap-2 lg:grid-cols-[59fr_41fr]">
        {/* ── LEFT: Tree panel ── */}
        <Card className="border-slate-200/90">
          <CardHeader className="flex-row items-center justify-between px-3 py-2">
            <CardTitle className="flex items-center gap-1.5 text-[13px]">
              <Building2 className="h-4 w-4 text-slate-400" />
              {L.treeTitle}
            </CardTitle>
            <Button
              type="button"
              onClick={openCreateRoot}
              className="h-7 rounded-lg bg-[#23468F] px-2.5 text-[11px] text-white hover:bg-[#1D3975]"
            >
              <Plus className="mr-1 h-3.5 w-3.5" /> {L.addRoot}
            </Button>
          </CardHeader>
          <CardCnt className="px-2 py-2 pt-0">
            {loading ? (
              <div className="flex min-h-[200px] items-center justify-center text-sm text-slate-400">
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              </div>
            ) : treeItems.length === 0 ? (
              <div className="flex min-h-[200px] items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50/70 text-sm text-slate-400">
                {L.noData}
              </div>
            ) : (
              <div className="max-h-[calc(100vh-220px)] overflow-y-auto wsp-scrollbar">
                {treeItems.map((node) => renderTreeNode(node, 0))}
              </div>
            )}
          </CardCnt>
        </Card>

        {/* ── RIGHT: Detail / Form panel ── */}
        <Card className="border-slate-200/90">
          <CardHeader className="flex-row items-center justify-between px-3 py-2">
            <CardTitle className="flex items-center gap-1.5 text-[13px]">
              <Pencil className="h-4 w-4 text-slate-400" />
              {L.detailTitle}
            </CardTitle>
          </CardHeader>
          <CardCnt className="px-3 py-2 pt-0">
            {form.mode === "idle" ? (
              <div className="flex min-h-[200px] items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50/70 text-sm text-slate-400">
                {L.noDeptSelected}
              </div>
            ) : (
              <div className="space-y-3">
                {/* Parent dept (read only for context) */}
                <div>
                  <FieldLabel label={L.parentDept} />
                  <div className="h-8 flex items-center rounded-lg border border-slate-200 bg-slate-50 px-2.5 text-[13px] text-slate-500">
                    {form.parentNm || L.rootLevel}
                  </div>
                </div>

                {/* dept code + dept name */}
                <div className="grid gap-3 sm:grid-cols-2">
                  <div>
                    <FieldLabel label={L.deptCd} required error={errors.deptCd} />
                    <input
                      type="text"
                      value={form.deptCd}
                      maxLength={100}
                      disabled={form.mode === "edit"}
                      onChange={(e) => updateField("deptCd", e.target.value)}
                      className={`h-8 w-full rounded-lg border bg-white px-2.5 text-[13px] font-mono uppercase outline-none focus:border-[#4F72C8] ${
                        errors.deptCd ? "border-rose-400" : "border-slate-200"
                      } ${form.mode === "edit" ? "bg-slate-50 text-slate-500" : ""}`}
                    />
                  </div>
                  <div>
                    <FieldLabel label={L.deptNm} required error={errors.deptNm} />
                    <input
                      type="text"
                      value={form.deptNm}
                      maxLength={255}
                      onChange={(e) => updateField("deptNm", e.target.value)}
                      className={`h-8 w-full rounded-lg border bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8] ${
                        errors.deptNm ? "border-rose-400" : "border-slate-200"
                      }`}
                    />
                  </div>
                </div>

                {/* region + status */}
                <div className="grid gap-3 sm:grid-cols-2">
                  <div>
                    <FieldLabel label={L.regionCd} />
                    <select
                      value={form.regionCd}
                      onChange={(e) => updateField("regionCd", e.target.value)}
                      className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-[13px] outline-none focus:border-[#4F72C8]"
                    >
                      <option value="">-</option>
                      {REGION_OPTIONS.map((cd) => (
                        <option key={cd} value={cd}>{regionLabel(locale, cd)}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <FieldLabel label={L.stsCd} />
                    <select
                      value={form.stsCd}
                      onChange={(e) => updateField("stsCd", e.target.value)}
                      className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-[13px] outline-none focus:border-[#4F72C8]"
                    >
                      <option value="ACTIVE">{L.active}</option>
                      <option value="INACTIVE">{L.inactive}</option>
                    </select>
                  </div>
                </div>

                {/* vacation days + increment + display order */}
                <div className="grid gap-3 sm:grid-cols-3">
                  <div>
                    <FieldLabel label={L.vacCnt} />
                    <input
                      type="number"
                      value={form.vacCnt}
                      step="0.5"
                      min="0"
                      max="99"
                      onChange={(e) => updateField("vacCnt", e.target.value)}
                      className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                    />
                  </div>
                  <div>
                    <FieldLabel label={L.vacInc} />
                    <input
                      type="number"
                      value={form.vacInc}
                      step="0.5"
                      min="0"
                      max="10"
                      onChange={(e) => updateField("vacInc", e.target.value)}
                      className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                    />
                  </div>
                  <div>
                    <FieldLabel label={L.dspOrd} />
                    <input
                      type="number"
                      value={form.dspOrd}
                      min="1"
                      max="999"
                      onChange={(e) => updateField("dspOrd", e.target.value)}
                      className="h-8 w-full rounded-lg border border-slate-200 bg-white px-2.5 text-[13px] outline-none focus:border-[#4F72C8]"
                    />
                  </div>
                </div>

                {/* employee count (edit mode only) */}
                {form.mode === "edit" && form.editId && empCounts[form.editId] != null && (
                  <div className="flex items-center gap-2 rounded-lg bg-slate-50 px-3 py-2 text-[12px] text-slate-600">
                    <Users className="h-3.5 w-3.5 text-slate-400" />
                    {L.empCount}: <span className="font-semibold">{empCounts[form.editId]}</span>
                  </div>
                )}

                {/* action buttons */}
                <div className="flex items-center gap-2 pt-1">
                  <Button
                    type="button"
                    onClick={() => void handleSave()}
                    disabled={saving}
                    className="h-8 rounded-lg bg-[#23468F] px-3 text-[12px] text-white hover:bg-[#1D3975]"
                  >
                    {saving && <Loader2 className="mr-1 h-3.5 w-3.5 animate-spin" />}
                    {L.save}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={closeForm}
                    className="h-8 rounded-lg px-3 text-[12px]"
                  >
                    {L.cancel}
                  </Button>
                  {form.mode === "edit" && form.editId && (
                    <Button
                      type="button"
                      onClick={() => requestDelete(form.editId!)}
                      className="ml-auto h-8 rounded-lg bg-[#C53030] px-3 text-[12px] text-white hover:bg-[#9B2C2C]"
                    >
                      <Trash2 className="mr-1 h-3.5 w-3.5" />
                      {L.del}
                    </Button>
                  )}
                </div>
              </div>
            )}
          </CardCnt>
        </Card>
      </div>

      {/* ── Confirm delete dialog ── */}
      <ConfirmDialog
        open={confirmOpen}
        title={L.confirmDelTitle}
        message={L.confirmDelMsg}
        confirmLabel={L.confirmDelBtn}
        cancelLabel={L.cancel}
        variant="danger"
        onConfirm={() => void handleDelete()}
        onCancel={() => { setConfirmOpen(false); setPendingDeleteId(null); }}
      />
    </div>
  );
}

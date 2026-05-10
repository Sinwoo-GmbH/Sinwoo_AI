"use client";

import {
  Activity,
  CheckCircle2,
  Database,
  GitBranch,
  Plus,
  RefreshCw,
  Save,
  Search,
  ShieldCheck,
  Trash2,
  X,
} from "lucide-react";
import type { FormEvent, ReactNode } from "react";
import { useCallback, useEffect, useMemo, useState } from "react";

import type {
  BusinessModuleRes,
  BusinessRecordColumnRes,
  BusinessRecordListRes,
  BusinessRecordRes,
  BusinessRecordRow,
  BusinessRelatedListRes,
} from "@/lib/api/business-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { cn } from "@/lib/utils";

type BusinessModulePageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  moduleCd: string;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const labels = {
  en: {
    loading: "Loading",
    unavailable: "Business module unavailable",
    itemCount: "Records",
    primaryTable: "Primary table",
    relatedTables: "Related tables",
    sourceMenu: "AS-IS source",
    coverage: "Business coverage",
    empty: "No rows",
    moduleGroup: "Module group",
    status: "Ready",
    records: "Records",
    search: "Search",
    period: "Period",
    new: "New",
    save: "Save",
    delete: "Delete",
    submit: "Submit",
    approve: "Approve",
    reject: "Reject",
    detail: "Detail",
    refresh: "Refresh",
    cancel: "Cancel",
    locked: "Read only",
    related: "Related",
  },
  de: {
    loading: "Laedt",
    unavailable: "Fachmodul nicht verfuegbar",
    itemCount: "Datensaetze",
    primaryTable: "Primaertabelle",
    relatedTables: "Verbundene Tabellen",
    sourceMenu: "AS-IS Quelle",
    coverage: "Fachabdeckung",
    empty: "Keine Daten",
    moduleGroup: "Modulgruppe",
    status: "Bereit",
    records: "Datensaetze",
    search: "Suchen",
    period: "Periode",
    new: "Neu",
    save: "Speichern",
    delete: "Loeschen",
    submit: "Einreichen",
    approve: "Freigeben",
    reject: "Ablehnen",
    detail: "Detail",
    refresh: "Aktualisieren",
    cancel: "Abbrechen",
    locked: "Nur Lesen",
    related: "Bezug",
  },
  ko: {
    loading: "로딩 중",
    unavailable: "업무 모듈을 불러올 수 없습니다",
    itemCount: "데이터 건수",
    primaryTable: "대표 테이블",
    relatedTables: "연관 테이블",
    sourceMenu: "AS-IS 출처",
    coverage: "업무 커버리지",
    empty: "데이터 없음",
    moduleGroup: "모듈 그룹",
    status: "준비됨",
    records: "업무 데이터",
    search: "검색",
    period: "기간",
    new: "신규",
    save: "저장",
    delete: "삭제",
    submit: "상신",
    approve: "승인",
    reject: "반려",
    detail: "상세",
    refresh: "새로고침",
    cancel: "취소",
    locked: "조회 전용",
    related: "연관 데이터",
  },
} satisfies Record<LoginLocale, Record<string, string>>;

export function BusinessModulePage({
  accessToken,
  locale,
  moduleCd,
  onLoadingChange,
  onUnauthorized,
}: BusinessModulePageProps) {
  const [module, setModule] = useState<BusinessModuleRes | null>(null);
  const [records, setRecords] = useState<BusinessRecordListRes | null>(null);
  const [keyword, setKeyword] = useState("");
  const [yearMonth, setYearMonth] = useState("");
  const [selectedRow, setSelectedRow] = useState<BusinessRecordRow | null>(null);
  const [draft, setDraft] = useState<BusinessRecordRow | null>(null);
  const [related, setRelated] = useState<BusinessRelatedListRes | null>(null);
  const [failed, setFailed] = useState(false);
  const [recordsFailed, setRecordsFailed] = useState(false);
  const [relatedFailed, setRelatedFailed] = useState(false);
  const [saving, setSaving] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const copy = labels[locale] ?? labels.en;
  const selectedId = getRowId(selectedRow);

  const loadRecords = useCallback(
    async (signal?: AbortSignal) => {
      if (!accessToken) {
        onUnauthorized();
        return;
      }
      const params = new URLSearchParams({ lang: locale, page: "0", size: "50" });
      if (keyword.trim()) params.set("keyword", keyword.trim());
      if (yearMonth.trim()) params.set("yearMonth", yearMonth.trim());

      const response = await fetch(`${API_BASE_URL}/api/v1/business/modules/${moduleCd}/records?${params.toString()}`, {
        method: "GET",
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        cache: "no-store",
        signal,
      });
      if (!response.ok) {
        if (response.status === 401) {
          onUnauthorized();
          return;
        }
        throw new Error(`Business records fetch failed: ${response.status}`);
      }
      const payload = (await response.json()) as BusinessRecordListRes;
      setRecords(payload);
      setSelectedRow((current) => reconcileSelectedRow(current, payload));
      setDraft((current) => reconcileSelectedRow(current, payload));
      setRecordsFailed(false);
    },
    [accessToken, keyword, locale, moduleCd, onUnauthorized, yearMonth]
  );

  useEffect(() => {
    const controller = new AbortController();

    if (!accessToken) {
      onUnauthorized();
      return;
    }

    setFailed(false);
    onLoadingChange(true);

    const params = new URLSearchParams({ lang: locale });
    fetch(`${API_BASE_URL}/api/v1/business/modules/${moduleCd}?${params.toString()}`, {
      method: "GET",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      cache: "no-store",
    })
      .then(async (response) => {
        if (!response.ok) {
          if (response.status === 401) {
            onUnauthorized();
            return null;
          }
          throw new Error(`Business module fetch failed: ${response.status}`);
        }
        return response.json() as Promise<BusinessModuleRes>;
      })
      .then((payload) => {
        if (controller.signal.aborted || !payload) return;
        setModule(payload);
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setFailed(true);
          setModule(null);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          onLoadingChange(false);
        }
      });

    return () => {
      controller.abort();
    };
  }, [accessToken, locale, moduleCd, onLoadingChange, onUnauthorized]);

  useEffect(() => {
    const controller = new AbortController();
    setRecordsFailed(false);
    loadRecords(controller.signal).catch(() => {
      if (!controller.signal.aborted) {
        setRecordsFailed(true);
        setRecords(null);
      }
    });
    return () => controller.abort();
  }, [loadRecords, refreshKey]);

  useEffect(() => {
    const controller = new AbortController();
    if (!accessToken || selectedId == null) {
      setRelated(null);
      setRelatedFailed(false);
      return () => controller.abort();
    }

    fetch(`${API_BASE_URL}/api/v1/business/modules/${moduleCd}/records/${selectedId}/related`, {
      method: "GET",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      cache: "no-store",
      signal: controller.signal,
    })
      .then(async (response) => {
        if (!response.ok) {
          if (response.status === 401) {
            onUnauthorized();
            return null;
          }
          throw new Error(`Business related fetch failed: ${response.status}`);
        }
        return response.json() as Promise<BusinessRelatedListRes>;
      })
      .then((payload) => {
        if (controller.signal.aborted || !payload) return;
        setRelated(payload);
        setRelatedFailed(false);
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setRelated(null);
          setRelatedFailed(true);
        }
      });

    return () => controller.abort();
  }, [accessToken, moduleCd, onUnauthorized, selectedId]);

  const tableCount = module?.tableNms.length ?? 0;
  const tableRows = useMemo(
    () =>
      module?.tableNms.map((tableNm, index) => ({
        tableNm,
        role: index === 0 ? copy.primaryTable : copy.relatedTables,
      })) ?? [],
    [copy.primaryTable, copy.relatedTables, module?.tableNms]
  );
  const visibleColumns = useMemo(
    () => records?.columnList.filter((column) => column.visibleYn).sort((a, b) => a.dspOrd - b.dspOrd) ?? [],
    [records?.columnList]
  );
  const editableColumns = useMemo(
    () =>
      records?.columnList
        .filter((column) => column.writableYn && !column.keyYn)
        .sort((a, b) => a.dspOrd - b.dspOrd) ?? [],
    [records?.columnList]
  );
  const canEditSelection = Boolean(records?.editableYn && selectedId != null);
  const canDeleteSelection = Boolean(records?.deletableYn && selectedId != null);
  const canCreate = Boolean(records?.creatableYn);

  if (!module && !failed) {
    return (
      <div className="flex min-h-[360px] items-center justify-center rounded-[4px] border border-slate-200 bg-white">
        <div className="flex items-center gap-2 text-sm font-semibold text-slate-600">
          <RefreshCw className="h-4 w-4 animate-spin" />
          {copy.loading}
        </div>
      </div>
    );
  }

  if (!module) {
    return (
      <div className="flex min-h-[360px] items-center justify-center rounded-[4px] border border-red-100 bg-white">
        <div className="text-sm font-semibold text-red-700">{copy.unavailable}</div>
      </div>
    );
  }

  const activeDraft = draft ?? selectedRow;

  const handleRefresh = () => setRefreshKey((value) => value + 1);
  const handleSearchSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    handleRefresh();
  };
  const handleNew = () => {
    const emptyDraft = editableColumns.reduce<BusinessRecordRow>((acc, column) => {
      acc[column.columnNm] = defaultDraftValue(column);
      return acc;
    }, {});
    setSelectedRow(null);
    setDraft(emptyDraft);
  };
  const handleCancel = () => {
    setDraft(selectedRow);
  };
  const handleFieldChange = (column: BusinessRecordColumnRes, value: string | boolean) => {
    setDraft((current) => ({
      ...(current ?? selectedRow ?? {}),
      [column.columnNm]: value,
    }));
  };
  const handleSave = async () => {
    if (!accessToken || !activeDraft) return;
    const isUpdate = selectedId != null;
    if (isUpdate && !records?.editableYn) return;
    if (!isUpdate && !records?.creatableYn) return;

    setSaving(true);
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/v1/business/modules/${moduleCd}/records${isUpdate ? `/${selectedId}` : ""}`,
        {
          method: isUpdate ? "PUT" : "POST",
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({ values: activeDraft }),
        }
      );
      if (!response.ok) {
        if (response.status === 401) onUnauthorized();
        throw new Error(`Business record save failed: ${response.status}`);
      }
      const payload = (await response.json()) as BusinessRecordRes;
      setSelectedRow(payload.values);
      setDraft(payload.values);
      handleRefresh();
    } finally {
      setSaving(false);
    }
  };
  const handleDelete = async () => {
    if (!accessToken || selectedId == null || !records?.deletableYn) return;
    setSaving(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/business/modules/${moduleCd}/records/${selectedId}`, {
        method: "DELETE",
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
      });
      if (!response.ok) {
        if (response.status === 401) onUnauthorized();
        throw new Error(`Business record delete failed: ${response.status}`);
      }
      setSelectedRow(null);
      setDraft(null);
      handleRefresh();
    } finally {
      setSaving(false);
    }
  };
  const handleStatus = async (aprvStsCd: string) => {
    if (!accessToken || selectedId == null) return;
    setSaving(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/business/modules/${moduleCd}/records/${selectedId}/status`, {
        method: "PATCH",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({ aprvStsCd }),
      });
      if (!response.ok) {
        if (response.status === 401) onUnauthorized();
        throw new Error(`Business record status failed: ${response.status}`);
      }
      const payload = (await response.json()) as BusinessRecordRes;
      setSelectedRow(payload.values);
      setDraft(payload.values);
      handleRefresh();
    } finally {
      setSaving(false);
    }
  };

  return (
    <div id={`business-module-${module.moduleCd.toLowerCase()}`} className="space-y-3">
      <section className="rounded-[4px] border border-slate-200 bg-white px-4 py-3">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-lg font-semibold text-slate-900">{module.moduleNm}</h1>
              <span className="rounded-[4px] border border-slate-200 bg-slate-50 px-2 py-1 text-[11px] font-semibold text-slate-600">
                {module.menuCd}
              </span>
            </div>
            <p className="mt-1 max-w-3xl text-sm leading-5 text-slate-600">{module.description}</p>
          </div>
          <div className="rounded-[4px] border border-emerald-100 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700">
            {copy.status}
          </div>
        </div>
      </section>

      <section className="grid gap-3 md:grid-cols-4">
        <Metric icon={<Activity className="h-4 w-4" />} label={copy.itemCount} value={module.itemCnt.toLocaleString()} />
        <Metric icon={<Database className="h-4 w-4" />} label={copy.primaryTable} value={module.primaryTableNm} />
        <Metric icon={<GitBranch className="h-4 w-4" />} label={copy.relatedTables} value={tableCount.toString()} />
        <Metric icon={<ShieldCheck className="h-4 w-4" />} label={copy.moduleGroup} value={module.groupCd} />
      </section>

      <section className="grid gap-3 xl:grid-cols-[1.1fr_0.9fr]">
        <div className="rounded-[4px] border border-slate-200 bg-white">
          <div className="border-b border-slate-200 px-3 py-2 text-sm font-semibold text-slate-800">
            {copy.coverage}
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-3 py-2 font-semibold">Table</th>
                  <th className="px-3 py-2 font-semibold">Role</th>
                </tr>
              </thead>
              <tbody>
                {tableRows.map((row) => (
                  <tr key={row.tableNm} className="border-t border-slate-100">
                    <td className="px-3 py-2 font-mono text-xs text-slate-800">{row.tableNm}</td>
                    <td className="px-3 py-2 text-slate-600">{row.role}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="rounded-[4px] border border-slate-200 bg-white">
          <div className="border-b border-slate-200 px-3 py-2 text-sm font-semibold text-slate-800">
            {copy.sourceMenu}
          </div>
          <div className="space-y-2 px-3 py-3 text-sm">
            <InfoRow label="Legacy code" value={module.legacyMenuCd || copy.empty} />
            <InfoRow label="Legacy URI" value={module.legacyUri || copy.empty} mono />
            <InfoRow label="TO-BE menu" value={module.menuCd} mono />
            <InfoRow label="Module" value={module.moduleCd} mono />
          </div>
        </div>
      </section>

      <section className="rounded-[4px] border border-slate-200 bg-white">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 px-3 py-2">
          <div className="flex items-center gap-2 text-sm font-semibold text-slate-800">
            <Database className="h-4 w-4 text-slate-500" />
            {copy.records}
            {records ? (
              <span className="rounded-[4px] bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
                {records.totCnt.toLocaleString()}
              </span>
            ) : null}
          </div>
          <div className="flex items-center gap-2">
            {records && !records.editableYn ? (
              <span className="rounded-[4px] border border-slate-200 px-2 py-1 text-xs font-semibold text-slate-500">
                {copy.locked}
              </span>
            ) : null}
            <IconButton label={copy.refresh} onClick={handleRefresh}>
              <RefreshCw className="h-4 w-4" />
            </IconButton>
            <button
              type="button"
              onClick={handleNew}
              disabled={!canCreate || saving}
              className="inline-flex h-8 items-center gap-1.5 rounded-[4px] bg-slate-900 px-3 text-xs font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              <Plus className="h-4 w-4" />
              {copy.new}
            </button>
          </div>
        </div>

        <form onSubmit={handleSearchSubmit} className="flex flex-wrap items-center gap-2 border-b border-slate-100 px-3 py-2">
          <label className="relative min-w-[220px] flex-1">
            <Search className="pointer-events-none absolute left-2 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              placeholder={copy.search}
              className="h-9 w-full rounded-[4px] border border-slate-200 bg-white pl-8 pr-2 text-sm outline-none focus:border-slate-400"
            />
          </label>
          <label className="flex items-center gap-2 text-xs font-semibold uppercase text-slate-500">
            {copy.period}
            <input
              type="month"
              value={yearMonth}
              onChange={(event) => setYearMonth(event.target.value)}
              className="h-9 rounded-[4px] border border-slate-200 bg-white px-2 text-sm font-normal text-slate-800 outline-none focus:border-slate-400"
            />
          </label>
          <button
            type="submit"
            className="inline-flex h-9 items-center gap-1.5 rounded-[4px] border border-slate-300 px-3 text-xs font-semibold text-slate-700 hover:bg-slate-50"
          >
            <Search className="h-4 w-4" />
            {copy.search}
          </button>
        </form>

        <div className="grid min-h-[420px] xl:grid-cols-[minmax(0,1fr)_360px]">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  {visibleColumns.map((column) => (
                    <th key={column.columnNm} className="whitespace-nowrap px-3 py-2 font-semibold">
                      {column.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {records?.itemList.map((row, index) => {
                  const rowId = getRowId(row) ?? index;
                  const selected = selectedId != null && selectedId === getRowId(row);
                  return (
                    <tr
                      key={`${rowId}-${index}`}
                      onClick={() => {
                        setSelectedRow(row);
                        setDraft(row);
                      }}
                      className={cn(
                        "cursor-pointer border-t border-slate-100 hover:bg-slate-50",
                        selected && "bg-slate-100"
                      )}
                    >
                      {visibleColumns.map((column) => (
                        <td key={column.columnNm} className="max-w-[220px] truncate px-3 py-2 text-slate-700">
                          {formatCell(row[column.columnNm], column)}
                        </td>
                      ))}
                    </tr>
                  );
                })}
                {records && records.itemList.length === 0 ? (
                  <tr>
                    <td colSpan={Math.max(visibleColumns.length, 1)} className="px-3 py-10 text-center text-sm text-slate-500">
                      {copy.empty}
                    </td>
                  </tr>
                ) : null}
                {!records && !recordsFailed ? (
                  <tr>
                    <td colSpan={Math.max(visibleColumns.length, 1)} className="px-3 py-10 text-center text-sm text-slate-500">
                      {copy.loading}
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>

          <aside className="border-t border-slate-200 bg-slate-50 xl:border-l xl:border-t-0">
            <div className="flex items-center justify-between border-b border-slate-200 px-3 py-2">
              <div className="text-sm font-semibold text-slate-800">{copy.detail}</div>
              <button
                type="button"
                onClick={handleCancel}
                disabled={!draft}
                className="inline-flex h-8 items-center gap-1 rounded-[4px] px-2 text-xs font-semibold text-slate-600 hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
              >
                <X className="h-4 w-4" />
                {copy.cancel}
              </button>
            </div>

            <div className="space-y-3 p-3">
              {editableColumns.map((column) => (
                <Field
                  key={column.columnNm}
                  column={column}
                  value={activeDraft?.[column.columnNm] ?? ""}
                  disabled={saving || (!records?.editableYn && selectedId != null)}
                  onChange={(value) => handleFieldChange(column, value)}
                />
              ))}

              <div className="flex flex-wrap gap-2 pt-1">
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={saving || !activeDraft || (!canEditSelection && selectedId != null) || (!canCreate && selectedId == null)}
                  className="inline-flex h-9 items-center gap-1.5 rounded-[4px] bg-emerald-700 px-3 text-xs font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
                >
                  {saving ? <RefreshCw className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                  {copy.save}
                </button>
                <button
                  type="button"
                  onClick={handleDelete}
                  disabled={saving || !canDeleteSelection}
                  className="inline-flex h-9 items-center gap-1.5 rounded-[4px] border border-red-200 px-3 text-xs font-semibold text-red-700 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-300"
                >
                  <Trash2 className="h-4 w-4" />
                  {copy.delete}
                </button>
              </div>

              {records?.editableYn && selectedId != null && hasApprovalStatus(records.columnList) ? (
                <div className="flex flex-wrap gap-2 border-t border-slate-200 pt-3">
                  <button
                    type="button"
                    onClick={() => handleStatus("RQ")}
                    disabled={saving}
                    className="inline-flex h-8 items-center gap-1.5 rounded-[4px] border border-slate-200 bg-white px-2 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:text-slate-300"
                  >
                    <Activity className="h-4 w-4" />
                    {copy.submit}
                  </button>
                  <button
                    type="button"
                    onClick={() => handleStatus("CF")}
                    disabled={saving}
                    className="inline-flex h-8 items-center gap-1.5 rounded-[4px] border border-emerald-200 bg-white px-2 text-xs font-semibold text-emerald-700 disabled:cursor-not-allowed disabled:text-slate-300"
                  >
                    <CheckCircle2 className="h-4 w-4" />
                    {copy.approve}
                  </button>
                  <button
                    type="button"
                    onClick={() => handleStatus("RJ")}
                    disabled={saving}
                    className="inline-flex h-8 items-center gap-1.5 rounded-[4px] border border-red-200 bg-white px-2 text-xs font-semibold text-red-700 disabled:cursor-not-allowed disabled:text-slate-300"
                  >
                    <X className="h-4 w-4" />
                    {copy.reject}
                  </button>
                </div>
              ) : null}

              {selectedId != null ? (
                <div className="space-y-2 border-t border-slate-200 pt-3">
                  <div className="text-xs font-semibold uppercase text-slate-500">{copy.related}</div>
                  {related?.tableList.map((table) => (
                    <RelatedTable key={table.tableNm} table={table} />
                  ))}
                  {related && related.tableList.length === 0 ? (
                    <div className="rounded-[4px] border border-slate-200 bg-white px-3 py-2 text-xs text-slate-500">
                      {copy.empty}
                    </div>
                  ) : null}
                  {!related && !relatedFailed ? (
                    <div className="rounded-[4px] border border-slate-200 bg-white px-3 py-2 text-xs text-slate-500">
                      {copy.loading}
                    </div>
                  ) : null}
                </div>
              ) : null}
            </div>
          </aside>
        </div>
      </section>
    </div>
  );
}

function Metric({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="min-w-0 rounded-[4px] border border-slate-200 bg-white px-3 py-2">
      <div className="flex items-center gap-2 text-xs font-semibold uppercase text-slate-500">
        <span className="text-slate-400">{icon}</span>
        {label}
      </div>
      <div className="mt-1 truncate text-sm font-semibold text-slate-900">{value}</div>
    </div>
  );
}

function InfoRow({ label, mono, value }: { label: string; mono?: boolean; value: string }) {
  return (
    <div className="grid grid-cols-[110px_1fr] gap-2">
      <div className="text-xs font-semibold uppercase text-slate-500">{label}</div>
      <div className={cn("min-w-0 truncate text-slate-800", mono && "font-mono text-xs")}>{value}</div>
    </div>
  );
}

function Field({
  column,
  disabled,
  onChange,
  value,
}: {
  column: BusinessRecordColumnRes;
  disabled: boolean;
  onChange: (value: string | boolean) => void;
  value: string | number | boolean | null;
}) {
  const stringValue = value == null ? "" : String(value);
  if (column.dataTpCd === "yn") {
    return (
      <label className="flex h-10 items-center justify-between rounded-[4px] border border-slate-200 bg-white px-3">
        <span className="text-xs font-semibold text-slate-600">{column.label}</span>
        <input
          type="checkbox"
          checked={stringValue === "Y" || stringValue === "true"}
          disabled={disabled}
          onChange={(event) => onChange(event.target.checked ? "Y" : "N")}
          className="h-4 w-4"
        />
      </label>
    );
  }

  const inputType =
    column.dataTpCd === "date"
      ? "date"
      : column.dataTpCd === "time"
        ? "time"
        : column.dataTpCd === "datetime"
          ? "datetime-local"
          : column.dataTpCd === "number" || column.dataTpCd === "decimal"
            ? "number"
            : "text";

  return (
    <label className="block">
      <span className="mb-1 block text-xs font-semibold text-slate-600">{column.label}</span>
      <input
        type={inputType}
        step={column.dataTpCd === "decimal" ? "0.01" : undefined}
        value={inputType === "datetime-local" ? stringValue.slice(0, 16) : stringValue}
        disabled={disabled}
        required={column.requiredYn}
        onChange={(event) => onChange(event.target.value)}
        className="h-9 w-full rounded-[4px] border border-slate-200 bg-white px-2 text-sm text-slate-800 outline-none focus:border-slate-400 disabled:bg-slate-100 disabled:text-slate-400"
      />
    </label>
  );
}

function IconButton({ children, label, onClick }: { children: ReactNode; label: string; onClick: () => void }) {
  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      onClick={onClick}
      className="inline-flex h-8 w-8 items-center justify-center rounded-[4px] border border-slate-200 text-slate-600 hover:bg-slate-50"
    >
      {children}
    </button>
  );
}

function RelatedTable({ table }: { table: BusinessRelatedListRes["tableList"][number] }) {
  const previewColumns = table.columnList.slice(0, 4);
  return (
    <div className="rounded-[4px] border border-slate-200 bg-white">
      <div className="flex items-center justify-between border-b border-slate-100 px-2 py-1.5">
        <div className="truncate text-xs font-semibold text-slate-700">{table.label}</div>
        <span className="rounded-[4px] bg-slate-100 px-1.5 py-0.5 text-[11px] font-semibold text-slate-500">
          {table.totCnt.toLocaleString()}
        </span>
      </div>
      {table.itemList.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-[11px]">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                {previewColumns.map((column) => (
                  <th key={column} className="whitespace-nowrap px-2 py-1 font-semibold">
                    {column}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {table.itemList.slice(0, 3).map((row, index) => (
                <tr key={`${table.tableNm}-${index}`} className="border-t border-slate-100">
                  {previewColumns.map((column) => (
                    <td key={column} className="max-w-[120px] truncate px-2 py-1 text-slate-600">
                      {formatCell(row[column], { dataTpCd: "text" } as BusinessRecordColumnRes)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}

function formatCell(value: string | number | boolean | null | undefined, column: BusinessRecordColumnRes) {
  if (value == null || value === "") return "-";
  if (column.dataTpCd === "decimal" || column.dataTpCd === "number") {
    const num = Number(value);
    return Number.isFinite(num) ? num.toLocaleString() : String(value);
  }
  return String(value);
}

function defaultDraftValue(column: BusinessRecordColumnRes): string {
  if (column.dataTpCd === "yn") return "N";
  return "";
}

function getRowId(row: BusinessRecordRow | null): number | null {
  if (!row) return null;
  const raw = row.ID ?? row.EXP_DOC_ID ?? row.EXP_LINE_ID;
  const numeric = Number(raw);
  return Number.isFinite(numeric) ? numeric : null;
}

function reconcileSelectedRow(current: BusinessRecordRow | null, payload: BusinessRecordListRes): BusinessRecordRow | null {
  const currentId = getRowId(current);
  if (currentId == null) return current;
  return payload.itemList.find((row) => getRowId(row) === currentId) ?? null;
}

function hasApprovalStatus(columns: BusinessRecordColumnRes[]) {
  return columns.some((column) => column.columnNm === "APRV_STS_CD");
}

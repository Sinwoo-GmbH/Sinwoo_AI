"use client";

import type { KpiItem } from "@/lib/workspace/platform-shell-data";

type WorkspaceKpiGridProps = {
  items: KpiItem[];
};

export function WorkspaceKpiGrid({ items }: WorkspaceKpiGridProps) {
  return (
    <div id="workspace-kpi-grid" className="mt-2 grid gap-1.5 md:grid-cols-2 xl:grid-cols-4">
      {items.map((item, index) => (
        <div
          id={`workspace-kpi-card-${index + 1}`}
          key={`${item.label}-${index}`}
          className="rounded-[4px] border border-slate-300 bg-[#f8f9fb] px-2.5 py-2 shadow-none"
        >
          <div className="space-y-1">
            <div
              id={`workspace-kpi-label-${index + 1}`}
              className="text-[9px] font-medium uppercase tracking-[0.1em] text-slate-500"
            >
              {item.label}
            </div>
            <div
              id={`workspace-kpi-value-${index + 1}`}
              className="text-[18px] font-semibold leading-none text-slate-900"
            >
              {item.value}
            </div>
            <p id={`workspace-kpi-delta-${index + 1}`} className="text-[9px] leading-4 text-slate-500">
              {item.delta}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}

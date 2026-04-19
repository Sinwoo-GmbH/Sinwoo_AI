"use client";

import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { ViewModel } from "@/lib/workspace/platform-shell-data";

type WorkspaceGridSectionProps = {
  shellContent: WorkspaceShellContent;
  view: ViewModel;
};

export function WorkspaceGridSection({ shellContent, view }: WorkspaceGridSectionProps) {
  return (
    <WorkspaceSectionPanel
      id="workspace-grid-card"
      title={view.gridTitle}
      titleId="workspace-grid-title"
      description={shellContent.tableDescription}
      descriptionId="workspace-grid-description"
      contentClassName="space-y-3"
    >
      <WorkspaceContentContainer id="workspace-grid-container" className="space-y-3 bg-slate-50/55">
        <div
          id="workspace-grid-header"
          className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-3 rounded-2xl border border-slate-200 bg-slate-100 px-4 py-3 text-xs uppercase tracking-[0.22em] text-slate-500"
        >
          <div>{shellContent.name}</div>
          <div>{shellContent.owner}</div>
          <div>{shellContent.status}</div>
          <div>{shellContent.updated}</div>
        </div>
        {view.gridRows.map((row, index) => (
          <div
            id={`workspace-grid-row-${index + 1}`}
            key={`${row.name}-${row.updated}-${index}`}
            className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-4 text-sm shadow-[0_8px_18px_rgba(148,163,184,0.06)]"
          >
            <div id={`workspace-grid-name-${index + 1}`} className="font-medium text-slate-950">
              {row.name}
            </div>
            <div id={`workspace-grid-owner-${index + 1}`} className="text-slate-600">
              {row.owner}
            </div>
            <div id={`workspace-grid-status-${index + 1}`} className="text-slate-700">
              {row.status}
            </div>
            <div id={`workspace-grid-updated-${index + 1}`} className="text-slate-500">
              {row.updated}
            </div>
          </div>
        ))}
      </WorkspaceContentContainer>
    </WorkspaceSectionPanel>
  );
}

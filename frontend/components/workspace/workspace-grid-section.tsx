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
      contentClassName="space-y-2"
    >
      <WorkspaceContentContainer id="workspace-grid-container" className="space-y-2 bg-white">
        <div
          id="workspace-grid-header"
          className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-2 rounded-[3px] border border-slate-300 bg-[#eef1f4] px-3 py-1.5 text-[9px] uppercase tracking-[0.08em] text-slate-500"
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
            className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-2 rounded-[3px] border border-slate-300 bg-white px-3 py-2 text-[11px]"
          >
            <div id={`workspace-grid-name-${index + 1}`} className="font-medium leading-4 text-slate-950">
              {row.name}
            </div>
            <div id={`workspace-grid-owner-${index + 1}`} className="leading-4 text-slate-600">
              {row.owner}
            </div>
            <div id={`workspace-grid-status-${index + 1}`} className="leading-4 text-slate-700">
              {row.status}
            </div>
            <div id={`workspace-grid-updated-${index + 1}`} className="leading-4 text-slate-500">
              {row.updated}
            </div>
          </div>
        ))}
      </WorkspaceContentContainer>
    </WorkspaceSectionPanel>
  );
}

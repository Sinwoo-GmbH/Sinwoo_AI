"use client";

import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { FeedItem } from "@/lib/workspace/platform-shell-data";
import { cn } from "@/lib/utils";

type WorkspacePrioritySectionProps = {
  items: FeedItem[];
  shellContent: WorkspaceShellContent;
};

export function WorkspacePrioritySection({
  items,
  shellContent,
}: WorkspacePrioritySectionProps) {
  return (
    <WorkspaceSectionPanel
      id="workspace-priority-card"
      title={shellContent.focusTitle}
      titleId="workspace-priority-title"
      description={shellContent.focusDescription}
      descriptionId="workspace-priority-description"
      contentClassName="space-y-3"
    >
      {items.map((item, index) => (
        <div
          id={`workspace-highlight-card-${index + 1}`}
          key={`${item.title}-${index}`}
          className={cn(
            "rounded-2xl border p-4",
            item.emphasis === "warning"
              ? "border-amber-200 bg-amber-50"
              : item.emphasis === "success"
                ? "border-emerald-200 bg-emerald-50"
                : "border-slate-200 bg-slate-50"
          )}
        >
          <div
            id={`workspace-highlight-title-${index + 1}`}
            className="font-medium text-slate-950"
          >
            {item.title}
          </div>
          <div id={`workspace-highlight-meta-${index + 1}`} className="mt-1 text-sm text-slate-500">
            {item.meta}
          </div>
        </div>
      ))}
    </WorkspaceSectionPanel>
  );
}

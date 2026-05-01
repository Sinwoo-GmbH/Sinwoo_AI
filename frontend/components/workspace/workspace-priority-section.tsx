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
      contentClassName="space-y-2"
    >
      {items.map((item, index) => (
        <div
          id={`workspace-highlight-card-${index + 1}`}
          key={`${item.title}-${index}`}
          className={cn(
            "rounded-[3px] border px-3 py-2",
            item.emphasis === "warning"
              ? "border-amber-200 bg-[#faf6ed]"
              : item.emphasis === "success"
                ? "border-emerald-200 bg-[#eef8f1]"
                : "border-slate-300 bg-white"
          )}
        >
          <div
            id={`workspace-highlight-title-${index + 1}`}
            className="text-[11px] font-medium leading-4 text-slate-950"
          >
            {item.title}
          </div>
          <div id={`workspace-highlight-meta-${index + 1}`} className="mt-0.5 text-[10px] leading-4 text-slate-500">
            {item.meta}
          </div>
        </div>
      ))}
    </WorkspaceSectionPanel>
  );
}

"use client";

import { ChevronDown, ChevronLeft, ChevronRight, Workflow } from "lucide-react";

import type { WspShellCnt } from "@/lib/i18n/wsp-shell-cnt";
import type { MnuNode } from "@/lib/utils/wsp/platform-shell-data";
import { resolveWspIcon } from "@/lib/utils/wsp/wsp-icon-map";
import { cn } from "@/lib/utils";

type WspSidebarProps = {
  activeTabId: string;
  expandedMnuIds: string[];
  label: string;
  mnus: MnuNode[];
  onMnuSelect: (mnu: MnuNode) => void;
  onToggleCollapsed: () => void;
  sidebarCollapsed: boolean;
  shellCnt: WspShellCnt;
};

type WspSidebarNodeProps = {
  activeTabId: string;
  depth?: number;
  expandedMnuIds: string[];
  mnu: MnuNode;
  onMnuSelect: (mnu: MnuNode) => void;
  sidebarCollapsed: boolean;
};

function WspSidebarNode({
  activeTabId,
  depth = 1,
  expandedMnuIds,
  mnu,
  onMnuSelect,
  sidebarCollapsed,
}: WspSidebarNodeProps) {
  const isExpanded = expandedMnuIds.includes(mnu.id);
  const hasChildren = Boolean(mnu.children?.length);
  const Icon = mnu.icon ? resolveWspIcon(mnu.icon) : Workflow;
  const isActiveLeaf = !hasChildren && activeTabId === mnu.id;

  return (
    <div id={`wsp-mnu-node-${mnu.id}`} className="space-y-1">
      <button
        id={`wsp-mnu-button-${mnu.id}`}
        type="button"
        onClick={() => onMnuSelect(mnu)}
        className={cn(
          "flex w-full items-center gap-2 rounded-[3px] border border-transparent px-2 py-1 text-left leading-4 transition-colors",
          depth === 1 ? "font-semibold" : "font-medium",
          isActiveLeaf
            ? "border-[#31588f] bg-[#31588f] text-white"
            : "text-slate-600 hover:border-slate-300 hover:bg-slate-100 hover:text-slate-900",
          depth === 1 ? "text-[11px]" : "",
          depth === 2 ? "py-1 text-[10px] text-slate-500" : "",
          depth >= 3 ? "py-0.5 text-[10px] text-slate-400" : "",
          sidebarCollapsed && depth === 1 ? "justify-center px-1.5" : ""
        )}
      >
        {depth === 1 ? (
          <Icon className="h-3.5 w-3.5 shrink-0" />
        ) : (
          <span className="block h-1 w-1 bg-slate-300" />
        )}

        {!sidebarCollapsed ? (
          <span
            className={cn(
              depth === 1 ? "text-[13px]" : "",
              depth === 2 ? "text-[10px]" : "",
              depth >= 3 ? "text-[10px]" : ""
            )}
          >
            {mnu.title}
          </span>
        ) : null}

        {!sidebarCollapsed && hasChildren ? (
          <ChevronDown
            className={cn(
              "ml-auto h-3.5 w-3.5 transition-transform",
              isExpanded ? "rotate-180" : ""
            )}
          />
        ) : null}
      </button>

      {!sidebarCollapsed && hasChildren && isExpanded ? (
        <div
          id={`wsp-mnu-children-${mnu.id}`}
          className={cn(
            "border-l border-slate-200 pl-2",
            depth === 1 ? "ml-2 space-y-0.5" : "ml-1.5 space-y-0.5"
          )}
        >
          {mnu.children?.map((child) => (
            <WspSidebarNode
              key={child.id}
              activeTabId={activeTabId}
              depth={Math.min(depth + 1, 4)}
              expandedMnuIds={expandedMnuIds}
              mnu={child}
              onMnuSelect={onMnuSelect}
              sidebarCollapsed={sidebarCollapsed}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function WspSidebar({
  activeTabId,
  expandedMnuIds,
  label,
  mnus,
  onMnuSelect,
  onToggleCollapsed,
  sidebarCollapsed,
  shellCnt,
}: WspSidebarProps) {
  return (
    <aside
      id="wsp-sidebar"
      className={cn(
        "wsp-scrollbar self-stretch overflow-y-auto rounded-[4px] border border-slate-300 bg-[#f8f9fb] p-2 shadow-none transition-all",
        sidebarCollapsed ? "w-[78px]" : "w-[238px]"
      )}
    >
      <div id="wsp-sidebar-hdr" className="mb-3 flex items-center justify-between border-b border-slate-200 pb-2">
        {!sidebarCollapsed ? (
          <div>
            <p
              id="wsp-sidebar-eyebrow"
              className="text-[9px] uppercase tracking-[0.12em] text-slate-400"
            >
              {shellCnt.navigation}
            </p>
            <p id="wsp-sidebar-label" className="mt-0.5 text-[11px] font-semibold text-slate-700">
              {label}
            </p>
          </div>
        ) : (
          <div className="text-[9px] uppercase tracking-[0.12em] text-slate-400">
            {shellCnt.navShort}
          </div>
        )}

        <button
          id="wsp-sidebar-toggle"
          type="button"
          onClick={onToggleCollapsed}
          className="rounded-[3px] border border-slate-300 p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-950"
        >
          {sidebarCollapsed ? (
            <ChevronRight className="h-3.5 w-3.5" />
          ) : (
            <ChevronLeft className="h-3.5 w-3.5" />
          )}
        </button>
      </div>

      <div id="wsp-sidebar-tree" className="space-y-1">
        {mnus.map((mnu) => (
          <WspSidebarNode
            key={mnu.id}
            activeTabId={activeTabId}
            expandedMnuIds={expandedMnuIds}
            mnu={mnu}
            onMnuSelect={onMnuSelect}
            sidebarCollapsed={sidebarCollapsed}
          />
        ))}
      </div>
    </aside>
  );
}

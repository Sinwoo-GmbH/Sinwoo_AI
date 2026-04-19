"use client";

import type { CSSProperties } from "react";

import type { WorkspaceTabContextMenuContent } from "@/lib/i18n/workspace-shell-content";

type WorkspaceTabContextMenuProps = {
  defaultTabId: string;
  labels: WorkspaceTabContextMenuContent;
  onCloseAllTabs: () => void;
  onCloseOtherTabs: (tabId: string) => void;
  onCloseTab: (tabId: string) => void;
  style?: CSSProperties;
  tabId: string;
};

export function WorkspaceTabContextMenu({
  defaultTabId,
  labels,
  onCloseAllTabs,
  onCloseOtherTabs,
  onCloseTab,
  style,
  tabId,
}: WorkspaceTabContextMenuProps) {
  return (
    <div
      id="workspace-tab-context-menu"
      className="absolute z-50 w-[164px] rounded-xl border border-slate-200 bg-[#F7FAFF] p-1.5 shadow-[0_16px_40px_rgba(15,23,42,0.16)]"
      style={style}
      onClick={(event) => event.stopPropagation()}
    >
      <button
        id="workspace-tab-context-close"
        type="button"
        disabled={tabId === defaultTabId}
        onClick={() => onCloseTab(tabId)}
        className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100 disabled:cursor-default disabled:text-slate-300 disabled:hover:bg-transparent"
      >
        {labels.close}
      </button>
      <button
        id="workspace-tab-context-close-others"
        type="button"
        onClick={() => onCloseOtherTabs(tabId)}
        className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100"
      >
        {labels.closeOther}
      </button>
      <button
        id="workspace-tab-context-close-all"
        type="button"
        onClick={onCloseAllTabs}
        className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100"
      >
        {labels.closeAll}
      </button>
    </div>
  );
}

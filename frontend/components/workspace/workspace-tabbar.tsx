"use client";

import type { MouseEvent as ReactMouseEvent, RefObject } from "react";
import { ChevronLeft, ChevronRight, X } from "lucide-react";

import type { TabItem } from "@/lib/workspace/platform-shell-data";
import { cn } from "@/lib/utils";

type WorkspaceTabbarProps = {
  activeTabId: string;
  canScrollTabsLeft: boolean;
  canScrollTabsRight: boolean;
  defaultTabId: string;
  desktopTabs: TabItem[];
  getTabTitle: (tabId: string, fallbackTitle?: string) => string;
  onCloseTab: (tabId: string) => void;
  onOpenContextMenu: (event: ReactMouseEvent<HTMLElement>, tabId: string) => void;
  onScrollTabs: (direction: "left" | "right") => void;
  onSelectTab: (tabId: string) => void;
  showTabScrollControls: boolean;
  tabScrollRef: RefObject<HTMLDivElement | null>;
};

export function WorkspaceTabbar({
  activeTabId,
  canScrollTabsLeft,
  canScrollTabsRight,
  defaultTabId,
  desktopTabs,
  getTabTitle,
  onCloseTab,
  onOpenContextMenu,
  onScrollTabs,
  onSelectTab,
  showTabScrollControls,
  tabScrollRef,
}: WorkspaceTabbarProps) {
  return (
    <div
      id="workspace-tabbar"
      className="relative z-10 border-b border-slate-300 bg-[#f3f5f8]"
    >
      <div id="workspace-tabbar-scroll" className="flex h-[34px] items-end gap-1 px-1.5 pb-0 pt-0">
        <div
          id="workspace-tabbar-track"
          ref={tabScrollRef}
          className="workspace-tab-scroll h-full min-w-0 flex-1 overflow-x-auto overflow-y-hidden"
        >
          <div id="workspace-tab-list" className="flex h-full w-max items-end gap-px">
            {desktopTabs.map((tab) => {
              const isActive = tab.id === activeTabId;

              return (
                <div
                  id={`workspace-tab-${tab.id}`}
                  key={tab.id}
                  onContextMenu={(event) => onOpenContextMenu(event, tab.id)}
                  className={cn(
                    "group relative mb-[-1px] inline-flex min-w-fit items-center gap-1 rounded-t-[3px] border border-b-0 px-2 py-1 text-[11px] leading-4 transition-all",
                    isActive
                      ? "z-10 border-slate-300 bg-[#f8f9fb] text-slate-900"
                      : "translate-y-[2px] border-slate-300 bg-[#e9edf2] text-slate-500 hover:border-slate-400 hover:bg-[#edf1f5] hover:text-slate-700"
                  )}
                >
                  <button
                    id={`workspace-tab-button-${tab.id}`}
                    type="button"
                    onContextMenu={(event) => onOpenContextMenu(event, tab.id)}
                    onClick={() => onSelectTab(tab.id)}
                    className="max-w-[180px] truncate"
                  >
                    {getTabTitle(tab.id, tab.title)}
                  </button>

                  {tab.id !== defaultTabId ? (
                    <button
                      id={`workspace-tab-close-${tab.id}`}
                      type="button"
                      onClick={() => onCloseTab(tab.id)}
                      className={cn(
                        "rounded-[3px] p-0.5 text-slate-400 transition-all hover:bg-slate-200 hover:text-slate-900",
                        isActive ? "opacity-100" : "opacity-0 group-hover:opacity-100"
                      )}
                      onContextMenu={(event) => onOpenContextMenu(event, tab.id)}
                    >
                      <X className="h-3.5 w-3.5" />
                    </button>
                  ) : null}
                </div>
              );
            })}
          </div>
        </div>

        {showTabScrollControls ? (
          <div
            id="workspace-tab-scroll-controls"
            className="mb-0.5 flex shrink-0 self-end items-center gap-1 rounded-[3px] border border-slate-300 bg-white px-1 py-0.5"
          >
            <button
              id="workspace-tab-scroll-prev"
              type="button"
              onClick={() => onScrollTabs("left")}
              disabled={!canScrollTabsLeft}
              className="rounded-[3px] p-0.5 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
            >
              <ChevronLeft className="h-3.5 w-3.5" />
            </button>
            <button
              id="workspace-tab-scroll-next"
              type="button"
              onClick={() => onScrollTabs("right")}
              disabled={!canScrollTabsRight}
              className="rounded-[3px] p-0.5 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
            >
              <ChevronRight className="h-3.5 w-3.5" />
            </button>
          </div>
        ) : null}
      </div>
    </div>
  );
}

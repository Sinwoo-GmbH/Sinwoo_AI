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
      className="relative z-10 border-b border-slate-200 bg-[linear-gradient(180deg,#F7F9FD_0%,#EEF3FB_100%)]"
    >
      <div id="workspace-tabbar-scroll" className="flex h-[42px] items-end gap-1.5 px-2 pb-0 pt-0">
        <div
          id="workspace-tabbar-track"
          ref={tabScrollRef}
          className="workspace-tab-scroll h-full min-w-0 flex-1 overflow-x-auto overflow-y-hidden"
        >
          <div id="workspace-tab-list" className="flex h-full w-max items-end gap-[0.1rem]">
            {desktopTabs.map((tab) => {
              const isActive = tab.id === activeTabId;

              return (
                <div
                  id={`workspace-tab-${tab.id}`}
                  key={tab.id}
                  onContextMenu={(event) => onOpenContextMenu(event, tab.id)}
                  className={cn(
                    "group relative mb-[-1px] inline-flex min-w-fit items-center gap-1.5 rounded-t-[12px] border border-b-0 px-3 py-1 text-[12px] leading-5 transition-all",
                    isActive
                      ? "z-10 border-slate-300 bg-white text-slate-950 shadow-[0_-1px_0_rgba(255,255,255,0.85)]"
                      : "translate-y-[4px] border-slate-200/70 bg-[linear-gradient(180deg,#F4F7FC_0%,#E6EDF8_100%)] text-slate-500 hover:border-slate-300 hover:bg-[linear-gradient(180deg,#F8FAFE_0%,#EDF3FB_100%)] hover:text-slate-700"
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
                        "rounded-full p-0.5 text-slate-400 transition-all hover:bg-slate-200 hover:text-slate-900",
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
            className="mb-1 flex shrink-0 self-end items-center gap-1 rounded-xl border border-slate-200 bg-white/92 px-1 py-0.5 shadow-sm backdrop-blur"
          >
            <button
              id="workspace-tab-scroll-prev"
              type="button"
              onClick={() => onScrollTabs("left")}
              disabled={!canScrollTabsLeft}
              className="rounded-md p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <button
              id="workspace-tab-scroll-next"
              type="button"
              onClick={() => onScrollTabs("right")}
              disabled={!canScrollTabsRight}
              className="rounded-md p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        ) : null}
      </div>
    </div>
  );
}

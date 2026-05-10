"use client";

import type { CSSProperties } from "react";

import type { WspTabCtxMnuCnt } from "@/lib/i18n/wsp-shell-cnt";

type WspTabCtxMnuProps = {
  defaultTabId: string;
  labels: WspTabCtxMnuCnt;
  onCloseAllTabs: () => void;
  onCloseOtherTabs: (tabId: string) => void;
  onCloseTab: (tabId: string) => void;
  style?: CSSProperties;
  tabId: string;
};

export function WspTabCtxMnu({
  defaultTabId,
  labels,
  onCloseAllTabs,
  onCloseOtherTabs,
  onCloseTab,
  style,
  tabId,
}: WspTabCtxMnuProps) {
  return (
    <div
      id="wsp-tab-ctx-mnu"
      className="absolute z-50 w-[156px] rounded-[4px] border border-slate-300 bg-white p-1 shadow-[0_6px_14px_rgba(15,23,42,0.10)]"
      style={style}
      onClick={(event) => event.stopPropagation()}
    >
      <button
        id="wsp-tab-context-close"
        type="button"
        disabled={tabId === defaultTabId}
        onClick={() => onCloseTab(tabId)}
        className="flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[11px] text-slate-700 transition-colors hover:bg-slate-100 disabled:cursor-default disabled:text-slate-300 disabled:hover:bg-transparent"
      >
        {labels.close}
      </button>
      <button
        id="wsp-tab-context-close-others"
        type="button"
        onClick={() => onCloseOtherTabs(tabId)}
        className="flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[11px] text-slate-700 transition-colors hover:bg-slate-100"
      >
        {labels.closeOther}
      </button>
      <button
        id="wsp-tab-context-close-all"
        type="button"
        onClick={onCloseAllTabs}
        className="flex w-full items-center rounded-[3px] px-2 py-1 text-left text-[11px] text-slate-700 transition-colors hover:bg-slate-100"
      >
        {labels.closeAll}
      </button>
    </div>
  );
}

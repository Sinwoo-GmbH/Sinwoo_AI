"use client";

import { WspAttndCard } from "@/components/layout/wsp/wsp-attnd-card";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import type { WspShellCnt } from "@/lib/i18n/wsp-shell-cnt";
import type { ViewModel, WspMode } from "@/lib/utils/wsp/platform-shell-data";

type WspHeroSectProps = {
  accessToken: string | null;
  desktopTabsCount: number;
  locale: LoginLocale;
  mode: WspMode;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
  shellCnt: WspShellCnt;
  sidebarCollapsed: boolean;
  view: ViewModel;
};

export function WspHeroSect({
  accessToken,
  desktopTabsCount,
  locale,
  mode,
  onLoadingChange,
  onUnauthorized,
  shellCnt,
  sidebarCollapsed,
  view,
}: WspHeroSectProps) {
  return (
    <div id="wsp-hero-grid" className="grid gap-3 xl:grid-cols-[1.15fr_0.85fr]">
      <WspPageHdr
        id="wsp-hero-card"
        eyebrow={view.eyebrow}
        eyebrowId="wsp-hero-eyebrow"
        title={view.title}
        titleId="wsp-hero-title"
        desc={view.desc}
        descId="wsp-hero-desc"
        actionsId="wsp-page-hdr-actions"
        actions={
          <>
            <div
              id="wsp-page-hdr-action-mode"
              className="min-w-[108px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.modeLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">
                {mode === "admin" ? shellCnt.modeAdminValue : shellCnt.modeClientValue}
              </div>
            </div>
            <div
              id="wsp-page-hdr-action-sidebar"
              className="min-w-[108px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.sidebarLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">
                {sidebarCollapsed ? shellCnt.collapsed : shellCnt.expanded}
              </div>
            </div>
            <div
              id="wsp-page-hdr-action-tabs"
              className="min-w-[88px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.tabsLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">{desktopTabsCount}</div>
            </div>
          </>
        }
        footerId="wsp-hero-stats"
        footer={
          <div className="grid gap-2 md:grid-cols-3">
            <div
              id="wsp-stat-mode"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.modeLabel}
              </div>
              <div className="mt-1 text-[14px] font-semibold leading-4 text-slate-900">
                {mode === "admin" ? shellCnt.modeAdminValue : shellCnt.modeClientValue}
              </div>
            </div>
            <div
              id="wsp-stat-sidebar"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.sidebarLabel}
              </div>
              <div className="mt-1 text-[14px] font-semibold leading-4 text-slate-900">
                {sidebarCollapsed ? shellCnt.collapsed : shellCnt.expanded}
              </div>
            </div>
            <div
              id="wsp-stat-tabs"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellCnt.tabsLabel}
              </div>
              <div className="mt-1 text-[14px] font-semibold leading-4 text-slate-900">{desktopTabsCount}</div>
            </div>
          </div>
        }
        className="border-slate-300 bg-[#f8f9fb] text-slate-900"
        eyebrowClassName="text-slate-400"
        titleClassName="text-[18px] font-semibold leading-5 text-slate-900"
        descriptionClassName="text-[11px] leading-4 text-slate-500"
      />

      {mode === "client" ? (
        <WspAttndCard
          accessToken={accessToken}
          locale={locale}
          onUnauthorized={onUnauthorized}
          onLoadingChange={onLoadingChange}
        />
      ) : (
        <WspSectPanel
          id="wsp-rules-card"
          eyebrow={shellCnt.quickActs}
          eyebrowId="wsp-rules-eyebrow"
          contentClassName="space-y-2 text-[11px] text-slate-600"
        >
          <div id="wsp-rule-client-admin-switch" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellCnt.ruleOne}
          </div>
          <div id="wsp-rule-depth-mnu" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellCnt.ruleTwo}
          </div>
          <div id="wsp-rule-active-wsp" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellCnt.ruleThree}
          </div>
        </WspSectPanel>
      )}
    </div>
  );
}

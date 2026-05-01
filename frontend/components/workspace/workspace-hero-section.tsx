"use client";

import { WorkspaceAttendanceCard } from "@/components/workspace/workspace-attendance-card";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { LoginLocale } from "@/lib/i18n/login-content";
import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { ViewModel, WorkspaceMode } from "@/lib/workspace/platform-shell-data";

type WorkspaceHeroSectionProps = {
  accessToken: string | null;
  desktopTabsCount: number;
  locale: LoginLocale;
  mode: WorkspaceMode;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
  shellContent: WorkspaceShellContent;
  sidebarCollapsed: boolean;
  view: ViewModel;
};

export function WorkspaceHeroSection({
  accessToken,
  desktopTabsCount,
  locale,
  mode,
  onLoadingChange,
  onUnauthorized,
  shellContent,
  sidebarCollapsed,
  view,
}: WorkspaceHeroSectionProps) {
  return (
    <div id="workspace-hero-grid" className="grid gap-3 xl:grid-cols-[1.15fr_0.85fr]">
      <WorkspacePageHeader
        id="workspace-hero-card"
        eyebrow={view.eyebrow}
        eyebrowId="workspace-hero-eyebrow"
        title={view.title}
        titleId="workspace-hero-title"
        description={view.description}
        descriptionId="workspace-hero-description"
        actionsId="workspace-page-header-actions"
        actions={
          <>
            <div
              id="workspace-page-header-action-mode"
              className="min-w-[108px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.modeLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">
                {mode === "admin" ? shellContent.modeAdminValue : shellContent.modeClientValue}
              </div>
            </div>
            <div
              id="workspace-page-header-action-sidebar"
              className="min-w-[108px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.sidebarLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">
                {sidebarCollapsed ? shellContent.collapsed : shellContent.expanded}
              </div>
            </div>
            <div
              id="workspace-page-header-action-tabs"
              className="min-w-[88px] rounded-[3px] border border-slate-300 bg-white px-2.5 py-1.5 text-left"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.tabsLabel}
              </div>
              <div className="mt-0.5 text-[11px] font-semibold text-slate-800">{desktopTabsCount}</div>
            </div>
          </>
        }
        footerId="workspace-hero-stats"
        footer={
          <div className="grid gap-2 md:grid-cols-3">
            <div
              id="workspace-stat-mode"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.modeLabel}
              </div>
              <div className="mt-1 text-[14px] font-semibold leading-4 text-slate-900">
                {mode === "admin" ? shellContent.modeAdminValue : shellContent.modeClientValue}
              </div>
            </div>
            <div
              id="workspace-stat-sidebar"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.sidebarLabel}
              </div>
              <div className="mt-1 text-[14px] font-semibold leading-4 text-slate-900">
                {sidebarCollapsed ? shellContent.collapsed : shellContent.expanded}
              </div>
            </div>
            <div
              id="workspace-stat-tabs"
              className="rounded-[3px] border border-slate-300 bg-white px-3 py-2"
            >
              <div className="text-[8px] uppercase tracking-[0.12em] text-slate-400">
                {shellContent.tabsLabel}
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
        <WorkspaceAttendanceCard
          accessToken={accessToken}
          locale={locale}
          onUnauthorized={onUnauthorized}
          onLoadingChange={onLoadingChange}
        />
      ) : (
        <WorkspaceSectionPanel
          id="workspace-rules-card"
          eyebrow={shellContent.quickActions}
          eyebrowId="workspace-rules-eyebrow"
          contentClassName="space-y-2 text-[11px] text-slate-600"
        >
          <div id="workspace-rule-client-admin-switch" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellContent.ruleOne}
          </div>
          <div id="workspace-rule-depth-menu" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellContent.ruleTwo}
          </div>
          <div id="workspace-rule-active-workspace" className="rounded-[3px] border border-slate-300 bg-white px-3 py-2">
            {shellContent.ruleThree}
          </div>
        </WorkspaceSectionPanel>
      )}
    </div>
  );
}

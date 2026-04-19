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
    <div id="workspace-hero-grid" className="grid gap-5 xl:grid-cols-[1.15fr_0.85fr]">
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
              className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
            >
              <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">
                {shellContent.modeLabel}
              </div>
              <div className="mt-1 text-sm font-semibold">
                {mode === "admin" ? shellContent.modeAdminValue : shellContent.modeClientValue}
              </div>
            </div>
            <div
              id="workspace-page-header-action-sidebar"
              className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
            >
              <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">
                {shellContent.sidebarLabel}
              </div>
              <div className="mt-1 text-sm font-semibold">
                {sidebarCollapsed ? shellContent.collapsed : shellContent.expanded}
              </div>
            </div>
            <div
              id="workspace-page-header-action-tabs"
              className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
            >
              <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">
                {shellContent.tabsLabel}
              </div>
              <div className="mt-1 text-sm font-semibold">{desktopTabsCount}</div>
            </div>
          </>
        }
        footerId="workspace-hero-stats"
        footer={
          <div className="grid gap-3 md:grid-cols-3">
            <div
              id="workspace-stat-mode"
              className="rounded-2xl border border-white/15 bg-white/10 p-4"
            >
              <div className="text-xs uppercase tracking-[0.24em] text-slate-200">
                {shellContent.modeLabel}
              </div>
              <div className="mt-2 text-xl font-semibold">
                {mode === "admin" ? shellContent.modeAdminValue : shellContent.modeClientValue}
              </div>
            </div>
            <div
              id="workspace-stat-sidebar"
              className="rounded-2xl border border-white/15 bg-white/10 p-4"
            >
              <div className="text-xs uppercase tracking-[0.24em] text-slate-200">
                {shellContent.sidebarLabel}
              </div>
              <div className="mt-2 text-xl font-semibold">
                {sidebarCollapsed ? shellContent.collapsed : shellContent.expanded}
              </div>
            </div>
            <div
              id="workspace-stat-tabs"
              className="rounded-2xl border border-white/15 bg-white/10 p-4"
            >
              <div className="text-xs uppercase tracking-[0.24em] text-slate-200">
                {shellContent.tabsLabel}
              </div>
              <div className="mt-2 text-xl font-semibold">{desktopTabsCount}</div>
            </div>
          </div>
        }
        className="border-white bg-gradient-to-br from-[#132C67] via-[#1F4FAE] to-[#4F72C8] text-white shadow-[0_24px_60px_rgba(35,58,122,0.28)]"
        eyebrowClassName="text-slate-200"
        titleClassName="text-4xl font-bold text-white"
        descriptionClassName="text-base text-slate-100"
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
          contentClassName="space-y-3 text-sm text-slate-600"
        >
          <div id="workspace-rule-client-admin-switch" className="rounded-2xl bg-slate-50 p-4">
            {shellContent.ruleOne}
          </div>
          <div id="workspace-rule-depth-menu" className="rounded-2xl bg-slate-50 p-4">
            {shellContent.ruleTwo}
          </div>
          <div id="workspace-rule-active-workspace" className="rounded-2xl bg-slate-50 p-4">
            {shellContent.ruleThree}
          </div>
        </WorkspaceSectionPanel>
      )}
    </div>
  );
}

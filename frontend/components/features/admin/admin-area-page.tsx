"use client";

import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceAdminAreaMessages } from "@/lib/i18n/workspace-content";

type AdminAreaPageProps = {
  locale: LoginLocale;
};

export function AdminAreaPage({ locale }: AdminAreaPageProps) {
  const messages = getWorkspaceAdminAreaMessages(locale);

  return (
    <div id="workspace-admin-area-page" className="space-y-2">
      <WorkspacePageHeader
        strip
        id="workspace-client-admin-header"
        title={messages.title}
        titleId="workspace-client-admin-title"
      />

      <WorkspaceSectionPanel
        id="workspace-client-admin-card"
        title={messages.sectionTitle}
        titleId="workspace-client-admin-card-title"
        className="border-slate-200/90 shadow-[0_12px_24px_rgba(148,163,184,0.06)]"
        headerClassName="px-3 py-2"
        contentClassName="px-3 py-3 pt-2"
      >
        <WorkspaceContentContainer id="workspace-client-admin-container" className="bg-slate-50/60">
          <div
            id="workspace-client-admin-placeholder"
            className="rounded-[16px] border border-dashed border-slate-200 bg-white px-4 py-8 text-center"
          >
            <div
              id="workspace-client-admin-placeholder-title"
              className="text-sm font-semibold text-slate-700"
            >
              {messages.placeholderTitle}
            </div>
            <div
              id="workspace-client-admin-placeholder-description"
              className="mt-1.5 text-sm leading-5 text-slate-500"
            >
              {messages.placeholderDescription}
            </div>
          </div>
        </WorkspaceContentContainer>
      </WorkspaceSectionPanel>
    </div>
  );
}

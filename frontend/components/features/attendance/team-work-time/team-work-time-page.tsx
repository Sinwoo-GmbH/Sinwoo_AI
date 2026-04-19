"use client";

import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceTeamWorkTimeMessages } from "@/lib/i18n/workspace-content";

type TeamWorkTimePageProps = {
  locale: LoginLocale;
};

export function TeamWorkTimePage({ locale }: TeamWorkTimePageProps) {
  const messages = getWorkspaceTeamWorkTimeMessages(locale);

  return (
    <div id="workspace-team-work-time-page" className="space-y-2">
      <WorkspacePageHeader
        strip
        id="workspace-team-work-time-header"
        title={messages.title}
        titleId="workspace-team-work-time-title"
      />

      <WorkspaceSectionPanel
        id="workspace-team-work-time-table-card"
        title={messages.tableTitle}
        titleId="workspace-team-work-time-table-title"
        className="border-slate-200/90 shadow-[0_12px_24px_rgba(148,163,184,0.06)]"
        headerClassName="px-3 py-2"
        contentClassName="px-3 py-3 pt-2"
      >
        <WorkspaceContentContainer id="workspace-team-work-time-table-container" className="bg-slate-50/60">
          <div id="workspace-team-work-time-table-wrap" className="overflow-x-auto">
            <table id="workspace-team-work-time-table" className="min-w-full border-separate border-spacing-y-1.5">
              <thead>
                <tr className="text-left text-[11px] uppercase tracking-[0.18em] text-slate-400">
                  <th className="px-3 py-2">{messages.employee}</th>
                  <th className="px-3 py-2">{messages.department}</th>
                  <th className="px-3 py-2">{messages.status}</th>
                  <th className="px-3 py-2">{messages.checkIn}</th>
                  <th className="px-3 py-2">{messages.checkOut}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td colSpan={5} className="px-0 pb-0 pt-1">
                    <div
                      id="workspace-team-work-time-placeholder"
                      className="rounded-[16px] border border-dashed border-slate-200 bg-white px-4 py-8 text-center"
                    >
                      <div
                        id="workspace-team-work-time-placeholder-title"
                        className="text-sm font-semibold text-slate-700"
                      >
                        {messages.placeholderTitle}
                      </div>
                      <div
                        id="workspace-team-work-time-placeholder-description"
                        className="mt-1.5 text-sm leading-5 text-slate-500"
                      >
                        {messages.placeholderDescription}
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </WorkspaceContentContainer>
      </WorkspaceSectionPanel>
    </div>
  );
}

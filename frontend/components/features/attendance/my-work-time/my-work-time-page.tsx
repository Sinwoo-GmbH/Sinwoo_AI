"use client";

import { AttendanceCalendarCard } from "@/components/features/attendance/attendance-calendar-card";
import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { LoginLocale } from "@/lib/i18n/login-content";
import { getWorkspaceMyWorkTimeMessages } from "@/lib/i18n/workspace-content";

type MyWorkTimePageProps = {
  accessToken: string | null;
  locale: LoginLocale;
  onLoadingChange: (loading: boolean) => void;
  onUnauthorized: () => void;
};

export function MyWorkTimePage({
  accessToken,
  locale,
  onLoadingChange,
  onUnauthorized,
}: MyWorkTimePageProps) {
  const messages = getWorkspaceMyWorkTimeMessages(locale);

  return (
    <div id="workspace-my-work-time-page" className="space-y-2">
      <WorkspacePageHeader
        strip
        id="workspace-client-attendance-header"
        title={messages.title}
        titleId="workspace-client-attendance-title"
      />

      <div
        id="workspace-my-work-time-content-grid"
        className="grid gap-3 xl:grid-cols-[1.15fr_0.85fr]"
      >
        <AttendanceCalendarCard
          accessToken={accessToken}
          locale={locale}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
        />

        <WorkspaceSectionPanel
          id="workspace-my-work-time-summary-card"
          title={messages.sectionTitle}
          titleId="workspace-my-work-time-summary-title"
          className="border-slate-200/90 shadow-[0_10px_24px_rgba(148,163,184,0.06)]"
          headerClassName="px-3 py-2"
          contentClassName="px-3 py-3 pt-2"
        >
          <WorkspaceContentContainer id="workspace-my-work-time-summary-container" className="bg-slate-50/70">
            <ul id="workspace-my-work-time-summary-list" className="space-y-2">
              {messages.pointList.map((item, index) => (
                <li
                  id={`workspace-my-work-time-summary-item-${index + 1}`}
                  key={`${item}-${index}`}
                  className="flex items-start gap-2 text-sm leading-5 text-slate-600"
                >
                  <span className="mt-[0.45rem] block h-1.5 w-1.5 rounded-full bg-[#233A7A]" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </WorkspaceContentContainer>
        </WorkspaceSectionPanel>
      </div>
    </div>
  );
}

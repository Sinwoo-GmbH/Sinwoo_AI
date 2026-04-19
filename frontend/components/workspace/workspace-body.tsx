"use client";

import { AdminAreaPage } from "@/components/features/admin/admin-area-page";
import { MyWorkTimePage } from "@/components/features/attendance/my-work-time/my-work-time-page";
import { TeamWorkTimePage } from "@/components/features/attendance/team-work-time/team-work-time-page";
import { WorkTimeReportPage } from "@/components/features/reports/work-time/work-time-report-page";
import type { LoginLocale } from "@/lib/i18n/login-content";
import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { ViewModel, WorkspaceMode } from "@/lib/workspace/platform-shell-data";

type WorkspaceBodyProps = {
  activeRuntimePageId: string | null;
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

const BODY_CLASS_NAME = "workspace-scrollbar flex-1 overflow-y-auto p-5";

export function WorkspaceBody({
  activeRuntimePageId,
  accessToken,
  locale,
  mode,
  onLoadingChange,
  onUnauthorized,
}: WorkspaceBodyProps) {
  const runtimePageId = activeRuntimePageId ?? "MNU_CUSTOMER_MY_TIME";

  if (
    runtimePageId === "MNU_CUSTOMER_MY_TIME" ||
    runtimePageId === "MNU_CUSTOMER_DASH" ||
    runtimePageId === "MNU_CUSTOMER_ATTENDANCE"
  ) {
    return (
      <div id="workspace-body" className={BODY_CLASS_NAME}>
        <MyWorkTimePage
          accessToken={accessToken}
          locale={locale}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
        />
      </div>
    );
  }

  if (runtimePageId === "MNU_CUSTOMER_TEAM_TIME") {
    return (
      <div id="workspace-body" className={BODY_CLASS_NAME}>
        <TeamWorkTimePage locale={locale} />
      </div>
    );
  }

  if (
    runtimePageId === "MNU_CUSTOMER_WORK_TIME" ||
    runtimePageId === "MNU_CUSTOMER_WORK_TIME_HISTORY" ||
    runtimePageId === "MNU_CUSTOMER_REPORTS"
  ) {
    return (
      <div id="workspace-body" className={BODY_CLASS_NAME}>
        <WorkTimeReportPage
          accessToken={accessToken}
          locale={locale}
          mode={mode}
          onUnauthorized={onUnauthorized}
          onLoadingChange={onLoadingChange}
        />
      </div>
    );
  }

  if (runtimePageId === "MNU_CUSTOMER_ADMIN_HOME" || runtimePageId === "MNU_CUSTOMER_ADMIN") {
    return (
      <div id="workspace-body" className={BODY_CLASS_NAME}>
        <AdminAreaPage locale={locale} />
      </div>
    );
  }

  if (runtimePageId === "MNU_ADMIN_WORK_TIME_HISTORY") {
    return (
      <div id="workspace-body" className={BODY_CLASS_NAME}>
        <WorkTimeReportPage
          accessToken={accessToken}
          locale={locale}
          mode={mode}
          onUnauthorized={onUnauthorized}
          onLoadingChange={onLoadingChange}
        />
      </div>
    );
  }

  return (
    <div id="workspace-body" className={BODY_CLASS_NAME}>
      <MyWorkTimePage
        accessToken={accessToken}
        locale={locale}
        onLoadingChange={onLoadingChange}
        onUnauthorized={onUnauthorized}
      />
    </div>
  );
}

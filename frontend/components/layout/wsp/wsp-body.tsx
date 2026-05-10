"use client";

import { AdminAreaPage } from "@/features/admin/admin-area-page";
import { MyWorkTimePage } from "@/features/attendance/my-work-time/my-work-time-page";
import { LeaveReqPage } from "@/features/requests/leave/leave-req-page";
import { TeamWorkTimePage } from "@/features/attendance/team-work-time/team-work-time-page";
import { BizModPage } from "@/features/common/biz-mod/biz-mod-page";
import { WorkTimeRptPage } from "@/features/reports/work-time/work-time-rpt-page";
import { WspGridSect } from "@/components/layout/wsp/wsp-grid-sect";
import { WspHeroSect } from "@/components/layout/wsp/wsp-hero-sect";
import { WspKpiGrid } from "@/components/layout/wsp/wsp-kpi-grid";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import { WspPrioritySect } from "@/components/layout/wsp/wsp-priority-sect";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import type { WspShellCnt } from "@/lib/i18n/wsp-shell-cnt";
import type { ViewModel, WspMode } from "@/lib/utils/wsp/platform-shell-data";

type WspBodyProps = {
  activeRuntimePageId: string | null;
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

const BODY_CLASS_NAME = "wsp-scrollbar flex-1 overflow-y-auto p-3";

const BIZ_MNU_MOD_MAP: Record<string, string> = {
  MNU_CUSTOMER_EXPENSE_REVIEW: "FIN_MGT_EXPENSE",
  MNU_CUSTOMER_EMPLOYEES: "MST_EMP",
  MNU_CUSTOMER_DEPARTMENTS: "MST_DEPT",
  MNU_BIZ_REQ_WORK_TIME: "REQ_WORK_TIME",
  MNU_BIZ_REQ_EXPENSE: "REQ_EXPENSE",
  MNU_BIZ_REQ_TRIP: "REQ_TRIP",
  MNU_BIZ_CLAIM_EXPENSE: "CLAIM_EXPENSE",
  MNU_BIZ_CLAIM_TRAVEL_EXP: "CLAIM_TRAVEL_EXP",
  MNU_BIZ_RPT_WORK_TIME: "RPT_WORK_TIME",
  MNU_BIZ_RPT_MONTHLY_EXP: "RPT_MONTHLY_EXP",
  MNU_BIZ_FIN_TXN_PURCHASE: "FIN_TXN_PURCHASE",
  MNU_BIZ_FIN_TXN_SALES: "FIN_TXN_SALES",
  MNU_BIZ_FIN_TXN_OPEX: "FIN_TXN_OPEX",
  MNU_BIZ_FIN_TXN_CORP_ACC: "FIN_TXN_CORP_ACC",
  MNU_BIZ_FIN_TXN_PAYROLL: "FIN_TXN_PAYROLL",
  MNU_BIZ_FIN_MGT_EXPENSE: "FIN_MGT_EXPENSE",
  MNU_BIZ_FIN_MGT_BALANCE: "FIN_MGT_BALANCE",
  MNU_BIZ_FIN_MGT_CLOSING: "FIN_MGT_CLOSING",
  MNU_BIZ_FIN_MGT_ASSET: "FIN_MGT_ASSET",
  MNU_BIZ_FIN_MGT_FIXED_OPEX: "FIN_MGT_FIXED_OPEX",
  MNU_BIZ_FIN_MGT_FIXED_SALES: "FIN_MGT_FIXED_SALES",
  MNU_BIZ_FIN_RPT_LEDGER: "FIN_RPT_LEDGER",
  MNU_BIZ_FIN_RPT_ASSET: "FIN_RPT_ASSET",
  MNU_BIZ_FIN_RPT_ANNUAL: "FIN_RPT_ANNUAL",
  MNU_BIZ_MST_EMP: "MST_EMP",
  MNU_BIZ_MST_DEPT: "MST_DEPT",
  MNU_BIZ_MST_ACC: "MST_ACC",
  MNU_BIZ_MST_CORP_ACC: "MST_CORP_ACC",
  MNU_BIZ_MST_DAILY_ALLOWANCE: "MST_DAILY_ALLOWANCE",
  MNU_BIZ_MST_CORP_CAL: "MST_CORP_CAL",
  MNU_BIZ_MST_ACC_MAPPING: "MST_ACC_MAPPING",
  MNU_BIZ_MST_FI_STMT: "MST_FI_STMT",
};

export function WspBody({
  activeRuntimePageId,
  accessToken,
  desktopTabsCount,
  locale,
  mode,
  onLoadingChange,
  onUnauthorized,
  shellCnt,
  sidebarCollapsed,
  view,
}: WspBodyProps) {
  const runtimePageId = activeRuntimePageId ?? "MNU_CUSTOMER_MY_TIME";
  const bizModCd = BIZ_MNU_MOD_MAP[runtimePageId];

  if (bizModCd) {
    return (
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <BizModPage
          accessToken={accessToken}
          locale={locale}
          modCd={bizModCd}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
        />
      </div>
    );
  }

  if (runtimePageId === "MNU_CUSTOMER_DASH" || runtimePageId === "admin-overview") {
    return (
      <div id="wsp-body" className={`${BODY_CLASS_NAME} space-y-5`}>
        <WspHeroSect
          accessToken={accessToken}
          desktopTabsCount={desktopTabsCount}
          locale={locale}
          mode={mode}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
          shellCnt={shellCnt}
          sidebarCollapsed={sidebarCollapsed}
          view={view}
        />
        <WspKpiGrid items={view.kpis} />
        <div className="grid gap-5 xl:grid-cols-[0.8fr_1.2fr]">
          <WspPrioritySect items={view.highlights} shellCnt={shellCnt} />
          <WspGridSect shellCnt={shellCnt} view={view} />
        </div>
      </div>
    );
  }

  if (
    runtimePageId === "MNU_CUSTOMER_MY_TIME" ||
    runtimePageId === "MNU_CUSTOMER_ATTENDANCE"
  ) {
    return (
      <div id="wsp-body" className={BODY_CLASS_NAME}>
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
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <TeamWorkTimePage locale={locale} />
      </div>
    );
  }

  if (runtimePageId === "MNU_CUSTOMER_LEAVE" || runtimePageId === "MNU_BIZ_REQ_LEAVE") {
    return (
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <LeaveReqPage
          accessToken={accessToken}
          locale={locale}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
        />
      </div>
    );
  }

  if (
    runtimePageId === "MNU_CUSTOMER_WORK_TIME" ||
    runtimePageId === "MNU_CUSTOMER_WORK_TIME_HISTORY" ||
    runtimePageId === "MNU_CUSTOMER_REPORTS"
  ) {
    return (
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <WorkTimeRptPage
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
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <AdminAreaPage locale={locale} />
      </div>
    );
  }

  if (runtimePageId === "MNU_ADMIN_WORK_TIME_HISTORY") {
    return (
      <div id="wsp-body" className={BODY_CLASS_NAME}>
        <WorkTimeRptPage
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
    <div id="wsp-body" className={`${BODY_CLASS_NAME} space-y-5`}>
      <WspPageHdr
        compact
        eyebrow={view.eyebrow}
        title={view.title}
        desc={view.desc}
      />
      <WspKpiGrid items={view.kpis} />
      <div className="grid gap-5 xl:grid-cols-[0.8fr_1.2fr]">
        <WspPrioritySect items={view.highlights} shellCnt={shellCnt} />
        <WspGridSect shellCnt={shellCnt} view={view} />
      </div>
    </div>
  );
}

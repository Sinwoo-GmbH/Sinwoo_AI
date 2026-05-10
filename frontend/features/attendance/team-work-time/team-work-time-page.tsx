"use client";

import { WspCntCntr } from "@/components/layout/wsp/wsp-cnt-cntr";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspTeamWorkTimeMsgs } from "@/lib/i18n/wsp-cnt";

type TeamWorkTimePageProps = {
  locale: LoginLocale;
};

export function TeamWorkTimePage({ locale }: TeamWorkTimePageProps) {
  const msgs = getWspTeamWorkTimeMsgs(locale);

  return (
    <div id="wsp-team-work-time-page" className="space-y-2">
      <WspPageHdr
        strip
        id="wsp-team-work-time-hdr"
        title={msgs.title}
        titleId="wsp-team-work-time-title"
      />

      <WspSectPanel
        id="wsp-team-work-time-table-card"
        title={msgs.tableTitle}
        titleId="wsp-team-work-time-table-title"
        className="border-slate-200/90 shadow-[0_12px_24px_rgba(148,163,184,0.06)]"
        headerClassName="px-3 py-2"
        contentClassName="px-3 py-3 pt-2"
      >
        <WspCntCntr id="wsp-team-work-time-table-cntr" className="bg-slate-50/60">
          <div id="wsp-team-work-time-table-wrap" className="overflow-x-auto">
            <table id="wsp-team-work-time-table" className="min-w-full border-separate border-spacing-y-1.5">
              <thead>
                <tr className="text-left text-[11px] uppercase tracking-[0.18em] text-slate-400">
                  <th className="px-3 py-2">{msgs.emp}</th>
                  <th className="px-3 py-2">{msgs.dept}</th>
                  <th className="px-3 py-2">{msgs.status}</th>
                  <th className="px-3 py-2">{msgs.checkIn}</th>
                  <th className="px-3 py-2">{msgs.checkOut}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td colSpan={5} className="px-0 pb-0 pt-1">
                    <div
                      id="wsp-team-work-time-ph"
                      className="rounded-[16px] border border-dashed border-slate-200 bg-white px-4 py-8 text-center"
                    >
                      <div
                        id="wsp-team-work-time-ph-title"
                        className="text-sm font-semibold text-slate-700"
                      >
                        {msgs.phTitle}
                      </div>
                      <div
                        id="wsp-team-work-time-ph-desc"
                        className="mt-1.5 text-sm leading-5 text-slate-500"
                      >
                        {msgs.phDesc}
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </WspCntCntr>
      </WspSectPanel>
    </div>
  );
}

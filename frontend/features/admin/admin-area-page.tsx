"use client";

import { WspCntCntr } from "@/components/layout/wsp/wsp-cnt-cntr";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspAdminAreaMsgs } from "@/lib/i18n/wsp-cnt";

type AdminAreaPageProps = {
  locale: LoginLocale;
};

export function AdminAreaPage({ locale }: AdminAreaPageProps) {
  const msgs = getWspAdminAreaMsgs(locale);

  return (
    <div id="wsp-admin-area-page" className="space-y-2">
      <WspPageHdr
        strip
        id="wsp-clnt-admin-hdr"
        title={msgs.title}
        titleId="wsp-clnt-admin-title"
      />

      <WspSectPanel
        id="wsp-clnt-admin-card"
        title={msgs.sectionTitle}
        titleId="wsp-clnt-admin-card-title"
        className="border-slate-200/90 shadow-[0_12px_24px_rgba(148,163,184,0.06)]"
        headerClassName="px-3 py-2"
        contentClassName="px-3 py-3 pt-2"
      >
        <WspCntCntr id="wsp-clnt-admin-cntr" className="bg-slate-50/60">
          <div
            id="wsp-clnt-admin-ph"
            className="rounded-[16px] border border-dashed border-slate-200 bg-white px-4 py-8 text-center"
          >
            <div
              id="wsp-clnt-admin-ph-title"
              className="text-sm font-semibold text-slate-700"
            >
              {msgs.phTitle}
            </div>
            <div
              id="wsp-clnt-admin-ph-desc"
              className="mt-1.5 text-sm leading-5 text-slate-500"
            >
              {msgs.phDesc}
            </div>
          </div>
        </WspCntCntr>
      </WspSectPanel>
    </div>
  );
}

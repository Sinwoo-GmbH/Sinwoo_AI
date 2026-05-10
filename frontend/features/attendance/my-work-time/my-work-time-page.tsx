"use client";

import { AttndCalCard } from "@/features/attendance/attnd-cal-card";
import { WspCntCntr } from "@/components/layout/wsp/wsp-cnt-cntr";
import { WspPageHdr } from "@/components/layout/wsp/wsp-page-hdr";
import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import { getWspMyWorkTimeMsgs } from "@/lib/i18n/wsp-cnt";

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
  const msgs = getWspMyWorkTimeMsgs(locale);

  return (
    <div id="wsp-my-work-time-page" className="space-y-2">
      <WspPageHdr
        strip
        id="wsp-clnt-attnd-hdr"
        title={msgs.title}
        titleId="wsp-clnt-attnd-title"
      />

      <div
        id="wsp-my-work-time-cnt-grid"
        className="grid gap-3 xl:grid-cols-[1.15fr_0.85fr]"
      >
        <AttndCalCard
          accessToken={accessToken}
          locale={locale}
          onLoadingChange={onLoadingChange}
          onUnauthorized={onUnauthorized}
        />

        <WspSectPanel
          id="wsp-my-work-time-sum-card"
          title={msgs.sectionTitle}
          titleId="wsp-my-work-time-sum-title"
          className="border-slate-200/90 shadow-[0_10px_24px_rgba(148,163,184,0.06)]"
          headerClassName="px-3 py-2"
          contentClassName="px-3 py-3 pt-2"
        >
          <WspCntCntr id="wsp-my-work-time-sum-cntr" className="bg-slate-50/70">
            <ul id="wsp-my-work-time-sum-list" className="space-y-2">
              {msgs.pointList.map((item, index) => (
                <li
                  id={`wsp-my-work-time-sum-item-${index + 1}`}
                  key={`${item}-${index}`}
                  className="flex items-start gap-2 text-sm leading-5 text-slate-600"
                >
                  <span className="mt-[0.45rem] block h-1.5 w-1.5 rounded-full bg-[#233A7A]" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </WspCntCntr>
        </WspSectPanel>
      </div>
    </div>
  );
}

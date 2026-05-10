"use client";

import { WspCntCntr } from "@/components/layout/wsp/wsp-cnt-cntr";
import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { WspShellCnt } from "@/lib/i18n/wsp-shell-cnt";
import type { ViewModel } from "@/lib/utils/wsp/platform-shell-data";

type WspGridSectProps = {
  shellCnt: WspShellCnt;
  view: ViewModel;
};

export function WspGridSect({ shellCnt, view }: WspGridSectProps) {
  return (
    <WspSectPanel
      id="wsp-grid-card"
      title={view.gridTitle}
      titleId="wsp-grid-title"
      desc={shellCnt.tableDesc}
      descId="wsp-grid-desc"
      contentClassName="space-y-2"
    >
      <WspCntCntr id="wsp-grid-cntr" className="space-y-2 bg-white">
        <div
          id="wsp-grid-hdr"
          className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-2 rounded-[3px] border border-slate-300 bg-[#eef1f4] px-3 py-1.5 text-[9px] uppercase tracking-[0.08em] text-slate-500"
        >
          <div>{shellCnt.name}</div>
          <div>{shellCnt.owner}</div>
          <div>{shellCnt.status}</div>
          <div>{shellCnt.updated}</div>
        </div>
        {view.gridRows.map((row, index) => (
          <div
            id={`wsp-grid-row-${index + 1}`}
            key={`${row.name}-${row.updated}-${index}`}
            className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-2 rounded-[3px] border border-slate-300 bg-white px-3 py-2 text-[11px]"
          >
            <div id={`wsp-grid-name-${index + 1}`} className="font-medium leading-4 text-slate-950">
              {row.name}
            </div>
            <div id={`wsp-grid-owner-${index + 1}`} className="leading-4 text-slate-600">
              {row.owner}
            </div>
            <div id={`wsp-grid-status-${index + 1}`} className="leading-4 text-slate-700">
              {row.status}
            </div>
            <div id={`wsp-grid-updated-${index + 1}`} className="leading-4 text-slate-500">
              {row.updated}
            </div>
          </div>
        ))}
      </WspCntCntr>
    </WspSectPanel>
  );
}

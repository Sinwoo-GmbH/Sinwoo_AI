"use client";

import { WspSectPanel } from "@/components/layout/wsp/wsp-sect-panel";
import type { WspShellCnt } from "@/lib/i18n/wsp-shell-cnt";
import type { FeedItem } from "@/lib/utils/wsp/platform-shell-data";
import { cn } from "@/lib/utils";

type WspPrioritySectProps = {
  items: FeedItem[];
  shellCnt: WspShellCnt;
};

export function WspPrioritySect({
  items,
  shellCnt,
}: WspPrioritySectProps) {
  return (
    <WspSectPanel
      id="wsp-priority-card"
      title={shellCnt.focusTitle}
      titleId="wsp-priority-title"
      desc={shellCnt.focusDesc}
      descId="wsp-priority-desc"
      contentClassName="space-y-2"
    >
      {items.map((item, index) => (
        <div
          id={`wsp-highlight-card-${index + 1}`}
          key={`${item.title}-${index}`}
          className={cn(
            "rounded-[3px] border px-3 py-2",
            item.emphasis === "warning"
              ? "border-amber-200 bg-[#faf6ed]"
              : item.emphasis === "success"
                ? "border-emerald-200 bg-[#eef8f1]"
                : "border-slate-300 bg-white"
          )}
        >
          <div
            id={`wsp-highlight-title-${index + 1}`}
            className="text-[11px] font-medium leading-4 text-slate-950"
          >
            {item.title}
          </div>
          <div id={`wsp-highlight-meta-${index + 1}`} className="mt-0.5 text-[10px] leading-4 text-slate-500">
            {item.meta}
          </div>
        </div>
      ))}
    </WspSectPanel>
  );
}

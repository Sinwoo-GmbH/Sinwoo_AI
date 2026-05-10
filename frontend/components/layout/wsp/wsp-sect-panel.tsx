import type { ReactNode } from "react";

import { Card, CardCnt, CardDesc, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";

type WspSectPanelProps = {
  id?: string;
  eyebrow?: string;
  eyebrowId?: string;
  title?: string;
  titleId?: string;
  desc?: string;
  descId?: string;
  actions?: ReactNode;
  actionsId?: string;
  headerId?: string;
  contentId?: string;
  className?: string;
  headerClassName?: string;
  contentClassName?: string;
  children: ReactNode;
};

export function WspSectPanel({
  id,
  eyebrow,
  eyebrowId,
  title,
  titleId,
  desc,
  descId,
  actions,
  actionsId,
  headerId,
  contentId,
  className,
  headerClassName,
  contentClassName,
  children,
}: WspSectPanelProps) {
  const hasHeader = eyebrow || title || desc || actions;

  return (
    <Card id={id} className={cn("rounded-[4px] border-slate-300 bg-[#f8f9fb] shadow-none", className)}>
      {hasHeader ? (
        <CardHeader
          id={headerId}
          className={cn("flex flex-col gap-2 border-b border-slate-300 px-3 py-2 sm:flex-row sm:items-start sm:justify-between", headerClassName)}
        >
          <div className="space-y-1">
            {eyebrow ? (
              <CardDesc id={eyebrowId} className="text-[9px] uppercase tracking-[0.12em] text-slate-400">
                {eyebrow}
              </CardDesc>
            ) : null}
            {title ? (
              <CardTitle id={titleId} className="text-[13px] font-semibold leading-4 text-slate-900">
                {title}
              </CardTitle>
            ) : null}
            {desc ? (
              <CardDesc id={descId} className="max-w-2xl text-[11px] leading-4 text-slate-500">
                {desc}
              </CardDesc>
            ) : null}
          </div>
          {actions ? (
            <div id={actionsId} className="flex shrink-0 flex-wrap items-center gap-1.5">
              {actions}
            </div>
          ) : null}
        </CardHeader>
      ) : null}
      <CardCnt id={contentId} className={cn("px-3 py-3", hasHeader ? "pt-3" : "pt-3", contentClassName)}>
        {children}
      </CardCnt>
    </Card>
  );
}

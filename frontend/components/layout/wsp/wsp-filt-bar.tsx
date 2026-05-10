import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WspFiltBarProps = {
  id?: string;
  compact?: boolean;
  title?: string;
  titleId?: string;
  desc?: string;
  descId?: string;
  className?: string;
  children: ReactNode;
};

export function WspFiltBar({
  id,
  compact = false,
  title,
  titleId,
  desc,
  descId,
  className,
  children,
}: WspFiltBarProps) {
  return (
    <div
      id={id}
      className={cn(
        compact
          ? "rounded-[4px] border border-slate-300 bg-[#f3f5f8] px-3 py-2"
          : "rounded-[4px] border border-slate-300 bg-[#f3f5f8] p-3",
        className
      )}
    >
      {title || desc ? (
        <div className={cn(compact ? "mb-1.5 space-y-0.5" : "mb-2.5 space-y-0.5")}>
          {title ? (
            <div
              id={titleId}
              className={cn(
                compact ? "text-[10px] font-semibold uppercase tracking-[0.12em] text-slate-500" : "text-[11px] font-semibold text-slate-700"
              )}
            >
              {title}
            </div>
          ) : null}
          {desc ? (
            <div
              id={descId}
              className={cn(
                compact ? "text-[10px] leading-4 text-slate-500" : "text-[11px] leading-4 text-slate-500"
              )}
            >
              {desc}
            </div>
          ) : null}
        </div>
      ) : null}
      {children}
    </div>
  );
}

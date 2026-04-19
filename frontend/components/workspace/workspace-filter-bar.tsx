import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WorkspaceFilterBarProps = {
  id?: string;
  compact?: boolean;
  title?: string;
  titleId?: string;
  description?: string;
  descriptionId?: string;
  className?: string;
  children: ReactNode;
};

export function WorkspaceFilterBar({
  id,
  compact = false,
  title,
  titleId,
  description,
  descriptionId,
  className,
  children,
}: WorkspaceFilterBarProps) {
  return (
    <div
      id={id}
      className={cn(
        compact
          ? "rounded-[18px] border border-slate-200/90 bg-[linear-gradient(180deg,rgba(248,250,252,0.95)_0%,rgba(241,245,249,0.92)_100%)] px-3 py-2.5"
          : "rounded-[24px] border border-slate-200 bg-[linear-gradient(180deg,rgba(248,250,252,0.95)_0%,rgba(241,245,249,0.92)_100%)] p-4",
        className
      )}
    >
      {title || description ? (
        <div className={cn(compact ? "mb-2 space-y-0.5" : "mb-4 space-y-1")}>
          {title ? (
            <div
              id={titleId}
              className={cn(
                compact ? "text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-500" : "text-sm font-semibold text-slate-700"
              )}
            >
              {title}
            </div>
          ) : null}
          {description ? (
            <div
              id={descriptionId}
              className={cn(
                compact ? "text-[11px] leading-4 text-slate-500" : "text-xs leading-5 text-slate-500"
              )}
            >
              {description}
            </div>
          ) : null}
        </div>
      ) : null}
      {children}
    </div>
  );
}

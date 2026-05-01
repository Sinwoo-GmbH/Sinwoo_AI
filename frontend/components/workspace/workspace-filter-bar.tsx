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
          ? "rounded-[4px] border border-slate-300 bg-[#f3f5f8] px-3 py-2"
          : "rounded-[4px] border border-slate-300 bg-[#f3f5f8] p-3",
        className
      )}
    >
      {title || description ? (
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
          {description ? (
            <div
              id={descriptionId}
              className={cn(
                compact ? "text-[10px] leading-4 text-slate-500" : "text-[11px] leading-4 text-slate-500"
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

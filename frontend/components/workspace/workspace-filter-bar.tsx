import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WorkspaceFilterBarProps = {
  id?: string;
  title?: string;
  titleId?: string;
  description?: string;
  descriptionId?: string;
  className?: string;
  children: ReactNode;
};

export function WorkspaceFilterBar({
  id,
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
        "rounded-[24px] border border-slate-200 bg-[linear-gradient(180deg,rgba(248,250,252,0.95)_0%,rgba(241,245,249,0.92)_100%)] p-4",
        className
      )}
    >
      {title || description ? (
        <div className="mb-4 space-y-1">
          {title ? (
            <div id={titleId} className="text-sm font-semibold text-slate-700">
              {title}
            </div>
          ) : null}
          {description ? (
            <div id={descriptionId} className="text-xs leading-5 text-slate-500">
              {description}
            </div>
          ) : null}
        </div>
      ) : null}
      {children}
    </div>
  );
}

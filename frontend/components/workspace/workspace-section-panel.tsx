import type { ReactNode } from "react";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";

type WorkspaceSectionPanelProps = {
  id?: string;
  eyebrow?: string;
  eyebrowId?: string;
  title?: string;
  titleId?: string;
  description?: string;
  descriptionId?: string;
  actions?: ReactNode;
  actionsId?: string;
  headerId?: string;
  contentId?: string;
  className?: string;
  headerClassName?: string;
  contentClassName?: string;
  children: ReactNode;
};

export function WorkspaceSectionPanel({
  id,
  eyebrow,
  eyebrowId,
  title,
  titleId,
  description,
  descriptionId,
  actions,
  actionsId,
  headerId,
  contentId,
  className,
  headerClassName,
  contentClassName,
  children,
}: WorkspaceSectionPanelProps) {
  const hasHeader = eyebrow || title || description || actions;

  return (
    <Card id={id} className={cn("border-white/90 bg-white/86 shadow-[0_14px_36px_rgba(148,163,184,0.08)]", className)}>
      {hasHeader ? (
        <CardHeader
          id={headerId}
          className={cn("flex flex-col gap-3 border-b border-slate-200/70 px-5 py-4 sm:flex-row sm:items-start sm:justify-between", headerClassName)}
        >
          <div className="space-y-1.5">
            {eyebrow ? (
              <CardDescription id={eyebrowId} className="text-[11px] uppercase tracking-[0.24em] text-slate-400">
                {eyebrow}
              </CardDescription>
            ) : null}
            {title ? (
              <CardTitle id={titleId} className="font-brand text-[1.7rem] font-semibold text-slate-950">
                {title}
              </CardTitle>
            ) : null}
            {description ? (
              <CardDescription id={descriptionId} className="max-w-2xl text-sm leading-6 text-slate-500">
                {description}
              </CardDescription>
            ) : null}
          </div>
          {actions ? (
            <div id={actionsId} className="flex shrink-0 flex-wrap items-center gap-2">
              {actions}
            </div>
          ) : null}
        </CardHeader>
      ) : null}
      <CardContent id={contentId} className={cn("px-5 py-5", hasHeader ? "pt-5" : "pt-5", contentClassName)}>
        {children}
      </CardContent>
    </Card>
  );
}

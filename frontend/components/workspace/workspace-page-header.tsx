import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WorkspacePageHeaderProps = {
  id?: string;
  eyebrow?: string;
  eyebrowId?: string;
  title: string;
  titleId?: string;
  description?: string;
  descriptionId?: string;
  actions?: ReactNode;
  actionsId?: string;
  footer?: ReactNode;
  footerId?: string;
  className?: string;
  eyebrowClassName?: string;
  titleClassName?: string;
  descriptionClassName?: string;
  bodyClassName?: string;
};

export function WorkspacePageHeader({
  id,
  eyebrow,
  eyebrowId,
  title,
  titleId,
  description,
  descriptionId,
  actions,
  actionsId,
  footer,
  footerId,
  className,
  eyebrowClassName,
  titleClassName,
  descriptionClassName,
  bodyClassName,
}: WorkspacePageHeaderProps) {
  return (
    <section
      id={id}
      className={cn(
        "rounded-[28px] border border-white/90 bg-white/88 px-6 py-5 shadow-[0_14px_34px_rgba(148,163,184,0.10)] backdrop-blur",
        className
      )}
    >
      <div className={cn("flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between", bodyClassName)}>
        <div className="min-w-0 space-y-3">
          {eyebrow ? (
            <p
              id={eyebrowId}
              className={cn("text-xs uppercase tracking-[0.28em] text-slate-400", eyebrowClassName)}
            >
              {eyebrow}
            </p>
          ) : null}
          <div className="space-y-2">
            <h2 id={titleId} className={cn("font-brand text-3xl font-semibold tracking-tight text-slate-950", titleClassName)}>
              {title}
            </h2>
            {description ? (
              <p id={descriptionId} className={cn("max-w-3xl text-sm leading-6 text-slate-500", descriptionClassName)}>
                {description}
              </p>
            ) : null}
          </div>
        </div>
        {actions ? (
          <div id={actionsId} className="flex flex-wrap items-center justify-start gap-2 lg:justify-end">
            {actions}
          </div>
        ) : null}
      </div>
      {footer ? (
        <div id={footerId} className="mt-5">
          {footer}
        </div>
      ) : null}
    </section>
  );
}

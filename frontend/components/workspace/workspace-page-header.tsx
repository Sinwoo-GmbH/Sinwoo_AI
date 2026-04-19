import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WorkspacePageHeaderProps = {
  id?: string;
  compact?: boolean;
  strip?: boolean;
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
  compact = false,
  strip = false,
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
  if (strip) {
    return (
      <section
        id={id}
        className={cn(
          "rounded-[16px] border border-slate-200/80 bg-white/72 px-3 py-2 shadow-[0_6px_14px_rgba(148,163,184,0.05)] backdrop-blur",
          className
        )}
      >
        <div className={cn("min-w-0", bodyClassName)}>
          <h2
            id={titleId}
            className={cn("text-base font-semibold leading-5 text-slate-800", titleClassName)}
          >
            {title}
          </h2>
        </div>
      </section>
    );
  }

  return (
    <section
      id={id}
      className={cn(
        compact
          ? "rounded-[22px] border border-slate-200/80 bg-white/82 px-4 py-3 shadow-[0_10px_22px_rgba(148,163,184,0.08)] backdrop-blur"
          : "rounded-[28px] border border-white/90 bg-white/88 px-6 py-5 shadow-[0_14px_34px_rgba(148,163,184,0.10)] backdrop-blur",
        className
      )}
    >
      <div
        className={cn(
          compact
            ? "flex flex-col gap-2.5 lg:flex-row lg:items-start lg:justify-between"
            : "flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between",
          bodyClassName
        )}
      >
        <div className={cn("min-w-0", compact ? "space-y-1.5" : "space-y-3")}>
          {eyebrow ? (
            <p
              id={eyebrowId}
              className={cn(
                compact
                  ? "text-[10px] uppercase tracking-[0.24em] text-slate-400"
                  : "text-xs uppercase tracking-[0.28em] text-slate-400",
                eyebrowClassName
              )}
            >
              {eyebrow}
            </p>
          ) : null}
          <div className={cn(compact ? "space-y-1" : "space-y-2")}>
            <h2
              id={titleId}
              className={cn(
                compact
                  ? "font-brand text-[1.55rem] font-semibold tracking-tight text-slate-950"
                  : "font-brand text-3xl font-semibold tracking-tight text-slate-950",
                titleClassName
              )}
            >
              {title}
            </h2>
            {description ? (
              <p
                id={descriptionId}
                className={cn(
                  compact
                    ? "max-w-2xl text-[12px] leading-5 text-slate-500"
                    : "max-w-3xl text-sm leading-6 text-slate-500",
                  descriptionClassName
                )}
              >
                {description}
              </p>
            ) : null}
          </div>
        </div>
        {actions ? (
          <div
            id={actionsId}
            className={cn(
              "flex flex-wrap items-center justify-start lg:justify-end",
              compact ? "gap-1.5" : "gap-2"
            )}
          >
            {actions}
          </div>
        ) : null}
      </div>
      {footer ? (
        <div id={footerId} className={compact ? "mt-3" : "mt-5"}>
          {footer}
        </div>
      ) : null}
    </section>
  );
}

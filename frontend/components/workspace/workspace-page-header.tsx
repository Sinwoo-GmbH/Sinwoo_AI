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
          "rounded-[4px] border border-slate-300 bg-[#f8f9fb] px-3 py-2",
          className
        )}
      >
        <div className={cn("min-w-0", bodyClassName)}>
          <h2
            id={titleId}
            className={cn("text-[13px] font-semibold leading-4 text-slate-800", titleClassName)}
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
          ? "rounded-[4px] border border-slate-300 bg-[#f8f9fb] px-3 py-2.5"
          : "rounded-[4px] border border-slate-300 bg-[#f8f9fb] px-4 py-3",
        className
      )}
    >
      <div
        className={cn(
          compact
            ? "flex flex-col gap-2 lg:flex-row lg:items-start lg:justify-between"
            : "flex flex-col gap-2.5 lg:flex-row lg:items-start lg:justify-between",
          bodyClassName
        )}
      >
        <div className={cn("min-w-0", compact ? "space-y-1" : "space-y-1.5")}>
          {eyebrow ? (
            <p
              id={eyebrowId}
              className={cn(
                compact
                  ? "text-[9px] uppercase tracking-[0.12em] text-slate-400"
                  : "text-[9px] uppercase tracking-[0.12em] text-slate-400",
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
                  ? "text-[16px] font-semibold leading-5 tracking-tight text-slate-900"
                  : "text-[18px] font-semibold leading-5 tracking-tight text-slate-900",
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
                    ? "max-w-2xl text-[11px] leading-4 text-slate-500"
                    : "max-w-3xl text-[11px] leading-4 text-slate-500",
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
              compact ? "gap-1" : "gap-1.5"
            )}
          >
            {actions}
          </div>
        ) : null}
      </div>
      {footer ? (
        <div id={footerId} className={compact ? "mt-2" : "mt-3"}>
          {footer}
        </div>
      ) : null}
    </section>
  );
}

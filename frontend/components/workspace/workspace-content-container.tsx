import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type WorkspaceContentContainerProps = {
  id?: string;
  className?: string;
  children: ReactNode;
};

export function WorkspaceContentContainer({ id, className, children }: WorkspaceContentContainerProps) {
  return (
    <div
      id={id}
      className={cn("rounded-[22px] border border-slate-200/90 bg-white/90 p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.75)]", className)}
    >
      {children}
    </div>
  );
}

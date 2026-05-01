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
      className={cn("rounded-[4px] border border-slate-300 bg-white p-2.5 shadow-none", className)}
    >
      {children}
    </div>
  );
}

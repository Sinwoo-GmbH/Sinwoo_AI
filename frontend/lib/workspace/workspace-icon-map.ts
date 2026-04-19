import {
  BriefcaseBusiness,
  Building2,
  CreditCard,
  Grid2X2,
  KeyRound,
  ShieldCheck,
  Wallet,
  Workflow,
  Zap,
  type LucideIcon,
} from "lucide-react";

export const workspaceIconMap = {
  grid: Grid2X2,
  briefcase: BriefcaseBusiness,
  "credit-card": CreditCard,
  shield: ShieldCheck,
  building: Building2,
  key: KeyRound,
  wallet: Wallet,
  activity: Zap,
} satisfies Record<string, LucideIcon>;

export function resolveWorkspaceIcon(iconNm?: string | null): LucideIcon {
  if (!iconNm) {
    return Workflow;
  }

  return workspaceIconMap[iconNm as keyof typeof workspaceIconMap] ?? Workflow;
}

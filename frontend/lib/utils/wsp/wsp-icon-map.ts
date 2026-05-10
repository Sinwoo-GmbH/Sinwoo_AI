import {
  BriefcaseBusiness,
  Building2,
  BarChart3,
  CalendarDays,
  ClipboardList,
  CreditCard,
  Grid2X2,
  KeyRound,
  Plane,
  Receipt,
  ReceiptText,
  Settings,
  ShieldCheck,
  Shuffle,
  UserRoundCheck,
  Users as Usrs,
  Wallet,
  Workflow,
  Zap,
  type LucideIcon,
} from "lucide-react";

export const wspIconMap = {
  grid: Grid2X2,
  briefcase: BriefcaseBusiness,
  "credit-card": CreditCard,
  shield: ShieldCheck,
  building: Building2,
  key: KeyRound,
  receipt: Receipt,
  usrs: Usrs,
  "user-round-check": UserRoundCheck,
  wallet: Wallet,
  activity: Zap,
  "bar-chart-3": BarChart3,
  "calendar-days": CalendarDays,
  "clipboard-list": ClipboardList,
  plane: Plane,
  "receipt-text": ReceiptText,
  settings: Settings,
  shuffle: Shuffle,
  "mnu-square": Grid2X2,
} satisfies Record<string, LucideIcon>;

export function resolveWspIcon(iconNm?: string | null): LucideIcon {
  if (!iconNm) {
    return Workflow;
  }

  return wspIconMap[iconNm as keyof typeof wspIconMap] ?? Workflow;
}

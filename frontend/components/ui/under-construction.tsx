"use client";

import { Construction } from "lucide-react";
import type { LoginLocale } from "@/lib/i18n/login-cnt";

const LABELS: Record<LoginLocale, { title: string; desc: string }> = {
  ko: { title: "공사중", desc: "이 메뉴는 아직 준비 중입니다." },
  en: { title: "Under Construction", desc: "This menu is not ready yet." },
  de: { title: "In Bearbeitung", desc: "Dieses Menü ist noch nicht fertig." },
};

type UnderConstructionProps = {
  locale: LoginLocale;
  mnuLabel?: string;
};

export function UnderConstruction({ locale, mnuLabel }: UnderConstructionProps) {
  const L = LABELS[locale] ?? LABELS.en;
  return (
    <div className="flex h-full min-h-[60vh] flex-col items-center justify-center gap-4 text-slate-500">
      <Construction className="h-16 w-16 text-amber-500" strokeWidth={1.5} />
      <div className="text-center">
        <h2 className="text-2xl font-semibold text-slate-700">{L.title}</h2>
        {mnuLabel ? (
          <p className="mt-1 text-sm text-slate-400">{mnuLabel}</p>
        ) : null}
        <p className="mt-2 text-sm">{L.desc}</p>
      </div>
    </div>
  );
}

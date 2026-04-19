"use client";

import Image from "next/image";
import { LogOut, UserCircle2 } from "lucide-react";

import { LocaleCombobox } from "@/components/common/locale-combobox";
import { Button } from "@/components/ui/button";
import type { LoginLocale } from "@/lib/i18n/login-content";
import type { WorkspaceShellContent } from "@/lib/i18n/workspace-shell-content";
import type { WorkspaceMode } from "@/lib/workspace/platform-shell-data";
import { cn } from "@/lib/utils";

type WorkspaceHeaderProps = {
  locale: LoginLocale;
  localeLabel: string;
  localeNames: Record<LoginLocale, string>;
  mode: WorkspaceMode;
  shellTitle: string;
  shellContent: WorkspaceShellContent;
  onLocaleChange: (locale: LoginLocale) => void;
  onModeChange: (mode: WorkspaceMode) => void;
  onOpenProfile: () => void;
  onLogout: () => void;
};

export function WorkspaceHeader({
  locale,
  localeLabel,
  localeNames,
  mode,
  shellTitle,
  shellContent,
  onLocaleChange,
  onModeChange,
  onOpenProfile,
  onLogout,
}: WorkspaceHeaderProps) {
  return (
    <header
      id="workspace-header"
      className="relative z-20 mb-[0.3rem] rounded-[20px] border border-slate-200/70 bg-white/68 px-3 py-1 shadow-[0_6px_18px_rgba(148,163,184,0.10)] backdrop-blur lg:px-4 lg:py-1.5"
    >
      <div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
        <div id="workspace-header-brand" className="flex items-center gap-2">
          <div id="workspace-header-logo" className="w-full max-w-[82px] shrink-0 lg:max-w-[88px]">
            <Image
              src="/brand/sinwoo-logo.png"
              alt="Sinwoo International"
              width={800}
              height={389}
              className="h-auto w-full"
              priority
            />
          </div>
          <div>
            <p
              id="workspace-header-eyebrow"
              className="text-[9px] uppercase tracking-[0.22em] text-slate-400"
            >
              {shellContent.eyebrow}
            </p>
            <h1
              id="workspace-header-title"
              className="font-brand text-[15px] font-semibold tracking-tight text-slate-700 lg:text-base"
            >
              {shellTitle}
            </h1>
          </div>
        </div>

        <div id="workspace-header-actions" className="flex flex-wrap items-center gap-1">
          <div
            id="workspace-mode-switcher"
            className="inline-flex rounded-2xl border border-slate-200/80 bg-white/75 p-1"
          >
            <button
              id="workspace-mode-client"
              type="button"
              onClick={() => onModeChange("client")}
              className={cn(
                "rounded-xl px-2.5 py-0.5 text-[13px] font-medium transition-colors",
                mode === "client"
                  ? "bg-slate-100 text-slate-700"
                  : "text-slate-400 hover:text-slate-600"
              )}
            >
              {shellContent.client}
            </button>
            <button
              id="workspace-mode-admin"
              type="button"
              onClick={() => onModeChange("admin")}
              className={cn(
                "rounded-xl px-2.5 py-0.5 text-[13px] font-medium transition-colors",
                mode === "admin"
                  ? "bg-[#EAF0FB] text-[#18397E]"
                  : "text-slate-400 hover:text-slate-600"
              )}
            >
              {shellContent.admin}
            </button>
          </div>

          <Button
            id="workspace-profile-button"
            type="button"
            variant="outline"
            size="icon"
            className="h-9 w-9 rounded-2xl border-slate-200/80 bg-white/75 text-slate-500 hover:bg-slate-50 hover:text-slate-700"
            onClick={onOpenProfile}
          >
            <UserCircle2 className="h-4 w-4" />
          </Button>

          <LocaleCombobox
            idPrefix="workspace"
            value={locale}
            localeLabel={localeLabel}
            localeNames={localeNames}
            onChange={onLocaleChange}
            align="center"
            menuStrategy="fixed"
            buttonClassName="justify-between gap-1.5 px-2.5 py-1 text-[12px] text-slate-600"
            menuClassName="min-w-[124px]"
          />

          <Button
            id="workspace-logout-button"
            type="button"
            variant="outline"
            size="icon"
            className="h-9 w-9 rounded-2xl border-slate-200/80 bg-white/75 text-slate-600 hover:bg-slate-50 hover:text-slate-800"
            onClick={onLogout}
          >
            <LogOut className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </header>
  );
}

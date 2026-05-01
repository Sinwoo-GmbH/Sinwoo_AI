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
      className="relative z-20 mb-1 rounded-[4px] border border-slate-300 bg-[#f8f9fb] px-3 py-1.5 lg:px-3 lg:py-1.5"
    >
      <div className="flex flex-col gap-1.5 lg:flex-row lg:items-center lg:justify-between">
        <div id="workspace-header-brand" className="flex items-center gap-2">
          <div id="workspace-header-logo" className="w-full max-w-[76px] shrink-0 lg:max-w-[80px]">
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
              className="text-[8px] uppercase tracking-[0.12em] text-slate-400"
            >
              {shellContent.eyebrow}
            </p>
            <h1
              id="workspace-header-title"
              className="text-[13px] font-semibold leading-4 tracking-tight text-slate-800 lg:text-[13px]"
            >
              {shellTitle}
            </h1>
          </div>
        </div>

        <div id="workspace-header-actions" className="flex flex-wrap items-center gap-1">
          <div
            id="workspace-mode-switcher"
            className="inline-flex rounded-[4px] border border-slate-300 bg-white p-0.5"
          >
            <button
              id="workspace-mode-client"
              type="button"
              onClick={() => onModeChange("client")}
              className={cn(
                "rounded-[3px] px-2 py-1 text-[11px] font-medium leading-4 transition-colors",
                mode === "client"
                  ? "bg-slate-200 text-slate-800"
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
                "rounded-[3px] px-2 py-1 text-[11px] font-medium leading-4 transition-colors",
                mode === "admin"
                  ? "bg-[#dbe5f3] text-[#284a77]"
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
            className="h-7 w-7 rounded-[4px] border-slate-300 bg-white text-slate-500 hover:bg-slate-100 hover:text-slate-700"
            onClick={onOpenProfile}
          >
            <UserCircle2 className="h-3.5 w-3.5" />
          </Button>

          <LocaleCombobox
            idPrefix="workspace"
            value={locale}
            localeLabel={localeLabel}
            localeNames={localeNames}
            onChange={onLocaleChange}
            align="center"
            menuStrategy="fixed"
            buttonClassName="h-7 justify-between gap-1.5 px-2.5 py-1 text-[11px] text-slate-600"
            menuClassName="min-w-[120px]"
          />

          <Button
            id="workspace-logout-button"
            type="button"
            variant="outline"
            size="icon"
            className="h-7 w-7 rounded-[4px] border-slate-300 bg-white text-slate-600 hover:bg-slate-100 hover:text-slate-800"
            onClick={onLogout}
          >
            <LogOut className="h-3.5 w-3.5" />
          </Button>
        </div>
      </div>
    </header>
  );
}

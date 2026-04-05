"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";

import { CredentialLoginPanel } from "@/components/auth/credential-login-panel";
import type { AuthProviderItem } from "@/lib/api/auth-contract";
import {
  detectBrowserLoginLocale,
  getLoginMessages,
  type LoginLocale,
  LOGIN_LOCALES,
} from "@/lib/i18n/login-content";
import { cn } from "@/lib/utils";

type Props = {
  backendBaseUrl: string;
  providers: AuthProviderItem[];
};

const LOCALE_STORAGE_KEY = "sinwoo.loginLocale";

export function LoginPageShell({ backendBaseUrl, providers }: Props) {
  const [locale, setLocale] = useState<LoginLocale>("ko");

  useEffect(() => {
    const resolvedLocale = detectBrowserLoginLocale();
    setLocale(resolvedLocale);
  }, []);

  useEffect(() => {
    document.documentElement.lang = locale;
    window.localStorage.setItem(LOCALE_STORAGE_KEY, locale);
  }, [locale]);

  const messages = useMemo(() => getLoginMessages(locale), [locale]);

  return (
    <main className="login-shell relative bg-[linear-gradient(180deg,_#f7f9fc_0%,_#edf2f8_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-6rem] top-[-5rem] h-52 w-52 rounded-full bg-[rgba(34,58,122,0.08)] blur-3xl" />
        <div className="absolute right-[-4rem] top-[-5rem] h-72 w-72 rounded-full bg-[rgba(72,104,181,0.10)] blur-3xl" />
        <div className="absolute inset-x-0 bottom-0 h-40 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(255,255,255,0.52)_100%)]" />
      </div>

      <div className="login-shell-inner relative mx-auto flex max-w-6xl flex-col box-border px-4 py-4 sm:px-6 lg:px-8 lg:py-5">
        <div className="flex justify-end pb-2 sm:pb-3">
          <div className="inline-flex items-center gap-1 rounded-full border border-slate-200/80 bg-white/80 p-1 shadow-sm backdrop-blur">
            <span className="px-2 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-400">
              {messages.localeLabel}
            </span>
            {LOGIN_LOCALES.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => setLocale(item)}
                className={cn(
                  "rounded-full px-2.5 py-1 text-[11px] font-semibold transition",
                  locale === item
                    ? "bg-[#233a7a] text-white"
                    : "text-slate-500 hover:bg-slate-100 hover:text-slate-700"
                )}
              >
                {messages.localeNames[item]}
              </button>
            ))}
          </div>
        </div>

        <section className="flex flex-1 items-center justify-center py-3 sm:py-5 lg:min-h-0 lg:py-4">
          <div className="w-full lg:hidden">
            <div className="mx-auto w-full max-w-[438px] space-y-4 text-center lg:space-y-3.5">
              <div className="flex flex-col items-center gap-4">
                <div className="w-full max-w-[136px] sm:max-w-[148px]">
                  <Image
                    src="/brand/sinwoo-logo.png"
                    alt="Sinwoo International"
                    width={800}
                    height={389}
                    className="h-auto w-full"
                    priority
                  />
                </div>

                <div className="space-y-2">
                  <p className="text-[10px] font-semibold uppercase tracking-[0.34em] text-slate-500 sm:text-[11px]">
                    {messages.productName}
                  </p>
                  <p className="mx-auto max-w-xl text-[10px] font-semibold italic tracking-[0.28em] leading-4 text-slate-400 sm:text-[11px]">
                    {messages.tagline}
                  </p>
                </div>
              </div>

              <CredentialLoginPanel
                backendBaseUrl={backendBaseUrl}
                providers={providers}
                locale={locale}
                mode="mobile"
              />
            </div>
          </div>

          <div className="hidden w-full lg:block">
            <div className="grid min-h-[560px] grid-cols-[minmax(0,1.1fr)_minmax(360px,430px)] overflow-hidden rounded-[34px] border border-slate-200/80 bg-white/75 shadow-[0_30px_80px_rgba(15,23,42,0.12)] backdrop-blur">
              <div className="relative flex flex-col justify-between bg-[linear-gradient(145deg,_rgba(35,58,122,0.98)_0%,_rgba(17,34,78,0.92)_100%)] px-10 py-10 text-white">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.12),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(125,154,230,0.22),transparent_28%)]" />

                <div className="relative space-y-10">
                  <div className="w-full max-w-[220px]">
                    <Image
                      src="/brand/sinwoo-logo.png"
                      alt="Sinwoo International"
                      width={800}
                      height={389}
                      className="h-auto w-full brightness-[1.9] contrast-[0.96]"
                      priority
                    />
                  </div>

                  <div className="space-y-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.36em] text-white/60">
                      {messages.desktopLabel}
                    </p>
                    <div className="space-y-3">
                      <h1 className="max-w-[480px] text-[clamp(2rem,2.8vw,3.2rem)] font-semibold leading-[1.02] tracking-[-0.05em] text-white">
                        {messages.productName}
                      </h1>
                      <p className="max-w-[430px] text-sm font-medium uppercase tracking-[0.26em] text-white/68">
                        {messages.tagline}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="relative grid gap-3">
                  {messages.desktopHighlights.map((item) => (
                    <div
                      key={item}
                      className="rounded-2xl border border-white/12 bg-white/8 px-4 py-4 text-sm leading-6 text-white/88 backdrop-blur-sm"
                    >
                      {item}
                    </div>
                  ))}
                </div>
              </div>

              <div className="flex items-center bg-[linear-gradient(180deg,rgba(255,255,255,0.94)_0%,rgba(247,249,252,0.98)_100%)] px-10 py-10">
                <div className="w-full">
                  <CredentialLoginPanel
                    backendBaseUrl={backendBaseUrl}
                    providers={providers}
                    locale={locale}
                    mode="desktop"
                  />
                </div>
              </div>
            </div>
          </div>
        </section>

        <footer className="mt-auto flex shrink-0 flex-col gap-1 border-t border-slate-200/90 pt-2.5 text-center text-[10px] leading-5 text-slate-500 sm:text-[11px] lg:flex-row lg:items-center lg:justify-between">
          <p className="font-semibold tracking-[0.16em] text-slate-700">{messages.footerCompany}</p>
          <p>{messages.footerDescription}</p>
          <p>{messages.footerCopyright}</p>
        </footer>
      </div>
    </main>
  );
}

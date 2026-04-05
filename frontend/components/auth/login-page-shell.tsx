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

export function LoginPageShell({ backendBaseUrl, providers }: Props) {
  const [locale, setLocale] = useState<LoginLocale>("en");

  useEffect(() => {
    const resolvedLocale = detectBrowserLoginLocale();
    setLocale(resolvedLocale);
  }, []);

  useEffect(() => {
    document.documentElement.lang = locale;
  }, [locale]);

  const messages = useMemo(() => getLoginMessages(locale), [locale]);

  return (
    <main className="login-shell relative bg-[linear-gradient(180deg,_#f7f9fc_0%,_#edf2f8_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-6rem] top-[-5rem] h-52 w-52 rounded-full bg-[rgba(34,58,122,0.08)] blur-3xl" />
        <div className="absolute right-[-4rem] top-[-5rem] h-72 w-72 rounded-full bg-[rgba(72,104,181,0.10)] blur-3xl" />
        <div className="absolute inset-x-0 bottom-0 h-40 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(255,255,255,0.52)_100%)]" />
      </div>

      <div className="login-shell-inner relative mx-auto flex max-w-6xl flex-col box-border px-4 py-4 sm:px-6 lg:px-8 lg:py-6">
        <div className="flex justify-end pb-2 sm:pb-3 lg:absolute lg:right-8 lg:top-6 lg:z-20 lg:pb-0">
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

        <section className="flex flex-1 items-center justify-center py-3 sm:py-5 lg:min-h-0 lg:py-0">
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
                  <p className="font-brand text-[10px] font-semibold uppercase tracking-[0.34em] text-slate-500 sm:text-[11px]">
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

          <div className="hidden w-full lg:flex lg:flex-1 lg:items-center">
            <div className="grid h-[min(620px,calc(100dvh-168px))] w-full grid-cols-[minmax(0,1.18fr)_minmax(360px,420px)] overflow-hidden rounded-[34px] border border-slate-200/80 bg-white/75 shadow-[0_30px_80px_rgba(15,23,42,0.12)] backdrop-blur">
              <div className="relative flex h-full flex-col justify-center overflow-hidden bg-[linear-gradient(145deg,_rgba(35,58,122,0.98)_0%,_rgba(17,34,78,0.92)_100%)] px-10 py-10 text-white">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.12),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(125,154,230,0.22),transparent_28%)]" />

                <div className="relative mx-auto flex w-full max-w-[500px] flex-col gap-7">
                  <div className="w-full max-w-[196px]">
                    <Image
                      src="/brand/sinwoo-logo.png"
                      alt="Sinwoo International"
                      width={800}
                      height={389}
                      className="h-auto w-full brightness-[1.9] contrast-[0.96]"
                      priority
                    />
                  </div>

                  <div className="space-y-3">
                    <p className="font-brand text-[11px] font-semibold uppercase tracking-[0.32em] text-white/58">
                      {messages.desktopLabel}
                    </p>
                    <div className="space-y-4">
                      <h1 className="font-brand max-w-[460px] text-[clamp(1.85rem,2.4vw,2.65rem)] font-semibold leading-[1.02] tracking-[-0.05em] text-white">
                        {messages.productName}
                      </h1>
                      <p className="max-w-[470px] text-[15px] leading-7 text-white/72">
                        {messages.tagline}
                      </p>
                    </div>
                  </div>

                  <div className="h-px w-full bg-white/14" />

                  <div className="grid max-w-[500px] gap-3">
                    {messages.desktopHighlights.map((item) => (
                      <div
                        key={item}
                        className="flex min-h-[72px] items-start border-l-2 border-white/28 pl-4 pr-4 text-[14px] leading-6 text-white/86"
                      >
                        {item}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="flex h-full items-center overflow-y-auto bg-[linear-gradient(180deg,rgba(255,255,255,0.94)_0%,rgba(247,249,252,0.98)_100%)] px-9 py-8">
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

        <footer className="mt-auto flex shrink-0 flex-col gap-1 border-t border-slate-200/90 pt-2.5 text-center text-[10px] leading-5 text-slate-500 sm:text-[11px] lg:hidden">
          <p className="font-brand font-semibold tracking-[0.16em] text-slate-700">{messages.footerCompany}</p>
          <p>{messages.footerDescription}</p>
          <p>{messages.footerCopyright}</p>
        </footer>

        <footer className="hidden lg:absolute lg:bottom-5 lg:left-8 lg:right-8 lg:flex lg:items-center lg:justify-between lg:border-t lg:border-slate-200/90 lg:pt-2 lg:text-[11px] lg:text-slate-500">
          <p className="font-brand font-semibold tracking-[0.16em] text-slate-700">{messages.footerCompany}</p>
          <p>{messages.footerDescription}</p>
          <p>{messages.footerCopyright}</p>
        </footer>
      </div>
    </main>
  );
}

"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";

import { CredLoginPanel } from "@/features/auth/cred-login-panel";
import { LocaleCombobox } from "@/components/forms/locale-combobox";
import type { AuthProviderItem } from "@/lib/api/auth-contract";
import {
  detectBrowserLoginLocale,
  getLoginMsgs,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-cnt";

type Props = {
  backendBaseUrl: string;
  providers: AuthProviderItem[];
};

export function LoginPageShell({ backendBaseUrl, providers }: Props) {
  const [locale, setLocale] = useState<LoginLocale>("en");

  useEffect(() => {
    const savedLocale = window.localStorage.getItem(LOGIN_LOCALE_STORAGE_KEY);
    const resolvedLocale = isSupportedLoginLocale(savedLocale) ? savedLocale : detectBrowserLoginLocale();
    setLocale(resolvedLocale);
  }, []);

  useEffect(() => {
    document.documentElement.lang = locale;
    window.localStorage.setItem(LOGIN_LOCALE_STORAGE_KEY, locale);
  }, [locale]);

  const msgs = useMemo(() => getLoginMsgs(locale), [locale]);

  return (
    <main className="login-shell relative bg-[linear-gradient(180deg,_#f5f8fc_0%,_#eaf0f7_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-6rem] top-[-5rem] h-56 w-56 rounded-full bg-[rgba(34,58,122,0.07)] blur-3xl" />
        <div className="absolute right-[-3rem] top-[-4rem] h-64 w-64 rounded-full bg-[rgba(72,104,181,0.08)] blur-3xl" />
        <div className="absolute inset-x-0 top-0 h-px bg-[linear-gradient(90deg,rgba(35,58,122,0)_0%,rgba(35,58,122,0.2)_50%,rgba(35,58,122,0)_100%)]" />
      </div>

      <div className="login-shell-inner relative mx-auto flex max-w-7xl flex-col box-border px-4 py-4 sm:px-6 lg:px-8 lg:py-6">
        <div className="flex justify-end pb-3 lg:absolute lg:right-8 lg:top-6 lg:z-20 lg:pb-0">
          <LocaleCombobox
            idPrefix="login"
            value={locale}
            localeLabel={msgs.localeLabel}
            localeNames={msgs.localeNames}
            onChange={setLocale}
            buttonClassName="min-w-[166px] justify-between rounded-xl border-slate-300 bg-white/90 px-3.5 py-2 text-[12px] font-medium shadow-sm"
            mnuClassName="min-w-[186px]"
          />
        </div>

        <section className="flex flex-1 items-center justify-center py-3 lg:min-h-0 lg:py-0">
          <div className="w-full lg:hidden">
            <div className="mx-auto w-full max-w-[460px] space-y-4">
              <div className="rounded-[24px] border border-slate-200/80 bg-white/80 px-5 py-5 shadow-[0_18px_40px_rgba(15,23,42,0.08)] backdrop-blur">
                <div className="login-mobile-brand-row flex flex-col gap-4 sm:flex-row sm:items-start">
                  <div className="login-brand-logo login-brand-logo-mobile">
                    <Image
                      src="/brand/sinwoo-logo.png"
                      alt="Sinwoo International"
                      width={800}
                      height={389}
                      className="login-brand-logo-image"
                      priority
                    />
                  </div>
                  <div className="login-mobile-copy min-w-0 space-y-2">
                    <p className="font-brand text-[10px] font-semibold uppercase tracking-[0.24em] text-slate-500">
                      {msgs.platformEyebrow}
                    </p>
                    <h1 className="login-mobile-title font-brand text-[28px] font-semibold tracking-tight text-slate-950">
                      {msgs.platformTitle}
                    </h1>
                    <p className="login-mobile-desc text-[13px] leading-5 text-slate-600">
                      {msgs.platformDesc}
                    </p>
                  </div>
                </div>
              </div>

              <CredLoginPanel
                backendBaseUrl={backendBaseUrl}
                providers={providers}
                locale={locale}
                mode="mobile"
              />
            </div>
          </div>

          <div className="hidden w-full min-w-0 lg:flex lg:flex-1 lg:items-center">
            <div className="login-desktop-frame grid w-full overflow-hidden rounded-[32px] border border-slate-200/80 bg-white/80 shadow-[0_28px_60px_rgba(15,23,42,0.10)] backdrop-blur">
              <div className="relative flex h-full min-w-0 flex-col justify-center overflow-hidden bg-[linear-gradient(180deg,rgba(244,248,253,0.95)_0%,rgba(234,240,247,0.92)_100%)] px-10 py-10">
                <div className="absolute inset-y-10 right-0 w-px bg-[linear-gradient(180deg,rgba(35,58,122,0)_0%,rgba(35,58,122,0.18)_20%,rgba(35,58,122,0.18)_80%,rgba(35,58,122,0)_100%)]" />

                <div className="relative flex flex-col gap-8">
                  <div className="login-desktop-brand-row">
                    <div className="login-brand-logo login-brand-logo-desktop">
                      <Image
                        src="/brand/sinwoo-logo.png"
                        alt="Sinwoo International"
                        width={800}
                        height={389}
                        className="login-brand-logo-image"
                        priority
                      />
                    </div>

                    <div className="login-desktop-copy space-y-3">
                      <div className="space-y-1.5">
                        <p className="font-brand text-[11px] font-semibold uppercase tracking-[0.28em] text-slate-500">
                          {msgs.platformEyebrow}
                        </p>
                        <h1 className="font-brand text-[42px] font-semibold leading-[1.04] tracking-tight text-[#102343]">
                          {msgs.platformTitle}
                        </h1>
                      </div>

                      <p className="login-desktop-desc text-[15px] leading-7 text-slate-600">
                        {msgs.platformDesc}
                      </p>
                    </div>
                  </div>

                  <div className="rounded-[24px] border border-slate-200/80 bg-white/72 px-6 py-6 shadow-[0_12px_30px_rgba(15,23,42,0.05)]">
                    <div className="space-y-4">
                      <div className="space-y-1.5">
                        <p className="font-brand text-[10px] font-semibold uppercase tracking-[0.28em] text-slate-500">
                          {msgs.desktopLabel}
                        </p>
                        <p className="max-w-[490px] text-[15px] leading-7 text-slate-700">
                          {msgs.tagline}
                        </p>
                      </div>

                      <div className="grid gap-3">
                        {msgs.desktopHighlights.map((item) => (
                          <div
                            key={item}
                            className="flex items-start gap-3 rounded-2xl border border-slate-200/70 bg-white/75 px-4 py-3"
                          >
                            <span className="mt-1 block h-2 w-2 shrink-0 rounded-full bg-[#233a7a]" />
                            <p className="text-[13px] leading-6 text-slate-700">{item}</p>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

              </div>

              <div className="login-desktop-panel flex h-full items-center bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.96)_100%)] px-10 py-10">
                <div className="w-full">
                  <CredLoginPanel
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

        <footer className="mt-auto flex shrink-0 flex-col gap-1 border-t border-slate-200/90 pt-3 text-center text-[10px] leading-4 text-slate-500 sm:text-[11px] lg:mt-4 lg:flex-row lg:items-center lg:justify-between lg:gap-4 lg:text-left">
          <p className="font-brand font-semibold tracking-[0.16em] text-slate-700">{msgs.footerCo}</p>
          <p className="lg:flex-1 lg:text-center">{msgs.footerDesc}</p>
          <p>{msgs.footerCopyright}</p>
        </footer>
      </div>
    </main>
  );
}

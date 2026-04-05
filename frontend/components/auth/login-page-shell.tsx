"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";

import { CredentialLoginPanel } from "@/components/auth/credential-login-panel";
import { LocaleCombobox } from "@/components/common/locale-combobox";
import type { AuthProviderItem } from "@/lib/api/auth-contract";
import {
  detectBrowserLoginLocale,
  getLoginMessages,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-content";

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

  const messages = useMemo(() => getLoginMessages(locale), [locale]);

  return (
    <main className="login-shell relative bg-[linear-gradient(180deg,_#f7f9fc_0%,_#edf2f8_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-6rem] top-[-5rem] h-52 w-52 rounded-full bg-[rgba(34,58,122,0.08)] blur-3xl" />
        <div className="absolute right-[-4rem] top-[-5rem] h-72 w-72 rounded-full bg-[rgba(72,104,181,0.10)] blur-3xl" />
        <div className="absolute inset-x-0 bottom-0 h-40 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(255,255,255,0.52)_100%)]" />
      </div>

      <div className="login-shell-inner relative mx-auto flex max-w-6xl flex-col box-border px-3 py-2 sm:px-4 lg:px-5 lg:py-3">
        <div className="flex justify-end pb-1.5 sm:pb-2 lg:absolute lg:right-5 lg:top-3 lg:z-20 lg:pb-0">
          <LocaleCombobox
            idPrefix="login"
            value={locale}
            localeLabel={messages.localeLabel}
            localeNames={messages.localeNames}
            onChange={setLocale}
            buttonClassName="min-w-[158px] justify-between px-3 py-1.5 text-[12px]"
            menuClassName="min-w-[178px]"
          />
        </div>

        <section className="flex flex-1 items-center justify-center py-2 sm:py-3 lg:min-h-0 lg:py-0">
          <div className="w-full lg:hidden">
            <div className="mx-auto w-full max-w-[438px] space-y-2.5 text-center">
              <div className="flex flex-col items-center gap-2.5">
                <div className="w-full max-w-[120px] sm:max-w-[132px]">
                  <Image
                    src="/brand/sinwoo-logo.png"
                    alt="Sinwoo International"
                    width={800}
                    height={389}
                    className="h-auto w-full"
                    priority
                  />
                </div>

                <div className="space-y-1">
                  <p className="font-brand text-[10px] font-semibold uppercase tracking-[0.28em] text-slate-500 sm:text-[11px]">
                    {messages.productName}
                  </p>
                  <p className="mx-auto max-w-xl text-[10px] font-semibold italic tracking-[0.22em] leading-[0.95rem] text-slate-400 sm:text-[11px]">
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
            <div className="login-desktop-frame grid w-full grid-cols-[minmax(0,1.18fr)_minmax(340px,392px)] overflow-hidden rounded-[28px] border border-slate-200/80 bg-white/75 shadow-[0_22px_56px_rgba(15,23,42,0.12)] backdrop-blur">
              <div className="relative flex h-full flex-col justify-center overflow-hidden bg-[linear-gradient(145deg,_rgba(35,58,122,0.98)_0%,_rgba(17,34,78,0.92)_100%)] px-6 py-6 text-white">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.12),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(125,154,230,0.22),transparent_28%)]" />

                <div className="relative mx-auto flex w-full flex-col gap-4.5">
                  <div className="w-full max-w-[156px]">
                    <Image
                      src="/brand/sinwoo-logo.png"
                      alt="Sinwoo International"
                      width={800}
                      height={389}
                      className="h-auto w-full brightness-[1.9] contrast-[0.96]"
                      priority
                    />
                  </div>

                  <div className="space-y-2">
                    <p className="font-brand text-[10px] font-semibold uppercase tracking-[0.26em] text-white/58">
                      {messages.desktopLabel}
                    </p>
                    <div className="space-y-2.5">
                      <h1 className="font-brand max-w-[430px] text-[clamp(1.7rem,2.25vw,2.35rem)] font-semibold leading-[0.98] tracking-[-0.05em] text-white">
                        {messages.productName}
                      </h1>
                      <p className="max-w-[440px] text-[14px] leading-5 text-white/72">
                        {messages.tagline}
                      </p>
                    </div>
                  </div>

                  <div className="h-px w-full bg-white/14" />

                  <div className="grid max-w-[560px] gap-2">
                    {messages.desktopHighlights.map((item) => (
                      <div
                        key={item}
                        className="flex min-h-[auto] items-start border-l-2 border-white/28 pl-3 pr-3 text-[13px] leading-5 text-white/86"
                      >
                        {item}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="login-desktop-panel flex h-full items-center bg-[linear-gradient(180deg,rgba(255,255,255,0.94)_0%,rgba(247,249,252,0.98)_100%)] px-6 py-5">
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

        <footer className="mt-auto flex shrink-0 flex-col gap-0.5 border-t border-slate-200/90 pt-1.5 text-center text-[10px] leading-4 text-slate-500 sm:text-[11px] lg:mt-2.5 lg:flex-row lg:items-center lg:justify-between lg:gap-4 lg:text-left">
          <p className="font-brand font-semibold tracking-[0.16em] text-slate-700">{messages.footerCompany}</p>
          <p className="lg:flex-1 lg:text-center">{messages.footerDescription}</p>
          <p>{messages.footerCopyright}</p>
        </footer>
      </div>
    </main>
  );
}

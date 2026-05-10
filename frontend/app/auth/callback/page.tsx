"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { CheckCircle2, TriangleAlert } from "lucide-react";

import { buttonVariants } from "@/components/ui/button";
import { Card, CardCnt, CardDesc, CardHeader, CardTitle } from "@/components/ui/card";
import { getAuthCallbackMsgs } from "@/lib/i18n/auth-callback-cnt";
import {
  detectBrowserLoginLocale,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-cnt";
import { cn } from "@/lib/utils";

export default function AuthCallbackPage() {
  const [params, setParams] = useState<URLSearchParams | null>(null);
  const [locale, setLocale] = useState<LoginLocale>("en");

  useEffect(() => {
    const savedLocale = window.localStorage.getItem(LOGIN_LOCALE_STORAGE_KEY);
    setLocale(isSupportedLoginLocale(savedLocale) ? savedLocale : detectBrowserLoginLocale());
    setParams(new URLSearchParams(window.location.search));
  }, []);

  const accessToken = params?.get("accessToken") ?? null;
  const refreshToken = params?.get("refreshToken") ?? null;
  const providerCd = params?.get("providerCd") ?? null;
  const lgnId = params?.get("lgnId") ?? params?.get("usrId") ?? null;
  const tenantCd = params?.get("tenantCd") ?? params?.get("tenantId") ?? null;
  const error = params?.get("error") ?? null;

  useEffect(() => {
    if (accessToken) {
      window.localStorage.setItem("sinwoo.accessToken", accessToken);
    }
    if (refreshToken) {
      window.localStorage.setItem("sinwoo.refreshToken", refreshToken);
    }
  }, [accessToken, refreshToken]);

  const isSuccess = useMemo(() => Boolean(accessToken) && !error, [accessToken, error]);
  const msgs = useMemo(() => getAuthCallbackMsgs(locale), [locale]);

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(15,23,42,0.1),_transparent_35%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
      <div className="mx-auto flex min-h-screen max-w-3xl items-center px-4 py-8">
        <Card className="w-full border-white bg-white/90">
          <CardHeader>
            <div className={`mb-3 flex h-12 w-12 items-center justify-center rounded-2xl ${isSuccess ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"}`}>
              {isSuccess ? <CheckCircle2 className="h-5 w-5" /> : <TriangleAlert className="h-5 w-5" />}
            </div>
            <CardTitle>{isSuccess ? msgs.successTitle : msgs.failureTitle}</CardTitle>
            <CardDesc>
              {isSuccess
                ? msgs.successDesc
                : msgs.failureDesc}
            </CardDesc>
          </CardHeader>
          <CardCnt className="space-y-4 text-sm text-slate-600">
            {isSuccess ? (
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <p>{msgs.providerLabel}: {providerCd}</p>
                <p>{msgs.usrCdLabel}: {lgnId}</p>
                <p>{msgs.tenantCdLabel}: {tenantCd}</p>
              </div>
            ) : (
              <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-red-700">
                {error ?? msgs.unknownError}
              </div>
            )}

            <div className="flex gap-3">
              <Link href="/" className={cn(buttonVariants({ variant: "default" }))}>
                {msgs.goToDashboard}
              </Link>
              <Link href="/login" className={cn(buttonVariants({ variant: "outline" }))}>
                {msgs.backToLogin}
              </Link>
            </div>
          </CardCnt>
        </Card>
      </div>
    </main>
  );
}

"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { LoaderCircle } from "lucide-react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { ApiErrorResponse, AuthProviderItem, AuthTokenResponse, CredentialLoginRequest } from "@/lib/api/auth-contract";
import { getLoginMessages, type LoginLocale } from "@/lib/i18n/login-content";
import { cn } from "@/lib/utils";

type Props = {
  backendBaseUrl: string;
  providers: AuthProviderItem[];
  locale: LoginLocale;
  mode?: "desktop" | "mobile";
};

const SAVED_EMAIL_KEY = "sinwoo.savedLoginEmail";
const SAVE_EMAIL_YN_KEY = "sinwoo.saveLoginEmailYn";

async function resolveLoginErrorMessage(response: Response, locale: LoginLocale): Promise<string> {
  const messages = getLoginMessages(locale).errorMessages;

  try {
    const contentType = response.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      const payload = (await response.json()) as Partial<ApiErrorResponse>;
      if (payload.code && messages[payload.code]) {
        return messages[payload.code];
      }
      if (payload.message && payload.message.trim()) {
        return payload.message.trim();
      }
      if (payload.error && payload.error.trim()) {
        return payload.error.trim();
      }
    }

    const body = await response.text();
    if (body.trim()) {
      return body.trim();
    }
  } catch {
    // Ignore parse failures and fall back to generic text.
  }

  if (response.status === 401) {
    return messages.DEFAULT_401;
  }

  if (response.status === 403) {
    return messages.DEFAULT_403;
  }

  if (response.status === 400) {
    return messages.DEFAULT_400;
  }

  return messages.DEFAULT_FALLBACK;
}

export function CredentialLoginPanel({ backendBaseUrl, providers, locale, mode = "mobile" }: Props) {
  const router = useRouter();
  const [eml, setEml] = useState("");
  const [pwd, setPwd] = useState("");
  const [saveLoginId, setSaveLoginId] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const messages = useMemo(() => getLoginMessages(locale), [locale]);
  const isDesktop = mode === "desktop";

  useEffect(() => {
    const saveYn = window.localStorage.getItem(SAVE_EMAIL_YN_KEY);
    const savedEmail = window.localStorage.getItem(SAVED_EMAIL_KEY);

    if (saveYn === "N") {
      setSaveLoginId(false);
      return;
    }

    if (savedEmail) {
      setEml(savedEmail);
    }
  }, []);

  useEffect(() => {
    if (saveLoginId) {
      window.localStorage.setItem(SAVE_EMAIL_YN_KEY, "Y");
      if (eml.trim()) {
        window.localStorage.setItem(SAVED_EMAIL_KEY, eml.trim().toLowerCase());
      }
      return;
    }

    window.localStorage.setItem(SAVE_EMAIL_YN_KEY, "N");
    window.localStorage.removeItem(SAVED_EMAIL_KEY);
  }, [saveLoginId, eml]);

  const canSubmit = useMemo(
    () => eml.trim().length > 0 && pwd.trim().length > 0,
    [eml, pwd]
  );

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSubmit || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    const payload: CredentialLoginRequest = {
      eml: eml.trim().toLowerCase(),
      pwd,
    };

    try {
      const response = await fetch(`${backendBaseUrl}/api/v1/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(await resolveLoginErrorMessage(response, locale));
      }

      const data = (await response.json()) as AuthTokenResponse;
      window.localStorage.setItem("sinwoo.accessToken", data.accessToken);
      window.localStorage.setItem("sinwoo.refreshToken", data.refreshToken);
      window.localStorage.setItem("sinwoo.currentUser", JSON.stringify(data.user));
      router.push("/");
      router.refresh();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : messages.errorMessages.DEFAULT_FALLBACK);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Card
      className={cn(
        "w-full",
        isDesktop
          ? "border-0 bg-transparent shadow-none"
          : "border border-slate-200 bg-white/96 shadow-[0_18px_38px_rgba(15,23,42,0.08)] backdrop-blur"
      )}
    >
      <CardHeader className={cn("space-y-1.5", isDesktop ? "px-0 pb-4 pt-0" : "pb-3")}>
        <div className="font-brand text-[10px] font-semibold uppercase tracking-[0.28em] text-slate-400">{messages.cardEyebrow}</div>
        <CardTitle className={cn("font-brand font-semibold tracking-tight text-slate-950", isDesktop ? "text-[28px]" : "text-[24px]")}>
          {messages.signInTitle}
        </CardTitle>
      </CardHeader>

      <CardContent className={cn("space-y-3", isDesktop ? "px-0 pb-0" : "")}>
        <form className="space-y-3" onSubmit={handleSubmit}>
          <div className="space-y-2">
            <label htmlFor="credential-email" className="text-sm font-medium text-slate-700">
              {messages.emailLabel}
            </label>
            <input
              id="credential-email"
              type="email"
              value={eml}
              onChange={(event) => setEml(event.target.value)}
              placeholder={messages.emailPlaceholder}
              autoComplete="username"
              className={cn(
                "w-full border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-[#233a7a]",
                isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-lg"
              )}
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="credential-pwd" className="text-sm font-medium text-slate-700">
              {messages.passwordLabel}
            </label>
            <input
              id="credential-pwd"
              type="password"
              value={pwd}
              onChange={(event) => setPwd(event.target.value)}
              placeholder={messages.passwordPlaceholder}
              autoComplete="current-password"
              className={cn(
                "w-full border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-[#233a7a]",
                isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-lg"
              )}
            />
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={saveLoginId}
              onChange={(event) => setSaveLoginId(event.target.checked)}
              className="h-4 w-4 rounded border-slate-300 text-slate-900"
            />
            {messages.rememberEmail}
          </label>

          {errorMessage ? (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {errorMessage}
            </div>
          ) : null}

          <Button
            type="submit"
            disabled={!canSubmit || isSubmitting}
            className={cn(
              "w-full bg-[#233a7a] text-sm font-semibold text-white hover:bg-[#1c2f64]",
              isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-lg"
            )}
          >
            {isSubmitting ? <LoaderCircle className="mr-2 h-4 w-4 animate-spin" /> : null}
            {messages.signInButton}
          </Button>
        </form>

        {providers.length > 0 ? (
          <div className="space-y-2.5">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200" />
              </div>
              <div className="relative flex justify-center">
                <span className="bg-white px-3 text-xs font-medium tracking-[0.2em] text-slate-400">{messages.ssoLabel}</span>
              </div>
            </div>

            <div className="space-y-2.5">
              {providers.map((provider) => (
                <Link
                  key={provider.registrationId}
                  href={`${backendBaseUrl}${provider.authorizeUri}`}
                  className={cn(
                    buttonVariants({ variant: "outline" }),
                    "flex w-full justify-center border-slate-300 text-sm font-medium text-slate-700",
                    isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-lg"
                  )}
                >
                  {messages.continueWith} {provider.providerNm}
                </Link>
              ))}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

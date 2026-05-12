"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { LoaderCircle } from "lucide-react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardCnt, CardHeader, CardTitle } from "@/components/ui/card";
import type {
  ApiErrorResponse,
  AuthProviderItem,
  AuthTokenResponse,
  CredKeyResponse,
  CredLoginRequest,
} from "@/lib/api/auth-contract";
import { getLoginMsgs, type LoginLocale } from "@/lib/i18n/login-cnt";
import { encryptCredentialPassword } from "@/lib/auth/cred-encryption";
import { cn } from "@/lib/utils";

type Props = {
  backendBaseUrl: string;
  providers: AuthProviderItem[];
  locale: LoginLocale;
  mode?: "desktop" | "mobile";
};

const SAVED_EMAIL_KEY = "sinwoo.savedLoginEmail";
const SAVE_EMAIL_YN_KEY = "sinwoo.saveLoginEmailYn";

async function getCredKey(backendBaseUrl: string): Promise<CredKeyResponse> {
  const response = await fetch(`${backendBaseUrl}/api/v1/auth/credential-key`, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error("Unable to initialize secure sign-in.");
  }

  return response.json() as Promise<CredKeyResponse>;
}

async function resolveLoginErrorMsg(response: Response, locale: LoginLocale): Promise<string> {
  const msgs = getLoginMsgs(locale).errorMsgs;

  try {
    const cntType = response.headers.get("content-type") ?? "";
    if (cntType.includes("application/json")) {
      const payload = (await response.json()) as Partial<ApiErrorResponse>;
      if (payload.code && msgs[payload.code]) {
        return msgs[payload.code];
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
    return msgs.DEFAULT_401;
  }

  if (response.status === 403) {
    return msgs.DEFAULT_403;
  }

  if (response.status === 400) {
    return msgs.DEFAULT_400;
  }

  return msgs.DEFAULT_FALLBACK;
}

export function CredLoginPanel({ backendBaseUrl, providers, locale, mode = "mobile" }: Props) {
  const router = useRouter();
  const [eml, setEml] = useState("");
  const [pwd, setPwd] = useState("");
  const [saveLoginId, setSaveLoginId] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const msgs = useMemo(() => getLoginMsgs(locale), [locale]);
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
    setErrorMsg(null);

    try {
      let pwdEnc: string;
      try {
        const credKey = await getCredKey(backendBaseUrl);
        pwdEnc = await encryptCredentialPassword(pwd, credKey);
      } catch {
        throw new Error(msgs.errorMsgs.DEFAULT_FALLBACK);
      }

      const payload: CredLoginRequest = {
        eml: eml.trim().toLowerCase(),
        pwdEnc,
      };

      const response = await fetch(`${backendBaseUrl}/api/v1/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(await resolveLoginErrorMsg(response, locale));
      }

      const data = (await response.json()) as AuthTokenResponse;
      window.localStorage.setItem("sinwoo.accessToken", data.accessToken);
      window.localStorage.setItem("sinwoo.refreshToken", data.refreshToken);
      window.localStorage.setItem("sinwoo.currentUsr", JSON.stringify(data.user));
      router.push("/");
      router.refresh();
    } catch (error) {
      setErrorMsg(error instanceof Error ? error.message : msgs.errorMsgs.DEFAULT_FALLBACK);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Card
      className={cn(
        "w-full",
        isDesktop
          ? "rounded-[28px] border border-slate-200/80 bg-white/96 shadow-[0_18px_38px_rgba(15,23,42,0.08)] backdrop-blur"
          : "rounded-[24px] border border-slate-200/90 bg-white/96 shadow-[0_18px_38px_rgba(15,23,42,0.08)] backdrop-blur"
      )}
    >
      <CardHeader className={cn("space-y-2", isDesktop ? "px-7 pb-4 pt-7" : "px-5 pb-3 pt-5")}>
        <div className="font-brand text-[10px] font-semibold uppercase tracking-[0.28em] text-slate-400">{msgs.cardEyebrow}</div>
        <CardTitle className={cn("font-brand font-semibold tracking-tight text-slate-950", isDesktop ? "text-[30px]" : "text-[24px]")}>
          {msgs.signInTitle}
        </CardTitle>
        <p className="max-w-md text-[14px] leading-6 text-slate-600">
          {msgs.formDesc}
        </p>
      </CardHeader>

      <CardCnt className={cn("space-y-4", isDesktop ? "px-7 pb-7 pt-0" : "px-5 pb-5 pt-0")}>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div className="space-y-1.5">
            <label htmlFor="credential-email" className="text-[13px] font-semibold uppercase tracking-[0.08em] text-slate-500">
              {msgs.emailLabel}
            </label>
            <input
              id="credential-email"
              type="email"
              value={eml}
              onChange={(event) => setEml(event.target.value)}
              placeholder={msgs.emailPh}
              autoComplete="username"
              className={cn(
                "w-full border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-[#233a7a] focus:ring-4 focus:ring-[#233a7a]/10",
                isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-xl"
              )}
            />
          </div>

          <div className="space-y-1.5">
            <label htmlFor="credential-pwd" className="text-[13px] font-semibold uppercase tracking-[0.08em] text-slate-500">
              {msgs.passwordLabel}
            </label>
            <input
              id="credential-pwd"
              type="password"
              value={pwd}
              onChange={(event) => setPwd(event.target.value)}
              placeholder={msgs.passwordPh}
              autoComplete="current-password"
              className={cn(
                "w-full border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-[#233a7a] focus:ring-4 focus:ring-[#233a7a]/10",
                isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-xl"
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
            {msgs.rememberEmail}
          </label>

          {errorMsg ? (
            <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {errorMsg}
            </div>
          ) : null}

          <Button
            type="submit"
            disabled={!canSubmit || isSubmitting}
            className={cn(
              "w-full bg-[#233a7a] text-sm font-semibold text-white shadow-[0_12px_24px_rgba(35,58,122,0.22)] hover:bg-[#1c2f64]",
              isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-xl"
            )}
          >
            {isSubmitting ? <LoaderCircle className="mr-2 h-4 w-4 animate-spin" /> : null}
            {msgs.signInButton}
          </Button>
        </form>

        {providers.length > 0 ? (
          <div className="space-y-3 pt-1">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200" />
              </div>
              <div className="relative flex justify-center">
                <span className="bg-white px-3 text-xs font-medium tracking-[0.2em] text-slate-400">{msgs.ssoLabel}</span>
              </div>
            </div>

            <div className="space-y-2.5">
              {providers.map((provider) => (
                <Link
                  key={provider.registrationId}
                  href={`${backendBaseUrl}${provider.authorizeUri}`}
                  className={cn(
                    buttonVariants({ variant: "outline" }),
                    "flex w-full justify-center border-slate-300 bg-white text-sm font-medium text-slate-700",
                    isDesktop ? "h-12 rounded-2xl" : "h-11 rounded-xl"
                  )}
                >
                  {msgs.continueWith} {provider.providerNm}
                </Link>
              ))}
            </div>
          </div>
        ) : null}
      </CardCnt>
    </Card>
  );
}

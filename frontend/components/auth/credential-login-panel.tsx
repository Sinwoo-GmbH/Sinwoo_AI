"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { LoaderCircle } from "lucide-react";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type { AuthProviderItem, AuthTokenResponse, CredentialLoginRequest } from "@/lib/api/auth-contract";
import { cn } from "@/lib/utils";

type Props = {
  backendBaseUrl: string;
  providers: AuthProviderItem[];
};

const SAVED_EMAIL_KEY = "sinwoo.savedLoginEmail";
const SAVE_EMAIL_YN_KEY = "sinwoo.saveLoginEmailYn";

export function CredentialLoginPanel({ backendBaseUrl, providers }: Props) {
  const router = useRouter();
  const [eml, setEml] = useState("");
  const [pwd, setPwd] = useState("");
  const [saveLoginId, setSaveLoginId] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

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
        const body = await response.text();
        throw new Error(body || "Login failed");
      }

      const data = (await response.json()) as AuthTokenResponse;
      window.localStorage.setItem("sinwoo.accessToken", data.accessToken);
      window.localStorage.setItem("sinwoo.refreshToken", data.refreshToken);
      window.localStorage.setItem("sinwoo.currentUser", JSON.stringify(data.user));
      router.push("/");
      router.refresh();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Login failed");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Card className="border-slate-200 bg-white shadow-[0_24px_60px_rgba(15,23,42,0.08)]">
      <CardHeader className="space-y-3 pb-6">
        <div className="inline-flex w-fit items-center rounded-full border border-slate-200 px-3 py-1 text-xs font-medium text-slate-500">
          SINWOO Workspace
        </div>
        <div className="space-y-2">
          <CardTitle className="text-3xl font-semibold tracking-tight text-slate-950">로그인</CardTitle>
          <CardDescription className="text-sm leading-6 text-slate-500">
            이메일 주소로 회사 영역을 자동 인식합니다. 레거시처럼 단순하게 시작하고, 필요한 경우 SSO로 이어집니다.
          </CardDescription>
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div className="space-y-2">
            <label htmlFor="credential-email" className="text-sm font-medium text-slate-700">
              이메일 주소
            </label>
            <input
              id="credential-email"
              type="email"
              value={eml}
              onChange={(event) => setEml(event.target.value)}
              placeholder="name@company.com"
              autoComplete="username"
              className="h-12 w-full rounded-xl border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-slate-500"
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="credential-pwd" className="text-sm font-medium text-slate-700">
              비밀번호
            </label>
            <input
              id="credential-pwd"
              type="password"
              value={pwd}
              onChange={(event) => setPwd(event.target.value)}
              placeholder="비밀번호 입력"
              autoComplete="current-password"
              className="h-12 w-full rounded-xl border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-slate-500"
            />
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={saveLoginId}
              onChange={(event) => setSaveLoginId(event.target.checked)}
              className="h-4 w-4 rounded border-slate-300 text-slate-900"
            />
            로그인 이메일 저장
          </label>

          {errorMessage ? (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {errorMessage}
            </div>
          ) : null}

          <Button type="submit" disabled={!canSubmit || isSubmitting} className="h-12 w-full rounded-xl text-sm font-semibold">
            {isSubmitting ? <LoaderCircle className="mr-2 h-4 w-4 animate-spin" /> : null}
            로그인
          </Button>
        </form>

        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200" />
          </div>
          <div className="relative flex justify-center">
            <span className="bg-white px-3 text-xs font-medium tracking-[0.2em] text-slate-400">OR</span>
          </div>
        </div>

        <div className="space-y-3">
          {providers.length === 0 ? (
            <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
              SSO는 아직 설정되지 않았습니다. 현재는 이메일 로그인만 사용할 수 있습니다.
            </div>
          ) : (
            providers.map((provider) => (
              <Link
                key={provider.registrationId}
                href={`${backendBaseUrl}${provider.authorizeUri}`}
                className={cn(
                  buttonVariants({ variant: "outline" }),
                  "flex h-12 w-full justify-center rounded-xl border-slate-300 text-sm font-medium text-slate-700"
                )}
              >
                {provider.providerNm}로 계속하기
              </Link>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

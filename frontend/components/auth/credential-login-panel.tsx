"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { KeyRound, LoaderCircle } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type { AuthTokenResponse, CredentialLoginRequest } from "@/lib/api/auth-contract";

type Props = {
  backendBaseUrl: string;
};

export function CredentialLoginPanel({ backendBaseUrl }: Props) {
  const router = useRouter();
  const [tenantCd, setTenantCd] = useState("SINWOO");
  const [lgnId, setLgnId] = useState("");
  const [pwd, setPwd] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const canSubmit = useMemo(
    () => tenantCd.trim().length > 0 && lgnId.trim().length > 0 && pwd.trim().length > 0,
    [tenantCd, lgnId, pwd]
  );

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSubmit || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    const payload: CredentialLoginRequest = {
      tenantCd: tenantCd.trim().toUpperCase(),
      lgnId: lgnId.trim(),
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
    <Card className="border-white bg-white/90">
      <CardHeader>
        <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-950 text-white">
          <KeyRound className="h-5 w-5" />
        </div>
        <CardTitle>ID / Password Sign-In</CardTitle>
        <CardDescription>
          Sign in with tenant code, login ID, and password. This is the direct application login path for internal users and B2B customers.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div className="space-y-2">
            <label htmlFor="credential-tenantCd" className="text-sm font-medium text-slate-700">
              Tenant Code
            </label>
            <input
              id="credential-tenantCd"
              value={tenantCd}
              onChange={(event) => setTenantCd(event.target.value)}
              placeholder="SINWOO"
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-slate-400"
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="credential-lgnId" className="text-sm font-medium text-slate-700">
              Login ID
            </label>
            <input
              id="credential-lgnId"
              value={lgnId}
              onChange={(event) => setLgnId(event.target.value)}
              placeholder="SINWOO.ADMIN"
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-slate-400"
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="credential-pwd" className="text-sm font-medium text-slate-700">
              Password
            </label>
            <input
              id="credential-pwd"
              type="password"
              value={pwd}
              onChange={(event) => setPwd(event.target.value)}
              placeholder="Password"
              className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-slate-400"
            />
          </div>

          {errorMessage ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              {errorMessage}
            </div>
          ) : null}

          <Button type="submit" disabled={!canSubmit || isSubmitting} className="w-full">
            {isSubmitting ? <LoaderCircle className="mr-2 h-4 w-4 animate-spin" /> : null}
            Sign in with ID and Password
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

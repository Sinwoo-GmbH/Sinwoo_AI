"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { LockKeyhole, ShieldCheck } from "lucide-react";

import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import type { AuthProviderItem } from "@/lib/api/auth-contract";
import { cn } from "@/lib/utils";

type Props = {
  providers: AuthProviderItem[];
  backendBaseUrl: string;
};

export function OAuthLoginPanel({ providers, backendBaseUrl }: Props) {
  const [tenantCd, setTenantCd] = useState("SINWOO");

  const sanitizedTenantCd = useMemo(() => tenantCd.trim().toUpperCase(), [tenantCd]);

  return (
    <Card className="border-white bg-white/90">
      <CardHeader>
        <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-950 text-white">
          <LockKeyhole className="h-5 w-5" />
        </div>
        <CardTitle>OAuth Sign-In Bridge</CardTitle>
        <CardDescription>
          Enter a tenant code and start OAuth login. The backend links or provisions the user and issues SINWOO JWT tokens.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <label htmlFor="tenantCd" className="text-sm font-medium text-slate-700">
            Tenant Code
          </label>
          <input
            id="tenantCd"
            value={tenantCd}
            onChange={(event) => setTenantCd(event.target.value)}
            placeholder="SINWOO"
            className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none ring-0 transition focus:border-slate-400"
          />
          <p className="text-xs text-slate-500">
            The tenant code decides which B2B customer or internal tenant the OAuth login is attached to.
          </p>
        </div>

        <div className="grid gap-3">
          {providers.length === 0 ? (
            <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-slate-700">
              OAuth provider is not configured yet. Add provider credentials in backend environment variables first.
            </div>
          ) : null}

          {providers.map((provider) => (
            <div
              key={provider.registrationId}
              className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <p className="font-medium text-slate-950">{provider.providerNm}</p>
                <p className="text-sm text-slate-500">{provider.registrationId}</p>
              </div>
              <Link
                href={`${backendBaseUrl}${provider.authorizeUri}?tenantCd=${encodeURIComponent(sanitizedTenantCd)}`}
                className={cn(
                  buttonVariants({ variant: "default" }),
                  !sanitizedTenantCd ? "pointer-events-none opacity-50" : ""
                )}
              >
                Continue with {provider.providerNm}
              </Link>
            </div>
          ))}
        </div>

        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
          <div className="mb-2 flex items-center gap-2 font-medium text-slate-900">
            <ShieldCheck className="h-4 w-4" />
            What happens next
          </div>
          <ul className="space-y-1">
            <li>1. Backend starts the OAuth login for the selected tenant.</li>
            <li>2. Provider callback links the external account to an existing user or creates one.</li>
            <li>3. SINWOO issues its own access token and refresh token for the frontend.</li>
          </ul>
        </div>
      </CardContent>
    </Card>
  );
}

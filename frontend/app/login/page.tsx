import Image from "next/image";

import { CredentialLoginPanel } from "@/components/auth/credential-login-panel";
import type { AuthProviderListResponse } from "@/lib/api/auth-contract";

export const dynamic = "force-dynamic";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function getProviders(): Promise<AuthProviderListResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/oauth/providers`, { cache: "no-store" });
  if (!response.ok) {
    return { totCnt: 0, itemList: [] };
  }
  return response.json();
}

export default async function LoginPage() {
  const providers = await getProviders();

  return (
    <main className="h-[100dvh] overflow-hidden bg-[linear-gradient(180deg,_#f3f6fa_0%,_#e7edf4_100%)]">
      <div className="mx-auto grid h-full max-w-md grid-rows-[auto_1fr_auto] px-5 py-4 sm:px-6 sm:py-5">
        <div className="pt-2 text-center">
          <div className="mx-auto w-full max-w-[190px] sm:max-w-[220px]">
            <Image
              src="/brand/sinwoo-logo.png"
              alt="Sinwoo International"
              width={800}
              height={389}
              className="h-auto w-full"
              priority
            />
          </div>

          <div className="mt-3 space-y-1">
            <p className="text-[10px] font-semibold uppercase tracking-[0.32em] text-slate-400 sm:text-[11px]">
              Powered by SINWOO International
            </p>
            <h1 className="text-[clamp(1.9rem,3vw,2.7rem)] font-semibold tracking-tight text-slate-950">
              OneGate Enterprise Suite
            </h1>
            <p className="text-xs font-medium text-slate-500 sm:text-sm">Secure Identity and Workspace Access</p>
          </div>
        </div>

        <div className="flex min-h-0 items-center py-3">
          <div className="w-full">
            <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
          </div>
        </div>

        <footer className="border-t border-slate-200 pt-3 text-center text-[10px] leading-5 text-slate-500 sm:text-[11px]">
          <p className="font-semibold tracking-[0.18em] text-slate-600">SINWOO INTERNATIONAL</p>
          <p>Enterprise B2B Operations Platform for Germany and Global Teams</p>
          <p>ggamgang@sinwoo-itc.com | Copyright © 2026 SINWOO International</p>
        </footer>
      </div>
    </main>
  );
}

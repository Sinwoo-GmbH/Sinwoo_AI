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
    <main className="min-h-screen bg-[linear-gradient(180deg,_#f3f6fa_0%,_#e7edf4_100%)]">
      <div className="mx-auto flex min-h-screen max-w-md flex-col justify-between px-5 py-10">
        <div className="flex-1" />

        <div className="w-full space-y-8">
          <div className="space-y-4 text-center">
            <div className="mx-auto w-full max-w-[280px]">
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
              <p className="text-[11px] font-semibold uppercase tracking-[0.32em] text-slate-400">SINWOO International</p>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-950">OneGate Enterprise</h1>
              <p className="text-sm font-medium text-slate-500">Identity and Workspace Access</p>
            </div>
          </div>

          <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
        </div>

        <footer className="mt-10 border-t border-slate-200 pt-5 text-center text-[11px] leading-6 text-slate-500">
          <p className="font-semibold tracking-[0.18em] text-slate-600">SINWOO INTERNATIONAL</p>
          <p>Enterprise B2B Operations Platform</p>
          <p>Copyright © 2026 SINWOO International. All rights reserved.</p>
        </footer>
      </div>
    </main>
  );
}

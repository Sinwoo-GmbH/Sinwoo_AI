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
      <div className="mx-auto flex min-h-screen max-w-md items-center px-5 py-10">
        <div className="w-full space-y-8">
          <div className="space-y-5 text-center">
            <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-5 shadow-[0_18px_45px_rgba(15,23,42,0.08)]">
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
              <p className="text-[11px] font-semibold uppercase tracking-[0.35em] text-slate-400">Enterprise Access Portal</p>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-950">OneGate</h1>
            </div>
          </div>

          <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
        </div>
      </div>
    </main>
  );
}

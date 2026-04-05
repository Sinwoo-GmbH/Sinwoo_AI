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
    <main className="login-shell relative bg-[linear-gradient(180deg,_#f7f9fc_0%,_#edf2f8_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-6rem] top-[-5rem] h-52 w-52 rounded-full bg-[rgba(34,58,122,0.08)] blur-3xl" />
        <div className="absolute right-[-4rem] top-[-5rem] h-72 w-72 rounded-full bg-[rgba(72,104,181,0.10)] blur-3xl" />
        <div className="absolute inset-x-0 bottom-0 h-40 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(255,255,255,0.52)_100%)]" />
      </div>

      <div className="login-shell-inner relative mx-auto flex max-w-5xl flex-col box-border px-5 py-4 sm:px-6 lg:px-8 lg:py-5">
        <section className="flex flex-1 items-center justify-center py-3 sm:py-5 lg:min-h-0 lg:py-4">
          <div className="w-full max-w-[438px] space-y-4 text-center lg:space-y-3.5">
            <div className="flex flex-col items-center gap-4">
              <div className="w-full max-w-[136px] sm:max-w-[148px]">
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
                <p className="text-[10px] font-semibold uppercase tracking-[0.34em] text-slate-500 sm:text-[11px]">
                  OneGate Enterprise Console
                </p>
                <p className="mx-auto max-w-xl text-[10px] font-semibold italic tracking-[0.28em] leading-4 text-slate-400 sm:text-[11px]">
                  Access Sinwoo internal administration and customer workspace operations.
                </p>
              </div>
            </div>

            <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
          </div>
        </section>

        <footer className="mt-auto flex shrink-0 flex-col gap-1 border-t border-slate-200/90 pt-2.5 text-center text-[10px] leading-5 text-slate-500 sm:text-[11px] lg:flex-row lg:items-center lg:justify-between">
          <p className="font-semibold tracking-[0.16em] text-slate-700">SINWOO INTERNATIONAL</p>
          <p>Enterprise B2B Operations Platform for Germany and Global Teams</p>
          <p>Copyright 2026 SINWOO International. All rights reserved.</p>
        </footer>
      </div>
    </main>
  );
}

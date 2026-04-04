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
    <main className="relative h-[100dvh] overflow-hidden bg-[linear-gradient(180deg,_#f6f8fb_0%,_#e8eef5_100%)]">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -left-24 top-12 h-56 w-56 rounded-full bg-[rgba(32,54,122,0.08)] blur-3xl" />
        <div className="absolute right-[-4rem] top-[-3rem] h-72 w-72 rounded-full bg-[rgba(78,110,183,0.12)] blur-3xl" />
        <div className="absolute bottom-[-5rem] left-[22%] h-64 w-64 rounded-full bg-[rgba(255,255,255,0.55)] blur-3xl" />
      </div>

      <div className="relative mx-auto grid h-full max-w-6xl grid-rows-[1fr_auto] px-5 py-4 sm:px-6 lg:px-10 lg:py-6">
        <div className="grid min-h-0 items-center gap-8 lg:grid-cols-[minmax(0,1fr)_420px] lg:gap-14">
          <section className="space-y-6 text-center lg:text-left">
            <div className="mx-auto w-full max-w-[168px] lg:mx-0 lg:max-w-[210px]">
              <Image
                src="/brand/sinwoo-logo.png"
                alt="Sinwoo International"
                width={800}
                height={389}
                className="h-auto w-full"
                priority
              />
            </div>

            <div className="space-y-3">
              <p className="text-[10px] font-semibold uppercase tracking-[0.34em] text-slate-500 sm:text-[11px]">
                Enterprise Identity Layer
              </p>
              <h1 className="text-[clamp(2.3rem,4.2vw,4.25rem)] font-semibold leading-[0.96] tracking-[-0.04em] text-slate-950">
                OneGate Enterprise Console
              </h1>
              <p className="mx-auto max-w-xl text-sm leading-6 text-slate-600 lg:mx-0 lg:max-w-[560px] lg:text-[15px]">
                Secure access for internal operations, customer administration, and audit-ready B2B workspace control.
              </p>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-2.5 text-[11px] font-medium uppercase tracking-[0.16em] text-slate-600 lg:justify-start">
              <span className="rounded-full border border-slate-300/90 bg-white/75 px-3 py-1.5 shadow-sm backdrop-blur">
                Internal Admin
              </span>
              <span className="rounded-full border border-slate-300/90 bg-white/75 px-3 py-1.5 shadow-sm backdrop-blur">
                Customer Workspace
              </span>
              <span className="rounded-full border border-slate-300/90 bg-white/75 px-3 py-1.5 shadow-sm backdrop-blur">
                Germany-Ready Audit Trail
              </span>
            </div>
          </section>

          <div className="flex justify-center lg:justify-end">
            <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
          </div>
        </div>

        <footer className="flex flex-col gap-1.5 border-t border-slate-200/90 pt-3 text-center text-[10px] leading-5 text-slate-500 sm:text-[11px] lg:flex-row lg:items-center lg:justify-between lg:text-left">
          <p className="font-semibold tracking-[0.16em] text-slate-700">SINWOO INTERNATIONAL</p>
          <p>Enterprise B2B Operations Platform for Germany and Global Teams</p>
          <p>Copyright © 2026 SINWOO International. All rights reserved.</p>
        </footer>
      </div>
    </main>
  );
}

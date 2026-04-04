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
    <main className="min-h-screen bg-[linear-gradient(180deg,_#f8fafc_0%,_#eef2f7_100%)]">
      <div className="mx-auto flex min-h-screen max-w-md items-center px-5 py-10">
        <div className="w-full space-y-6">
          <div className="space-y-3 text-center">
            <p className="text-xs font-semibold uppercase tracking-[0.35em] text-slate-400">Sinwoo AI Platform</p>
            <h1 className="text-4xl font-semibold tracking-tight text-slate-950">SINWOO</h1>
            <p className="text-sm leading-6 text-slate-500">
              독일 법인 운영과 내부 관리가 함께 돌아가는 B2B 워크스페이스 로그인입니다.
            </p>
          </div>

          <CredentialLoginPanel backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
        </div>
      </div>
    </main>
  );
}

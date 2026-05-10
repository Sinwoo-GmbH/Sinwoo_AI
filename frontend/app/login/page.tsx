import { LoginPageShell } from "@/features/auth/login-page-shell";
import type { AuthProviderListResponse } from "@/lib/api/auth-contract";

export const dynamic = "force-dynamic";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function getProviders(): Promise<AuthProviderListResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/oauth/providers`, { cache: "no-store" });
    if (!response.ok) {
      return { totCnt: 0, itemList: [] };
    }
    return response.json();
  } catch {
    return { totCnt: 0, itemList: [] };
  }
}

export default async function LoginPage() {
  const providers = await getProviders();

  return (
    <LoginPageShell backendBaseUrl={API_BASE_URL} providers={providers.itemList} />
  );
}

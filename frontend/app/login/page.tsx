import Link from "next/link";
import { ChevronLeft, LogIn } from "lucide-react";

import { CredentialLoginPanel } from "@/components/auth/credential-login-panel";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { OAuthLoginPanel } from "@/components/auth/oauth-login-panel";
import type { AuthProviderListResponse } from "@/lib/api/auth-contract";
import { cn } from "@/lib/utils";

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
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(148,163,184,0.18),_transparent_40%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
      <div className="mx-auto flex min-h-screen max-w-6xl flex-col px-4 py-6 lg:px-6">
        <div className="mb-6 flex items-center justify-between">
          <Link href="/" className={cn(buttonVariants({ variant: "outline" }))}>
            <ChevronLeft className="mr-2 h-4 w-4" />
            Back to dashboard
          </Link>
          <Badge variant="secondary">{API_BASE_URL}</Badge>
        </div>

        <div className="grid flex-1 gap-6 xl:grid-cols-[0.8fr_1.2fr]">
          <Card className="border-white bg-slate-950 text-white">
            <CardHeader>
              <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10">
                <LogIn className="h-5 w-5" />
              </div>
              <CardTitle className="text-3xl">SINWOO OAuth Login</CardTitle>
              <CardDescription className="text-slate-300">
                Germany-ready B2B login bridge for internal SINWOO staff and customer tenants.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4 text-sm text-slate-200">
              <p>
                This page starts the OAuth login flow and returns SINWOO application tokens after the provider callback.
              </p>
              <div className="space-y-2 rounded-2xl bg-white/5 p-4">
                <p className="font-medium text-white">Supported flow</p>
                <ul className="space-y-1">
                  <li>- direct ID and password login</li>
                  <li>- OAuth2 / OIDC provider login</li>
                  <li>- tenant-aware user linking and auto-provisioning</li>
                  <li>- role-based menu visibility after login</li>
                  <li>- SINWOO JWT issue for frontend sessions</li>
                </ul>
              </div>
            </CardContent>
          </Card>

          <div className="grid gap-6">
            <CredentialLoginPanel backendBaseUrl={API_BASE_URL} />
            <OAuthLoginPanel providers={providers.itemList} backendBaseUrl={API_BASE_URL} />
          </div>
        </div>
      </div>
    </main>
  );
}

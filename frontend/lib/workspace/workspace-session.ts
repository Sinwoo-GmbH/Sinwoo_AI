import type { CurrentUser } from "@/lib/api/auth-contract";

export function parseWorkspaceCurrentUser(raw: string | null): CurrentUser | null {
  if (!raw) return null;

  try {
    return JSON.parse(raw) as CurrentUser;
  } catch {
    return null;
  }
}

export function clearWorkspaceAuthStorage() {
  window.localStorage.removeItem("sinwoo.accessToken");
  window.localStorage.removeItem("sinwoo.refreshToken");
  window.localStorage.removeItem("sinwoo.currentUser");
}

export function redirectWorkspaceToLogin() {
  clearWorkspaceAuthStorage();
  window.location.replace("/login");
}

export function hasWorkspaceCurrentUserSession() {
  const parsed = parseWorkspaceCurrentUser(window.localStorage.getItem("sinwoo.currentUser"));
  return Boolean(parsed?.usrId || parsed?.eml || parsed?.roleCds?.length);
}

export function isWorkspaceJwtExpired(token: string | null) {
  if (!token) return true;

  try {
    const [, payload] = token.split(".");
    if (!payload) return true;

    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    const decoded = JSON.parse(window.atob(padded)) as { exp?: number };

    if (!decoded.exp) return false;
    return decoded.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

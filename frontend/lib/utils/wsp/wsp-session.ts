import type { CurrentUsr } from "@/lib/api/auth-contract";

export function parseWspCurrentUsr(raw: string | null): CurrentUsr | null {
  if (!raw) return null;

  try {
    return JSON.parse(raw) as CurrentUsr;
  } catch {
    return null;
  }
}

export function clearWspAuthStorage() {
  window.localStorage.removeItem("sinwoo.accessToken");
  window.localStorage.removeItem("sinwoo.refreshToken");
  window.localStorage.removeItem("sinwoo.currentUsr");
}

export function redirectWspToLogin() {
  clearWspAuthStorage();
  window.location.replace("/login");
}

export function hasWspCurrentUsrSession() {
  const parsed = parseWspCurrentUsr(window.localStorage.getItem("sinwoo.currentUsr"));
  return Boolean(parsed?.usrId || parsed?.eml || parsed?.roleCds?.length);
}

export function isWspJwtExpired(token: string | null) {
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

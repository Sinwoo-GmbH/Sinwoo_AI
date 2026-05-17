"use client";

import type { Dispatch, SetStateAction } from "react";
import { useCallback, useEffect, useState } from "react";

import type { CurrentUsr } from "@/lib/api/auth-contract";
import {
  detectBrowserLoginLocale,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-cnt";
import type { WspMode } from "@/lib/utils/wsp/platform-shell-data";
import {
  buildWspStorageActorKey,
  readPersistedWspState,
  type PersistedWspShellState,
} from "@/lib/utils/wsp/wsp-storage";
import {
  clearWspAuthStorage,
  hasWspCurrentUsrSession,
  isWspJwtExpired,
  parseWspCurrentUsr,
  redirectWspToLogin,
} from "@/lib/utils/wsp/wsp-session";

type WspShellState = {
  isReady: boolean;
  locale: LoginLocale;
  setLocale: Dispatch<SetStateAction<LoginLocale>>;
  accessToken: string | null;
  setAccessToken: Dispatch<SetStateAction<string | null>>;
  mode: WspMode;
  setMode: Dispatch<SetStateAction<WspMode>>;
  sidebarCollapsed: boolean;
  setSidebarCollapsed: Dispatch<SetStateAction<boolean>>;
  loading: boolean;
  setLoading: Dispatch<SetStateAction<boolean>>;
  storageActorKey: string;
  currentUsr: CurrentUsr | null;
  initialPersistedState: PersistedWspShellState | null;
  handleUnauthorized: () => void;
  handleLogout: () => void;
  restorePersistedState: (mode: WspMode) => PersistedWspShellState | null;
};

export function useWspShellState(): WspShellState {
  const [mode, setMode] = useState<WspMode>("client");
  const [storageActorKey, setStorageActorKey] = useState("session");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isReady, setIsReady] = useState(false);
  const [locale, setLocale] = useState<LoginLocale>("en");
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [currentUsr, setCurrentUsr] = useState<CurrentUsr | null>(null);
  const [initialPersistedState, setInitialPersistedState] =
    useState<PersistedWspShellState | null>(null);

  const handleUnauthorized = useCallback(() => {
    setAccessToken(null);
    setCurrentUsr(null);
    redirectWspToLogin();
  }, []);

  const handleLogout = useCallback(() => {
    clearWspAuthStorage();
    setAccessToken(null);
    setCurrentUsr(null);
    window.location.href = "/login";
  }, []);

  const restorePersistedState = useCallback(
    (nextMode: WspMode) => readPersistedWspState(nextMode, storageActorKey),
    [storageActorKey]
  );

  useEffect(() => {
    const savedLocale = window.localStorage.getItem(LOGIN_LOCALE_STORAGE_KEY);
    const rawToken = window.localStorage.getItem("sinwoo.accessToken");
    const currentUsr = parseWspCurrentUsr(window.localStorage.getItem("sinwoo.currentUsr"));
    const token =
      !hasWspCurrentUsrSession() || isWspJwtExpired(rawToken) ? null : rawToken;
    const initialLocale = isSupportedLoginLocale(savedLocale)
      ? savedLocale
      : detectBrowserLoginLocale();
    const actorKey = buildWspStorageActorKey(currentUsr);
    const clientState = readPersistedWspState("client", actorKey);
    const adminState = readPersistedWspState("admin", actorKey);
    // 마지막 활성 모드 = 두 상태 중 lastActiveAt 큰 쪽. 둘 다 없으면 "client" (고객 대시보드 기본).
    const clientStamp = clientState?.lastActiveAt ?? 0;
    const adminStamp = adminState?.lastActiveAt ?? 0;
    let preferredMode: WspMode;
    if (!clientState && !adminState) {
      preferredMode = "client";
    } else if (adminStamp > clientStamp) {
      preferredMode = "admin";
    } else {
      preferredMode = clientState ? "client" : "admin";
    }
    const persistedState = preferredMode === "admin" ? adminState : clientState;

    if (!token) {
      redirectWspToLogin();
      return;
    }

    setLocale(initialLocale);
    setAccessToken(token);
    setCurrentUsr(currentUsr);
    setStorageActorKey(actorKey);
    setMode(preferredMode);
    setSidebarCollapsed(persistedState?.sidebarCollapsed ?? false);
    setInitialPersistedState(persistedState);
    setIsReady(true);
  }, []);

  useEffect(() => {
    if (!isReady) return;
    window.localStorage.setItem(LOGIN_LOCALE_STORAGE_KEY, locale);
  }, [isReady, locale]);

  return {
    isReady,
    locale,
    setLocale,
    accessToken,
    setAccessToken,
    mode,
    setMode,
    sidebarCollapsed,
    setSidebarCollapsed,
    loading,
    setLoading,
    storageActorKey,
    currentUsr,
    initialPersistedState,
    handleUnauthorized,
    handleLogout,
    restorePersistedState,
  };
}

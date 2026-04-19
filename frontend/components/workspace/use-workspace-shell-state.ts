"use client";

import type { Dispatch, SetStateAction } from "react";
import { useCallback, useEffect, useState } from "react";

import type { CurrentUser } from "@/lib/api/auth-contract";
import {
  detectBrowserLoginLocale,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-content";
import type { WorkspaceMode } from "@/lib/workspace/platform-shell-data";
import {
  buildWorkspaceStorageActorKey,
  readPersistedWorkspaceState,
  type PersistedWorkspaceShellState,
} from "@/lib/workspace/workspace-storage";
import {
  clearWorkspaceAuthStorage,
  hasWorkspaceCurrentUserSession,
  isWorkspaceJwtExpired,
  parseWorkspaceCurrentUser,
  redirectWorkspaceToLogin,
} from "@/lib/workspace/workspace-session";

type WorkspaceShellState = {
  isReady: boolean;
  locale: LoginLocale;
  setLocale: Dispatch<SetStateAction<LoginLocale>>;
  accessToken: string | null;
  setAccessToken: Dispatch<SetStateAction<string | null>>;
  mode: WorkspaceMode;
  setMode: Dispatch<SetStateAction<WorkspaceMode>>;
  sidebarCollapsed: boolean;
  setSidebarCollapsed: Dispatch<SetStateAction<boolean>>;
  loading: boolean;
  setLoading: Dispatch<SetStateAction<boolean>>;
  storageActorKey: string;
  currentUser: CurrentUser | null;
  initialPersistedState: PersistedWorkspaceShellState | null;
  handleUnauthorized: () => void;
  handleLogout: () => void;
  restorePersistedState: (mode: WorkspaceMode) => PersistedWorkspaceShellState | null;
};

export function useWorkspaceShellState(): WorkspaceShellState {
  const [mode, setMode] = useState<WorkspaceMode>("client");
  const [storageActorKey, setStorageActorKey] = useState("session");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isReady, setIsReady] = useState(false);
  const [locale, setLocale] = useState<LoginLocale>("en");
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [initialPersistedState, setInitialPersistedState] =
    useState<PersistedWorkspaceShellState | null>(null);

  const handleUnauthorized = useCallback(() => {
    setAccessToken(null);
    setCurrentUser(null);
    redirectWorkspaceToLogin();
  }, []);

  const handleLogout = useCallback(() => {
    clearWorkspaceAuthStorage();
    setAccessToken(null);
    setCurrentUser(null);
    window.location.href = "/login";
  }, []);

  const restorePersistedState = useCallback(
    (nextMode: WorkspaceMode) => readPersistedWorkspaceState(nextMode, storageActorKey),
    [storageActorKey]
  );

  useEffect(() => {
    const savedLocale = window.localStorage.getItem(LOGIN_LOCALE_STORAGE_KEY);
    const rawToken = window.localStorage.getItem("sinwoo.accessToken");
    const currentUser = parseWorkspaceCurrentUser(window.localStorage.getItem("sinwoo.currentUser"));
    const token =
      !hasWorkspaceCurrentUserSession() || isWorkspaceJwtExpired(rawToken) ? null : rawToken;
    const initialLocale = isSupportedLoginLocale(savedLocale)
      ? savedLocale
      : detectBrowserLoginLocale();
    const actorKey = buildWorkspaceStorageActorKey(currentUser);
    const clientState = readPersistedWorkspaceState("client", actorKey);
    const adminState = readPersistedWorkspaceState("admin", actorKey);
    const preferredMode = adminState?.mode ?? clientState?.mode ?? "client";
    const persistedState = preferredMode === "admin" ? adminState : clientState;

    if (!token) {
      redirectWorkspaceToLogin();
      return;
    }

    setLocale(initialLocale);
    setAccessToken(token);
    setCurrentUser(currentUser);
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
    currentUser,
    initialPersistedState,
    handleUnauthorized,
    handleLogout,
    restorePersistedState,
  };
}

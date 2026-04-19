"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import type { MenuTreeRes } from "@/lib/api/menu-contract";
import type { LoginLocale } from "@/lib/i18n/login-content";
import {
  localizeMenuNodesWithFallbackTitles,
  type MenuNode,
  type WorkspaceMode,
} from "@/lib/workspace/platform-shell-data";
import {
  normalizeWorkspaceApiMenus,
  projectClientRuntimeMenus,
} from "@/lib/workspace/workspace-menu-utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type UseWorkspaceMenusParams = {
  accessToken: string | null;
  fallbackMenus: MenuNode[];
  isReady: boolean;
  locale: LoginLocale;
  mode: WorkspaceMode;
  onUnauthorized: () => void;
  roleCds?: string[];
};

export function useWorkspaceMenus({
  accessToken,
  fallbackMenus,
  isReady,
  locale,
  mode,
  onUnauthorized,
  roleCds = [],
}: UseWorkspaceMenusParams) {
  const previousModeRef = useRef<WorkspaceMode>("client");
  const [resolvedMenus, setResolvedMenus] = useState<MenuNode[]>([]);

  useEffect(() => {
    if (!isReady || !resolvedMenus.length) return;

    setResolvedMenus((current) => {
      if (!current.length) {
        return current;
      }

      return localizeMenuNodesWithFallbackTitles(current, locale);
    });
  }, [isReady, locale, resolvedMenus.length]);

  useEffect(() => {
    if (!isReady) return;

    const scope = mode === "admin" ? "ADMIN" : "CUSTOMER";
    const modeChanged = previousModeRef.current !== mode;
    let aborted = false;

    previousModeRef.current = mode;

    if (!accessToken) {
      onUnauthorized();
      return;
    }

    if (modeChanged) {
      setResolvedMenus([]);
    }

    const menuParams = new URLSearchParams({
      mnuScopeCd: scope,
      lang: locale,
    });

    fetch(`${API_BASE_URL}/api/v1/menus/my?${menuParams.toString()}`, {
      method: "GET",
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      cache: "no-store",
    })
      .then(async (response) => {
        if (!response.ok) {
          if (response.status === 401) {
            onUnauthorized();
            return null;
          }
          throw new Error(`Menu fetch failed: ${response.status}`);
        }

        return response.json() as Promise<MenuTreeRes>;
      })
      .then((payload) => {
        if (aborted || !payload) return;
        const normalizedMenus = payload.itemList?.length
          ? normalizeWorkspaceApiMenus(payload.itemList)
          : [];
        const nextMenus =
          mode === "client"
            ? projectClientRuntimeMenus(normalizedMenus, locale, roleCds)
            : normalizedMenus;
        setResolvedMenus(nextMenus);
      })
      .catch(() => {
        if (!aborted) {
          setResolvedMenus([]);
        }
      });

    return () => {
      aborted = true;
    };
  }, [accessToken, isReady, locale, mode, onUnauthorized, roleCds]);

  const currentMenus = useMemo(
    () => (resolvedMenus.length ? resolvedMenus : fallbackMenus),
    [fallbackMenus, resolvedMenus]
  );

  return {
    resolvedMenus,
    currentMenus,
  };
}

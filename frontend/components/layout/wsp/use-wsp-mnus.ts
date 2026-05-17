"use client";

import { useEffect, useMemo, useRef, useState } from "react";

import type { MnuTreeResponse } from "@/lib/api/mnu-contract";
import type { LoginLocale } from "@/lib/i18n/login-cnt";
import {
  localizeMnuNodesWithFallbackTitles,
  type MnuNode,
  type WspMode,
} from "@/lib/utils/wsp/platform-shell-data";
import {
  normalizeWspApiMnus,
  projectClientRuntimeMnus,
} from "@/lib/utils/wsp/wsp-mnu-utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type UseWspMnusParams = {
  accessToken: string | null;
  fallbackMnus: MnuNode[];
  isReady: boolean;
  locale: LoginLocale;
  mode: WspMode;
  onUnauthorized: () => void;
  roleCds?: string[];
};

export function useWspMnus({
  accessToken,
  fallbackMnus,
  isReady,
  locale,
  mode,
  onUnauthorized,
  roleCds = [],
}: UseWspMnusParams) {
  const previousModeRef = useRef<WspMode>("client");
  const [resolvedMnus, setResolvedMnus] = useState<MnuNode[]>([]);

  useEffect(() => {
    if (!isReady || !resolvedMnus.length) return;

    setResolvedMnus((current) => {
      if (!current.length) {
        return current;
      }

      return localizeMnuNodesWithFallbackTitles(current, locale);
    });
  }, [isReady, locale, resolvedMnus.length]);

  useEffect(() => {
    if (!isReady) return;

    const scope = mode === "admin" ? "ADM" : "CST";
    const modeChanged = previousModeRef.current !== mode;
    let aborted = false;

    previousModeRef.current = mode;

    if (!accessToken) {
      onUnauthorized();
      return;
    }

    if (modeChanged) {
      setResolvedMnus([]);
    }

    const mnuParams = new URLSearchParams({
      mnuScopeCd: scope,
      lang: locale,
    });

    fetch(`${API_BASE_URL}/api/v1/menus/my?${mnuParams.toString()}`, {
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
          throw new Error(`Mnu fetch failed: ${response.status}`);
        }

        return response.json() as Promise<MnuTreeResponse>;
      })
      .then((payload) => {
        if (aborted || !payload) return;
        const normalizedMnus = payload.itemList?.length
          ? normalizeWspApiMnus(payload.itemList)
          : [];
        const nextMnus =
          mode === "client"
            ? projectClientRuntimeMnus(normalizedMnus, locale, roleCds)
            : normalizedMnus;
        setResolvedMnus(nextMnus);
      })
      .catch(() => {
        if (!aborted) {
          setResolvedMnus([]);
        }
      });

    return () => {
      aborted = true;
    };
  }, [accessToken, isReady, locale, mode, onUnauthorized, roleCds]);

  const currentMnus = useMemo(
    () => (resolvedMnus.length ? resolvedMnus : fallbackMnus),
    [fallbackMnus, resolvedMnus]
  );

  return {
    resolvedMnus,
    currentMnus,
  };
}

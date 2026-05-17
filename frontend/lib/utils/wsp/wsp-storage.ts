import type { CurrentUsr } from "@/lib/api/auth-contract";
import type { TabItem, WspMode } from "@/lib/utils/wsp/platform-shell-data";

export type PersistedWspShellState = {
  mode: WspMode;
  sidebarCollapsed: boolean;
  openTabs: TabItem[];
  activeTabId: string;
  /** 마지막 저장 시각 (epoch ms). 모드 간 마지막 활성 판정용. */
  lastActiveAt?: number;
};

const STORAGE_NAMESPACE = "sinwoo.wsp.shell.v1";
const LEGACY_DEMO_STORAGE_ACTOR_KEY = "ggamgang@sinwoo-itc.com";

export function buildWspStorageKey(actorKey: string, mode: WspMode) {
  return `${STORAGE_NAMESPACE}:${actorKey}:${mode}`;
}

export function buildWspStorageActorKey(
  user: Pick<CurrentUsr, "usrId" | "eml" | "lgnId"> | null
) {
  if (!user) return "session";
  if (user.lgnId) return `lgn:${user.lgnId.toLowerCase()}`;
  if (user.eml) return `eml:${user.eml.toLowerCase()}`;
  if (typeof user.usrId === "number") return `usr:USR${String(user.usrId).padStart(4, "0")}`;
  return "session";
}

export function readPersistedWspState(
  mode: WspMode,
  actorKey: string
): PersistedWspShellState | null {
  const candidates = [
    buildWspStorageKey(actorKey, mode),
    buildWspStorageKey(LEGACY_DEMO_STORAGE_ACTOR_KEY, mode),
  ];

  for (const key of candidates) {
    const raw = window.localStorage.getItem(key);
    if (!raw) continue;

    try {
      return JSON.parse(raw) as PersistedWspShellState;
    } catch {
      continue;
    }
  }

  return null;
}

export function writePersistedWspState(
  actorKey: string,
  mode: WspMode,
  state: PersistedWspShellState
) {
  const stamped: PersistedWspShellState = { ...state, lastActiveAt: Date.now() };
  window.localStorage.setItem(buildWspStorageKey(actorKey, mode), JSON.stringify(stamped));
}

/**
 * 로그인 직후 호출 — 이전에 저장된 모든 wsp shell 상태(모드/탭/사이드바)를 제거.
 * 그 결과 첫 진입 시 useWspShellState가 fallback "client" 모드로 시작.
 */
export function clearAllPersistedWspState() {
  const keysToRemove: string[] = [];
  for (let i = 0; i < window.localStorage.length; i++) {
    const key = window.localStorage.key(i);
    if (key && key.startsWith(`${STORAGE_NAMESPACE}:`)) {
      keysToRemove.push(key);
    }
  }
  keysToRemove.forEach((k) => window.localStorage.removeItem(k));
}

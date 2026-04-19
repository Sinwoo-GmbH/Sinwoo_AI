import type { CurrentUser } from "@/lib/api/auth-contract";
import type { TabItem, WorkspaceMode } from "@/lib/workspace/platform-shell-data";

export type PersistedWorkspaceShellState = {
  mode: WorkspaceMode;
  sidebarCollapsed: boolean;
  openTabs: TabItem[];
  activeTabId: string;
};

const STORAGE_NAMESPACE = "sinwoo.workspace.shell.v1";
const LEGACY_DEMO_STORAGE_ACTOR_KEY = "ggamgang@sinwoo-itc.com";

export function buildWorkspaceStorageKey(actorKey: string, mode: WorkspaceMode) {
  return `${STORAGE_NAMESPACE}:${actorKey}:${mode}`;
}

export function buildWorkspaceStorageActorKey(
  user: Pick<CurrentUser, "usrId" | "eml" | "lgnId"> | null
) {
  if (!user) return "session";
  if (typeof user.usrId === "number") return `usr:${user.usrId}`;
  if (user.eml) return `eml:${user.eml.toLowerCase()}`;
  if (user.lgnId) return `lgn:${user.lgnId.toLowerCase()}`;
  return "session";
}

export function readPersistedWorkspaceState(
  mode: WorkspaceMode,
  actorKey: string
): PersistedWorkspaceShellState | null {
  const candidates = [
    buildWorkspaceStorageKey(actorKey, mode),
    buildWorkspaceStorageKey(LEGACY_DEMO_STORAGE_ACTOR_KEY, mode),
  ];

  for (const key of candidates) {
    const raw = window.localStorage.getItem(key);
    if (!raw) continue;

    try {
      return JSON.parse(raw) as PersistedWorkspaceShellState;
    } catch {
      continue;
    }
  }

  return null;
}

export function writePersistedWorkspaceState(
  actorKey: string,
  mode: WorkspaceMode,
  state: PersistedWorkspaceShellState
) {
  window.localStorage.setItem(buildWorkspaceStorageKey(actorKey, mode), JSON.stringify(state));
}

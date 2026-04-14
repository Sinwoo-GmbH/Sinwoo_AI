"use client";

import Image from "next/image";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  BriefcaseBusiness,
  Building2,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CreditCard,
  Grid2X2,
  KeyRound,
  LogOut,
  Menu,
  ShieldCheck,
  UserCircle2,
  Users2,
  Wallet,
  Workflow,
  X,
  Zap,
} from "lucide-react";

import { LocaleCombobox } from "@/components/common/locale-combobox";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { WorkspaceAttendanceCard } from "@/components/workspace/workspace-attendance-card";
import { WorkspaceContentContainer } from "@/components/workspace/workspace-content-container";
import { WorkspacePageHeader } from "@/components/workspace/workspace-page-header";
import { WorkspaceSectionPanel } from "@/components/workspace/workspace-section-panel";
import type { MenuNodeRes, MenuTreeRes } from "@/lib/api/menu-contract";
import {
  detectBrowserLoginLocale,
  getLoginMessages,
  isSupportedLoginLocale,
  LOGIN_LOCALE_STORAGE_KEY,
  type LoginLocale,
} from "@/lib/i18n/login-content";
import {
  getWorkspaceShellMessages,
  getWorkspaceTabContextMenuLabels,
} from "@/lib/i18n/workspace-content";
import { cn } from "@/lib/utils";
import {
  findMenuTitle,
  getFallbackMenuPresentation,
  getLocalizedViewModel,
  getWorkspaceModeConfig,
  localizeMenuNodesWithFallbackTitles,
  type MenuNode,
  type TabItem,
  type WorkspaceMode,
} from "@/lib/workspace/platform-shell-data";

type PersistedState = {
  mode: WorkspaceMode;
  sidebarCollapsed: boolean;
  openTabs: TabItem[];
  activeTabId: string;
};

type TabContextMenuState = {
  tabId: string;
} | null;

const STORAGE_NAMESPACE = "sinwoo.workspace.shell.v1";
const DEMO_USER_KEY = "ggamgang@sinwoo-itc.com";
const PROFILE_TAB_ID = "my-profile";
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const TAB_CONTEXT_MENU_WIDTH = 164;
// Compatibility-only mapping:
// this exists to reopen old persisted tabs safely while the UI moves to DB menu ids.
const MENU_ID_COMPAT_ALIASES: Record<string, string> = {
  "admin-overview": "MNU_ADMIN_DASH",
  "client-dashboard": "MNU_CUSTOMER_DASH",
  "tenant-control": "MNU_ADMIN_TENANT",
  "tenant-list": "MNU_ADMIN_TENANT_LIST",
  "tenant-settings": "MNU_ADMIN_TENANT_SETTINGS",
  "company-profile": "MNU_ADMIN_COMPANY_PROFILE",
  "workspace-policy": "MNU_ADMIN_WORKSPACE_POLICY",
  "menu-policy": "MNU_ADMIN_MENU_POLICY",
  authorization: "MNU_ADMIN_AUTH",
  "role-policy": "MNU_ADMIN_ROLE_POLICY",
  "menu-management": "MNU_ADMIN_MENU",
  "menu-tree": "MNU_ADMIN_MENU_TREE",
  "tab-policy": "MNU_ADMIN_TAB_POLICY",
  "depth-policy": "MNU_ADMIN_DEPTH_POLICY",
  "depth-editor": "MNU_ADMIN_DEPTH_EDITOR",
  "billing-ops": "MNU_ADMIN_BILL",
  "plan-catalog": "MNU_ADMIN_PLAN_CATALOG",
  "payment-gates": "MNU_ADMIN_PAYMENT_GATES",
  "upgrade-queue": "MNU_ADMIN_UPGRADE_QUEUE",
  "audit-center": "MNU_ADMIN_AUDIT",
  "change-history": "MNU_ADMIN_CHANGE_HISTORY",
  "access-logs": "MNU_ADMIN_ACCESS_LOGS",
  compliance: "MNU_ADMIN_COMPLIANCE",
  "billing-center": "MNU_CUSTOMER_PAY",
  workspace: "MNU_CUSTOMER_WORKSPACE",
  documents: "MNU_CUSTOMER_DOC",
  "ocr-inbox": "MNU_CUSTOMER_OCR_INBOX",
  "expense-review": "MNU_CUSTOMER_EXPENSE_REVIEW",
  archive: "MNU_CUSTOMER_ARCHIVE",
  attendance: "MNU_CUSTOMER_ATTENDANCE",
  "my-time": "MNU_CUSTOMER_MY_TIME",
  "team-time": "MNU_CUSTOMER_TEAM_TIME",
  leave: "MNU_CUSTOMER_LEAVE",
  people: "MNU_CUSTOMER_PEOPLE",
  employees: "MNU_CUSTOMER_EMPLOYEES",
  organization: "MNU_CUSTOMER_ORG",
  departments: "MNU_CUSTOMER_DEPARTMENTS",
  roles: "MNU_CUSTOMER_ROLES",
  subscription: "MNU_CUSTOMER_SUBSCRIPTION",
  payments: "MNU_CUSTOMER_PAYMENTS",
};
const REVERSE_MENU_ID_COMPAT_ALIASES = Object.fromEntries(
  Object.entries(MENU_ID_COMPAT_ALIASES).map(([legacyId, dbId]) => [dbId, legacyId])
) as Record<string, string>;

const iconMap = {
  grid: Grid2X2,
  briefcase: BriefcaseBusiness,
  users: Users2,
  "credit-card": CreditCard,
  shield: ShieldCheck,
  building: Building2,
  key: KeyRound,
  wallet: Wallet,
  activity: Zap,
};

const uiMessages = {
  en: {
    eyebrow: "OneGate workspace shell",
    client: "Client",
    admin: "Admin",
    navigation: "Navigation",
    navShort: "Nav",
    logout: "Logout",
    profileTab: "My Profile",
    badgeClient: "Customer workspace view",
    badgeAdmin: "Super admin view",
    localState: "Local state",
    quickActions: "Quick actions",
    rulesTitle: "Platform shell rules",
    ruleOne: "Admins can start in client mode and switch to admin mode when needed.",
    ruleTwo: "Menus support four depths and are designed for admin-side configuration.",
    ruleThree: "Desktop uses tabs. Mobile web simplifies the flow into one active view.",
    modeLabel: "Mode",
    sidebarLabel: "Sidebar",
    tabsLabel: "Open tabs",
    modeAdminValue: "Admin control",
    modeClientValue: "Client portal",
    collapsed: "Collapsed",
    expanded: "Expanded",
    focusTitle: "Priority flow",
    focusDescription: "Use these cards for queues, alerts, and upgrade prompts.",
    tableDescription: "Desktop web should feel like a real browser workspace, not a mobile card stack.",
    name: "Name",
    owner: "Owner",
    status: "Status",
    updated: "Updated",
    mobileWeb: "Mobile web",
    mobileFocusTitle: "Mobile focus",
    mobileFocusDescription: "On mobile web, tabs are removed and the current menu becomes the main view.",
    tabContextClose: "Close",
    tabContextCloseOther: "Close other tab",
    tabContextCloseAll: "Close all tab",
  },
  de: {
    eyebrow: "OneGate workspace shell",
    client: "Client",
    admin: "Admin",
    navigation: "Navigation",
    navShort: "Nav",
    logout: "Abmelden",
    profileTab: "Persönliche Daten",
    badgeClient: "Kundenansicht",
    badgeAdmin: "Super-Admin-Ansicht",
    localState: "Lokaler Zustand",
    quickActions: "Schnellaktionen",
    rulesTitle: "Plattformregeln",
    ruleOne: "Administratoren können im Kundenmodus starten und danach in den Admin-Modus wechseln.",
    ruleTwo: "Menüs unterstützen bis zu vier Ebenen und werden über die Admin-Seite verwaltet.",
    ruleThree: "Desktop verwendet Tabs. Mobile Web reduziert die Oberfläche auf eine aktive Ansicht.",
    modeLabel: "Modus",
    sidebarLabel: "Sidebar",
    tabsLabel: "Offene Tabs",
    modeAdminValue: "Admin-Steuerung",
    modeClientValue: "Kundenportal",
    collapsed: "Eingeklappt",
    expanded: "Erweitert",
    focusTitle: "Prioritätsfluss",
    focusDescription: "Diese Karten zeigen Warteschlangen, Warnungen und Upgrade-Auslöser.",
    tableDescription: "Desktop-Web soll wie ein ernsthafter Arbeitsbereich wirken, nicht wie ein mobiler Kartenstapel.",
    name: "Name",
    owner: "Verantwortlich",
    status: "Status",
    updated: "Aktualisiert",
    mobileWeb: "Mobile Web",
    mobileFocusTitle: "Mobile Fokusansicht",
    mobileFocusDescription: "Im Mobile Web werden Tabs entfernt. Die aktive Auswahl bleibt die einzige Arbeitsansicht.",
    tabContextClose: "Schliessen",
    tabContextCloseOther: "Andere Tabs schliessen",
    tabContextCloseAll: "Alle Tabs schliessen",
  },
  ko: {
    eyebrow: "OneGate workspace shell",
    client: "고객",
    admin: "관리자",
    navigation: "메뉴",
    navShort: "메뉴",
    logout: "로그아웃",
    profileTab: "개인정보변경",
    badgeClient: "고객 워크스페이스 뷰",
    badgeAdmin: "슈퍼관리자 뷰",
    localState: "로컬 상태",
    quickActions: "빠른 작업",
    rulesTitle: "플랫폼 셸 규칙",
    ruleOne: "관리자는 먼저 고객 모드로 진입한 뒤 필요 시 관리자 모드로 전환합니다.",
    ruleTwo: "메뉴는 최대 4뎁스를 지원하며 관리자 페이지에서 관리되는 구조를 전제로 합니다.",
    ruleThree: "데스크톱은 탭을 사용하고, 모바일 웹은 하나의 활성 화면 중심으로 단순화합니다.",
    modeLabel: "모드",
    sidebarLabel: "사이드바",
    tabsLabel: "열린 탭",
    modeAdminValue: "관리자 제어",
    modeClientValue: "고객 포털",
    collapsed: "접힘",
    expanded: "펼침",
    focusTitle: "우선 처리 흐름",
    focusDescription: "큐, 경고, 업그레이드 유도 항목을 이 카드 영역에 배치합니다.",
    tableDescription: "데스크톱 웹은 모바일 카드 화면이 아니라 진짜 업무용 브라우저 워크스페이스처럼 보여야 합니다.",
    name: "이름",
    owner: "담당",
    status: "상태",
    updated: "갱신",
    mobileWeb: "모바일 웹",
    mobileFocusTitle: "모바일 집중 화면",
    mobileFocusDescription: "모바일 웹에서는 탭을 제거하고 현재 선택한 화면 하나에 집중합니다.",
  },
} satisfies Record<LoginLocale, Record<string, string>>;

function simulateLoading(setLoading: (value: boolean) => void) {
  setLoading(true);
  window.setTimeout(() => setLoading(false), 280);
}

const tabContextMenuLabels = {
  en: {
    close: "Close",
    closeOther: "Close other tab",
    closeAll: "Close all tab",
  },
  de: {
    close: "Schliessen",
    closeOther: "Andere Tabs schliessen",
    closeAll: "Alle Tabs schliessen",
  },
  ko: {
    close: "\uB2EB\uAE30",
    closeOther: "\uB2E4\uB978 \uD0ED \uB2EB\uAE30",
    closeAll: "\uBAA8\uB4E0 \uD0ED \uB2EB\uAE30",
  },
} satisfies Record<LoginLocale, { close: string; closeOther: string; closeAll: string }>;

function buildStorageKey(mode: WorkspaceMode) {
  return `${STORAGE_NAMESPACE}:${DEMO_USER_KEY}:${mode}`;
}

function clearAuthStorage() {
  window.localStorage.removeItem("sinwoo.accessToken");
  window.localStorage.removeItem("sinwoo.refreshToken");
  window.localStorage.removeItem("sinwoo.currentUser");
}

function redirectToLogin() {
  clearAuthStorage();
  window.location.replace("/login");
}

function hasCurrentUserSession() {
  const raw = window.localStorage.getItem("sinwoo.currentUser");
  if (!raw) return false;

  try {
    const parsed = JSON.parse(raw) as { usrId?: string; email?: string; roleCds?: string[] };
    return Boolean(parsed.usrId || parsed.email || parsed.roleCds?.length);
  } catch {
    return false;
  }
}

function isJwtExpired(token: string | null) {
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

function cloneTabs(tabs: TabItem[]) {
  return tabs.map((tab) => ({ ...tab }));
}

function extractMenusForMobile(menus: MenuNode[], ids: string[]) {
  const bucket = new Map<string, MenuNode>();
  const walk = (items: MenuNode[]) => {
    for (const item of items) {
      if (ids.includes(item.id)) bucket.set(item.id, item);
      if (item.children) walk(item.children);
    }
  };
  walk(menus);
  return ids.map((id) => bucket.get(id)).filter(Boolean) as MenuNode[];
}

function findMenuTitleFromNodes(menus: MenuNode[], targetId: string): string | null {
  for (const menu of menus) {
    if (menu.id === targetId) {
      return menu.title;
    }

    if (menu.children?.length) {
      const hit = findMenuTitleFromNodes(menu.children, targetId);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

function findMenuTrailIds(
  menus: MenuNode[],
  targetId: string,
  trail: string[] = []
): string[] | null {
  for (const menu of menus) {
    const nextTrail = [...trail, menu.id];

    if (menu.id === targetId) {
      return nextTrail;
    }

    if (menu.children?.length) {
      const hit = findMenuTrailIds(menu.children, targetId, nextTrail);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

function menuExists(menus: MenuNode[], targetId: string) {
  return Boolean(findMenuTitleFromNodes(menus, targetId));
}

function canonicalizeMenuId(menus: MenuNode[], targetId: string) {
  if (menuExists(menus, targetId)) {
    return targetId;
  }

  const alias = MENU_ID_COMPAT_ALIASES[targetId];
  if (alias && menuExists(menus, alias)) {
    return alias;
  }

  return targetId;
}

function resolveFallbackMenuId(targetId: string) {
  return REVERSE_MENU_ID_COMPAT_ALIASES[targetId] ?? targetId;
}

// Developer note:
// API menu tree is primary. Frontend fallback only supplies presentation metadata
// when backend icon data is absent, and only as a safe boot fallback.
function normalizeApiMenus(items: MenuNodeRes[]): MenuNode[] {
  const walk = (nodes: MenuNodeRes[]): MenuNode[] =>
    nodes.map((node) => {
      const fallbackPresentation = getFallbackMenuPresentation(node.mnuCd);
      return {
        id: node.mnuCd,
        title: node.mnuNm,
        icon: node.iconNm ?? fallbackPresentation?.icon,
        closable: fallbackPresentation?.closable,
        children: node.childList?.length ? walk(node.childList) : undefined,
      };
    });

  return walk(items);
}

export function PlatformShell() {
  const tabScrollRef = useRef<HTMLDivElement | null>(null);
  const previousModeRef = useRef<WorkspaceMode>("client");
  const [mode, setMode] = useState<WorkspaceMode>("client");
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [openTabs, setOpenTabs] = useState<TabItem[]>([]);
  const [activeTabId, setActiveTabId] = useState("");
  const [expandedMenuIds, setExpandedMenuIds] = useState<string[]>([]);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isReady, setIsReady] = useState(false);
  const [locale, setLocale] = useState<LoginLocale>("en");
  const [resolvedMenus, setResolvedMenus] = useState<MenuNode[]>([]);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [canScrollTabsLeft, setCanScrollTabsLeft] = useState(false);
  const [canScrollTabsRight, setCanScrollTabsRight] = useState(false);
  const [showTabScrollControls, setShowTabScrollControls] = useState(false);
  const [tabContextMenu, setTabContextMenu] = useState<TabContextMenuState>(null);
  const [headerHeight, setHeaderHeight] = useState(56);

  const ui = getWorkspaceShellMessages(locale);
  const config = useMemo(() => getWorkspaceModeConfig(mode, locale), [mode, locale]);
  const fallbackMenus = config.menus;
  const currentMenus = resolvedMenus.length ? resolvedMenus : fallbackMenus;
  const activeView = useMemo(() => getLocalizedViewModel(activeTabId || config.defaultTabId, locale), [activeTabId, config.defaultTabId, locale]);
  const mobileMenus = useMemo(() => extractMenusForMobile(currentMenus, config.mobileQuickMenus), [currentMenus, config.mobileQuickMenus]);
  const localeMessages = useMemo(() => getLoginMessages(locale), [locale]);
  const tabContextUi = useMemo(() => getWorkspaceTabContextMenuLabels(locale), [locale]);

  const getTabTitle = useCallback(
    (tabId: string, fallbackTitle?: string) => {
      if (tabId === PROFILE_TAB_ID) return ui.profileTab;

      const resolvedTitle = findMenuTitleFromNodes(currentMenus, tabId);
      if (resolvedTitle) {
        return resolvedTitle;
      }

      const fallbackMenuId = resolveFallbackMenuId(tabId);
      const fallbackMenuTitle = findMenuTitle(mode, fallbackMenuId, locale);
      if (fallbackMenuTitle !== fallbackMenuId) {
        return fallbackMenuTitle;
      }

      return fallbackTitle ?? tabId;
    },
    [currentMenus, locale, mode, ui.profileTab]
  );

  const getTabContextMenuStyle = () => {
    if (typeof window === "undefined" || !tabContextMenu) return undefined;

    const tabbarElement = document.getElementById("workspace-tabbar");
    const tabElement = document.getElementById(`workspace-tab-${tabContextMenu.tabId}`);
    if (!tabbarElement || !tabElement) return undefined;

    const tabbarRect = tabbarElement.getBoundingClientRect();
    const tabRect = tabElement.getBoundingClientRect();
    const left = Math.min(
      Math.max(tabRect.left - tabbarRect.left + tabRect.width / 2 - TAB_CONTEXT_MENU_WIDTH / 2, 8),
      tabbarRect.width - TAB_CONTEXT_MENU_WIDTH - 8
    );

    return {
      left,
      top: tabRect.bottom - tabbarRect.top + 6,
    };
  };

  useEffect(() => {
    const savedLocale = window.localStorage.getItem(LOGIN_LOCALE_STORAGE_KEY);
    const rawToken = window.localStorage.getItem("sinwoo.accessToken");
    const token = !hasCurrentUserSession() || isJwtExpired(rawToken) ? null : rawToken;
    const initialLocale = isSupportedLoginLocale(savedLocale) ? savedLocale : detectBrowserLoginLocale();
    const clientStateRaw = window.localStorage.getItem(buildStorageKey("client"));
    const adminStateRaw = window.localStorage.getItem(buildStorageKey("admin"));
    const clientState = clientStateRaw ? (JSON.parse(clientStateRaw) as PersistedState) : null;
    const adminState = adminStateRaw ? (JSON.parse(adminStateRaw) as PersistedState) : null;
    const preferredMode = adminState?.mode ?? clientState?.mode ?? "client";
    const state = preferredMode === "admin" ? adminState : clientState;
    const baseConfig = getWorkspaceModeConfig(preferredMode, initialLocale);

    if (!token) {
      redirectToLogin();
      return;
    }

    setLocale(initialLocale);
    setAccessToken(token);
    setMode(preferredMode);
    setSidebarCollapsed(state?.sidebarCollapsed ?? false);
    setOpenTabs(state?.openTabs?.length ? cloneTabs(state.openTabs) : [{ id: baseConfig.defaultTabId, title: findMenuTitle(preferredMode, baseConfig.defaultTabId, initialLocale) }]);
    setActiveTabId(state?.activeTabId ?? baseConfig.defaultTabId);
    setExpandedMenuIds([baseConfig.menus[1]?.id, baseConfig.menus[1]?.children?.[0]?.id].filter(Boolean) as string[]);
    setResolvedMenus([]);
    setIsReady(true);
  }, []);

  useEffect(() => {
    if (!isReady) return;
    window.localStorage.setItem(LOGIN_LOCALE_STORAGE_KEY, locale);
  }, [isReady, locale]);

  useEffect(() => {
    if (!isReady) return;
    window.localStorage.setItem(buildStorageKey(mode), JSON.stringify({ mode, sidebarCollapsed, openTabs, activeTabId }));
  }, [activeTabId, isReady, mode, openTabs, sidebarCollapsed]);

  useEffect(() => {
    if (!isReady) return;
    if (!openTabs.some((tab) => tab.id === activeTabId)) {
      setActiveTabId(openTabs[0]?.id ?? config.defaultTabId);
    }
  }, [activeTabId, config.defaultTabId, isReady, openTabs]);

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
    let aborted = false;
    const modeChanged = previousModeRef.current !== mode;
    previousModeRef.current = mode;

    if (!accessToken) {
      redirectToLogin();
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
            setAccessToken(null);
            redirectToLogin();
            return null;
          }
          throw new Error(`Menu fetch failed: ${response.status}`);
        }

        return response.json() as Promise<MenuTreeRes>;
      })
      .then((payload) => {
        if (aborted || !payload) return;
        const nextMenus = payload.itemList?.length
          ? normalizeApiMenus(payload.itemList)
          : fallbackMenus;
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
  }, [accessToken, config.menus, isReady, locale, mode]);

  useEffect(() => {
    if (!isReady) return;

    setOpenTabs((current) => {
      let changed = false;
      const next = current.map((tab) => {
        const nextTitle = getTabTitle(tab.id, tab.title);

        if (nextTitle !== tab.title) {
          changed = true;
          return { ...tab, title: nextTitle };
        }

        return tab;
      });

      return changed ? next : current;
    });
  }, [getTabTitle, isReady]);

  useEffect(() => {
    if (!isReady || !currentMenus.length) return;

    let nextActiveTabId = activeTabId;

    setOpenTabs((current) => {
      let changed = false;
      const seen = new Set<string>();
      const nextTabs: TabItem[] = [];

      for (const tab of current) {
        const nextId =
          tab.id === PROFILE_TAB_ID ? PROFILE_TAB_ID : canonicalizeMenuId(currentMenus, tab.id);
        const nextTitle = getTabTitle(nextId, tab.title);

        if (nextId !== tab.id || nextTitle !== tab.title) {
          changed = true;
        }

        if (seen.has(nextId)) {
          changed = true;
          continue;
        }

        seen.add(nextId);
        nextTabs.push({ id: nextId, title: nextTitle });
      }

      if (!nextTabs.length) {
        const defaultTabId = canonicalizeMenuId(currentMenus, config.defaultTabId);
        nextTabs.push({
          id: defaultTabId,
          title: getTabTitle(defaultTabId, findMenuTitle(mode, config.defaultTabId, locale)),
        });
        changed = true;
      }

      const canonicalActive =
        nextActiveTabId === PROFILE_TAB_ID
          ? PROFILE_TAB_ID
          : canonicalizeMenuId(currentMenus, nextActiveTabId || config.defaultTabId);

      nextActiveTabId = nextTabs.some((tab) => tab.id === canonicalActive)
        ? canonicalActive
        : nextTabs[0].id;

      return changed ? nextTabs : current;
    });

    if (nextActiveTabId !== activeTabId) {
      setActiveTabId(nextActiveTabId);
    }
  }, [activeTabId, config.defaultTabId, currentMenus, getTabTitle, isReady]);

  useEffect(() => {
    const tabScroller = tabScrollRef.current;
    if (!tabScroller) return;

    const updateScrollState = () => {
      const { scrollLeft, scrollWidth, clientWidth } = tabScroller;
      setShowTabScrollControls(scrollWidth > clientWidth + 4);
      setCanScrollTabsLeft(scrollLeft > 4);
      setCanScrollTabsRight(scrollLeft + clientWidth < scrollWidth - 4);
    };

    updateScrollState();
    tabScroller.addEventListener("scroll", updateScrollState);
    window.addEventListener("resize", updateScrollState);

    return () => {
      tabScroller.removeEventListener("scroll", updateScrollState);
      window.removeEventListener("resize", updateScrollState);
    };
  }, [config.defaultTabId, openTabs.length]);

  useEffect(() => {
    if (!tabContextMenu) return;

    const closeContextMenu = () => setTabContextMenu(null);
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setTabContextMenu(null);
      }
    };

    window.addEventListener("click", closeContextMenu);
    window.addEventListener("contextmenu", closeContextMenu);
    window.addEventListener("keydown", closeOnEscape);

    return () => {
      window.removeEventListener("click", closeContextMenu);
      window.removeEventListener("contextmenu", closeContextMenu);
      window.removeEventListener("keydown", closeOnEscape);
    };
  }, [tabContextMenu]);

  useEffect(() => {
    if (!isReady || !activeTabId) return;

    const trail = findMenuTrailIds(currentMenus, activeTabId);
    if (!trail?.length) return;

    const parentIds = trail.slice(0, -1);
    if (!parentIds.length) return;

    setExpandedMenuIds((current) => {
      const merged = [...new Set([...current, ...parentIds])];

      if (
        merged.length === current.length &&
        merged.every((value, index) => value === current[index])
      ) {
        return current;
      }

      return merged;
    });
  }, [activeTabId, currentMenus, isReady]);

  const openMenuAsTab = (menu: MenuNode) => {
    if (menu.children?.length) {
      setExpandedMenuIds((current) => (current.includes(menu.id) ? current.filter((id) => id !== menu.id) : [...current, menu.id]));
      return;
    }

    setOpenTabs((current) => (current.some((tab) => tab.id === menu.id) ? current : [...current, { id: menu.id, title: menu.title }]));
    setActiveTabId(menu.id);
    setMobileMenuOpen(false);
    simulateLoading(setLoading);
  };

  const openProfileTab = () => {
    setOpenTabs((current) => (current.some((tab) => tab.id === PROFILE_TAB_ID) ? current : [...current, { id: PROFILE_TAB_ID, title: ui.profileTab }]));
    setActiveTabId(PROFILE_TAB_ID);
    simulateLoading(setLoading);
  };

  const closeTab = (tabId: string) => {
    setTabContextMenu(null);
    setOpenTabs((current) => {
      const nextTabs = current.filter((tab) => tab.id !== tabId);
      if (activeTabId === tabId) {
        setActiveTabId(nextTabs[nextTabs.length - 1]?.id ?? config.defaultTabId);
      }
      return nextTabs.length ? nextTabs : [{ id: config.defaultTabId, title: findMenuTitle(mode, config.defaultTabId, locale) }];
    });
  };

  const closeOtherTabs = (tabId: string) => {
    setTabContextMenu(null);
    setOpenTabs((current) => {
      const selectedTab = current.find((tab) => tab.id === tabId);
      const defaultTab =
        current.find((tab) => tab.id === config.defaultTabId) ?? {
          id: config.defaultTabId,
          title: getTabTitle(config.defaultTabId, findMenuTitle(mode, config.defaultTabId, locale)),
        };

      if (!selectedTab || selectedTab.id === defaultTab.id) {
        setActiveTabId(defaultTab.id);
        return [defaultTab];
      }

      setActiveTabId(selectedTab.id);
      return [defaultTab, selectedTab];
    });
    simulateLoading(setLoading);
  };

  const closeAllTabs = () => {
    setTabContextMenu(null);
    const defaultTab = {
      id: config.defaultTabId,
      title: getTabTitle(config.defaultTabId, findMenuTitle(mode, config.defaultTabId, locale)),
    };
    setOpenTabs([defaultTab]);
    setActiveTabId(defaultTab.id);
    simulateLoading(setLoading);
  };

  const openTabContextMenu = (
    event: React.MouseEvent<HTMLElement>,
    tabId: string
  ) => {
    event.preventDefault();
    event.stopPropagation();
    setTabContextMenu({
      tabId,
    });
  };

  const scrollTabs = (direction: "left" | "right") => {
    const tabScroller = tabScrollRef.current;
    if (!tabScroller) return;

    tabScroller.scrollBy({
      left: direction === "left" ? -220 : 220,
      behavior: "smooth",
    });
  };

  const switchMode = (nextMode: WorkspaceMode) => {
    if (nextMode === mode) return;
    const raw = window.localStorage.getItem(buildStorageKey(nextMode));
    const persisted = raw ? (JSON.parse(raw) as PersistedState) : null;
    const nextConfig = getWorkspaceModeConfig(nextMode, locale);
    setMode(nextMode);
    setSidebarCollapsed(persisted?.sidebarCollapsed ?? false);
    setOpenTabs(persisted?.openTabs?.length ? cloneTabs(persisted.openTabs) : [{ id: nextConfig.defaultTabId, title: findMenuTitle(nextMode, nextConfig.defaultTabId, locale) }]);
    setActiveTabId(persisted?.activeTabId ?? nextConfig.defaultTabId);
    setExpandedMenuIds([nextConfig.menus[1]?.id].filter(Boolean) as string[]);
    setMobileMenuOpen(false);
    simulateLoading(setLoading);
  };

  const handleLogout = () => {
    clearAuthStorage();
    setAccessToken(null);
    window.location.href = "/login";
  };

  const renderMenuNode = (menu: MenuNode, depth = 1): React.ReactNode => {
    const isExpanded = expandedMenuIds.includes(menu.id);
    const hasChildren = Boolean(menu.children?.length);
    const Icon = menu.icon ? iconMap[menu.icon as keyof typeof iconMap] ?? Workflow : Workflow;
    const isActiveLeaf = !hasChildren && activeTabId === menu.id;

    return (
      <div id={`workspace-menu-node-${menu.id}`} key={menu.id} className="space-y-1">
        <button
          id={`workspace-menu-button-${menu.id}`}
          type="button"
          onClick={() => openMenuAsTab(menu)}
          className={cn(
            "flex w-full items-center gap-2.5 rounded-xl border border-transparent px-2.5 py-1.5 text-left transition-colors",
            depth === 1 ? "font-semibold" : "font-medium",
            isActiveLeaf
              ? "border-[#233a7a] bg-[#233a7a] text-white"
              : "text-slate-600 hover:border-[#DCE6F7] hover:bg-[#EEF3FB] hover:text-[#18397E]",
            depth === 1 ? "text-[13px]" : "",
            depth === 2 ? "py-1 text-[11px] text-slate-500" : "",
            depth >= 3 ? "py-0.5 text-[10px] text-slate-400" : "",
            sidebarCollapsed && depth === 1 ? "justify-center px-1.5" : ""
          )}
        >
          {depth === 1 ? <Icon className="h-3.5 w-3.5 shrink-0" /> : <span className="block h-1.5 w-1.5 rounded-full bg-slate-300" />}
          {!sidebarCollapsed && (
            <span
              className={cn(
                depth === 1 ? "text-[13px]" : "",
                depth === 2 ? "text-[11px]" : "",
                depth >= 3 ? "text-[10px]" : ""
              )}
            >
              {menu.title}
            </span>
          )}
          {!sidebarCollapsed && hasChildren ? <ChevronDown className={cn("ml-auto h-3.5 w-3.5 transition-transform", isExpanded ? "rotate-180" : "")} /> : null}
        </button>
        {!sidebarCollapsed && hasChildren && isExpanded ? (
          <div
            id={`workspace-menu-children-${menu.id}`}
            className={cn(
              "border-l border-slate-200 pl-2",
              depth === 1 ? "ml-2.5 space-y-0.5" : "ml-1.5 space-y-0.5"
            )}
          >
            {menu.children?.map((child) => renderMenuNode(child, Math.min(depth + 1, 4)))}
          </div>
        ) : null}
      </div>
    );
  };

  const desktopTabs = openTabs.length ? openTabs : [{ id: config.defaultTabId, title: findMenuTitle(mode, config.defaultTabId, locale) }];

  return (
    <main id="workspace-shell" className="workspace-shell min-h-screen bg-[radial-gradient(circle_at_top_left,rgba(35,58,122,0.10),transparent_28%),linear-gradient(180deg,#F8FAFE_0%,#EDF2F8_100%)]">
      <div id="workspace-global-loading-bar" className="fixed left-0 top-0 z-50 h-1 w-full bg-transparent">
        <div className={cn("h-full bg-[linear-gradient(90deg,#233a7a_0%,#4F72C8_100%)] transition-all duration-300", loading ? "w-full opacity-100" : "w-0 opacity-0")} />
      </div>

      <div id="workspace-shell-container" className="mx-auto flex min-h-screen max-w-[1700px] flex-col px-4 pb-4 pt-2 lg:h-[100dvh] lg:min-h-0 lg:px-6 lg:pb-5 lg:pt-2">
        <header
          id="workspace-header"
          className="relative z-20 mb-[0.3rem] rounded-[20px] border border-slate-200/70 bg-white/68 px-3 py-1 shadow-[0_6px_18px_rgba(148,163,184,0.10)] backdrop-blur lg:px-4 lg:py-1.5"
        >
          <div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
            <div id="workspace-header-brand" className="flex items-center gap-2">
              <div id="workspace-header-logo" className="w-full max-w-[82px] shrink-0 lg:max-w-[88px]">
                <Image src="/brand/sinwoo-logo.png" alt="Sinwoo International" width={800} height={389} className="h-auto w-full" priority />
              </div>
              <div>
                <p id="workspace-header-eyebrow" className="text-[9px] uppercase tracking-[0.22em] text-slate-400">{ui.eyebrow}</p>
                <h1 id="workspace-header-title" className="font-brand text-[15px] font-semibold tracking-tight text-slate-700 lg:text-base">{config.shellTitle}</h1>
              </div>
            </div>

            <div id="workspace-header-actions" className="flex flex-wrap items-center gap-1">
              <div id="workspace-mode-switcher" className="inline-flex rounded-2xl border border-slate-200/80 bg-white/75 p-1">
                <button
                  id="workspace-mode-client"
                  type="button"
                  onClick={() => switchMode("client")}
                  className={cn(
                    "rounded-xl px-2.5 py-0.5 text-[13px] font-medium transition-colors",
                    mode === "client" ? "bg-slate-100 text-slate-700" : "text-slate-400 hover:text-slate-600"
                  )}
                >
                  {ui.client}
                </button>
                <button
                  id="workspace-mode-admin"
                  type="button"
                  onClick={() => switchMode("admin")}
                  className={cn(
                    "rounded-xl px-2.5 py-0.5 text-[13px] font-medium transition-colors",
                    mode === "admin" ? "bg-[#EAF0FB] text-[#18397E]" : "text-slate-400 hover:text-slate-600"
                  )}
                >
                  {ui.admin}
                </button>
              </div>
              <Button id="workspace-profile-button" type="button" variant="outline" size="icon" className="h-9 w-9 rounded-2xl border-slate-200/80 bg-white/75 text-slate-500 hover:bg-slate-50 hover:text-slate-700" onClick={openProfileTab}>
                <UserCircle2 className="h-4 w-4" />
              </Button>
              <LocaleCombobox
                idPrefix="workspace"
                value={locale}
                localeLabel={localeMessages.localeLabel}
                localeNames={localeMessages.localeNames}
                onChange={setLocale}
                align="center"
                menuStrategy="fixed"
                buttonClassName="justify-between gap-1.5 px-2.5 py-1 text-[12px] text-slate-600"
                menuClassName="min-w-[124px]"
              />
              <Button
                id="workspace-logout-button"
                type="button"
                variant="outline"
                size="icon"
                className="h-9 w-9 rounded-2xl border-slate-200/80 bg-white/75 text-slate-600 hover:bg-slate-50 hover:text-slate-800"
                onClick={handleLogout}
              >
                <LogOut className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </header>

        <div id="workspace-desktop-layout" className="hidden min-h-0 flex-1 gap-4 lg:flex">
          <aside
            id="workspace-sidebar"
            className={cn("workspace-scrollbar self-stretch overflow-y-auto rounded-[30px] border border-white/80 bg-white/78 p-4 shadow-sm backdrop-blur transition-all", sidebarCollapsed ? "w-[96px]" : "w-[272px]")}
          >
            <div id="workspace-sidebar-header" className="mb-5 flex items-center justify-between">
              {!sidebarCollapsed ? (
                <div>
                  <p id="workspace-sidebar-eyebrow" className="text-[10px] uppercase tracking-[0.24em] text-slate-400">{ui.navigation}</p>
                  <p id="workspace-sidebar-label" className="mt-1 text-[13px] font-medium text-[#18397E]">{config.label}</p>
                </div>
              ) : (
                <div className="text-[10px] uppercase tracking-[0.24em] text-slate-400">{ui.navShort}</div>
              )}
              <button id="workspace-sidebar-toggle" type="button" onClick={() => setSidebarCollapsed((current) => !current)} className="rounded-xl border border-slate-200 p-1.5 text-slate-500 transition-colors hover:bg-slate-50 hover:text-slate-950">
                {sidebarCollapsed ? <ChevronRight className="h-3.5 w-3.5" /> : <ChevronLeft className="h-3.5 w-3.5" />}
              </button>
            </div>
            <div id="workspace-sidebar-tree" className="space-y-1.5">{currentMenus.map((menu) => renderMenuNode(menu))}</div>
          </aside>

          <section id="workspace-main-panel" className="flex min-w-0 min-h-0 flex-1 flex-col rounded-[30px] border border-white/80 bg-white/78 shadow-sm backdrop-blur">
            <div
              id="workspace-tabbar"
              className="relative z-10 border-b border-slate-200 bg-[linear-gradient(180deg,#F7F9FD_0%,#EEF3FB_100%)]"
            >
              <div
                id="workspace-tabbar-scroll"
                className="flex h-[42px] items-end gap-1.5 px-2 pb-0 pt-0"
              >
                <div
                  id="workspace-tabbar-track"
                  ref={tabScrollRef}
                  className="workspace-tab-scroll h-full min-w-0 flex-1 overflow-x-auto overflow-y-hidden"
                >
                  <div id="workspace-tab-list" className="flex h-full w-max items-end gap-[0.1rem]">
                    {desktopTabs.map((tab) => {
                      const isActive = tab.id === activeTabId;
                      return (
                      <div
                        id={`workspace-tab-${tab.id}`}
                        key={tab.id}
                        onContextMenu={(event) => openTabContextMenu(event, tab.id)}
                        className={cn(
                          "group relative mb-[-1px] inline-flex min-w-fit items-center gap-1.5 rounded-t-[12px] border border-b-0 px-3 py-1 text-[12px] leading-5 transition-all",
                          isActive
                              ? "z-10 border-slate-300 bg-white text-slate-950 shadow-[0_-1px_0_rgba(255,255,255,0.85)]"
                              : "translate-y-[4px] border-slate-200/70 bg-[linear-gradient(180deg,#F4F7FC_0%,#E6EDF8_100%)] text-slate-500 hover:border-slate-300 hover:bg-[linear-gradient(180deg,#F8FAFE_0%,#EDF3FB_100%)] hover:text-slate-700"
                          )}
                        >
                          <button
                            id={`workspace-tab-button-${tab.id}`}
                          type="button"
                          onContextMenu={(event) => openTabContextMenu(event, tab.id)}
                          onClick={() => {
                            setActiveTabId(tab.id);
                            simulateLoading(setLoading);
                            }}
                            className="max-w-[180px] truncate"
                          >
                            {getTabTitle(tab.id, tab.title)}
                          </button>
                          {tab.id !== config.defaultTabId ? (
                            <button
                              id={`workspace-tab-close-${tab.id}`}
                              type="button"
                              onClick={() => closeTab(tab.id)}
                              className={cn(
                              "rounded-full p-0.5 text-slate-400 transition-all hover:bg-slate-200 hover:text-slate-900",
                              isActive ? "opacity-100" : "opacity-0 group-hover:opacity-100"
                            )}
                            onContextMenu={(event) => openTabContextMenu(event, tab.id)}
                          >
                            <X className="h-3.5 w-3.5" />
                          </button>
                          ) : null}
                        </div>
                      );
                    })}
                  </div>
                </div>
                {showTabScrollControls ? (
                  <div
                    id="workspace-tab-scroll-controls"
                    className="mb-1 flex shrink-0 self-end items-center gap-1 rounded-xl border border-slate-200 bg-white/92 px-1 py-0.5 shadow-sm backdrop-blur"
                  >
                    <button
                      id="workspace-tab-scroll-prev"
                      type="button"
                      onClick={() => scrollTabs("left")}
                      disabled={!canScrollTabsLeft}
                      className="rounded-md p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
                    >
                      <ChevronLeft className="h-4 w-4" />
                    </button>
                    <button
                      id="workspace-tab-scroll-next"
                      type="button"
                      onClick={() => scrollTabs("right")}
                      disabled={!canScrollTabsRight}
                      className="rounded-md p-1 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:cursor-default disabled:opacity-30"
                    >
                      <ChevronRight className="h-4 w-4" />
                    </button>
                  </div>
                ) : null}
              </div>
            </div>

            {tabContextMenu ? (
              <div
                id="workspace-tab-context-menu"
                className="absolute z-50 w-[164px] rounded-xl border border-slate-200 bg-[#F7FAFF] p-1.5 shadow-[0_16px_40px_rgba(15,23,42,0.16)]"
                style={getTabContextMenuStyle()}
                onClick={(event) => event.stopPropagation()}
              >
                <button
                  id="workspace-tab-context-close"
                  type="button"
                  disabled={tabContextMenu.tabId === config.defaultTabId}
                  onClick={() => closeTab(tabContextMenu.tabId)}
                  className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100 disabled:cursor-default disabled:text-slate-300 disabled:hover:bg-transparent"
                >
                  {tabContextUi.close}
                </button>
                <button
                  id="workspace-tab-context-close-others"
                  type="button"
                  onClick={() => closeOtherTabs(tabContextMenu.tabId)}
                  className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100"
                >
                  {tabContextUi.closeOther}
                </button>
                <button
                  id="workspace-tab-context-close-all"
                  type="button"
                  onClick={closeAllTabs}
                  className="flex w-full items-center rounded-lg px-2.5 py-1.5 text-left text-[12px] text-slate-700 transition-colors hover:bg-slate-100"
                >
                  {tabContextUi.closeAll}
                </button>
              </div>
            ) : null}

            <div id="workspace-body" className="workspace-scrollbar flex-1 overflow-y-auto p-5">
              <div id="workspace-hero-grid" className="grid gap-5 xl:grid-cols-[1.15fr_0.85fr]">
                <WorkspacePageHeader
                  id="workspace-hero-card"
                  eyebrow={activeView.eyebrow}
                  eyebrowId="workspace-hero-eyebrow"
                  title={activeView.title}
                  titleId="workspace-hero-title"
                  description={activeView.description}
                  descriptionId="workspace-hero-description"
                  actionsId="workspace-page-header-actions"
                  actions={
                    <>
                      <div
                        id="workspace-page-header-action-mode"
                        className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
                      >
                        <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">{ui.modeLabel}</div>
                        <div className="mt-1 text-sm font-semibold">{mode === "admin" ? ui.modeAdminValue : ui.modeClientValue}</div>
                      </div>
                      <div
                        id="workspace-page-header-action-sidebar"
                        className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
                      >
                        <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">{ui.sidebarLabel}</div>
                        <div className="mt-1 text-sm font-semibold">{sidebarCollapsed ? ui.collapsed : ui.expanded}</div>
                      </div>
                      <div
                        id="workspace-page-header-action-tabs"
                        className="rounded-2xl border border-white/15 bg-white/10 px-3 py-2 text-left text-white"
                      >
                        <div className="text-[11px] uppercase tracking-[0.24em] text-slate-200">{ui.tabsLabel}</div>
                        <div className="mt-1 text-sm font-semibold">{desktopTabs.length}</div>
                      </div>
                    </>
                  }
                  footerId="workspace-hero-stats"
                  footer={
                    <div className="grid gap-3 md:grid-cols-3">
                      <div id="workspace-stat-mode" className="rounded-2xl border border-white/15 bg-white/10 p-4">
                        <div className="text-xs uppercase tracking-[0.24em] text-slate-200">{ui.modeLabel}</div>
                        <div className="mt-2 text-xl font-semibold">{mode === "admin" ? ui.modeAdminValue : ui.modeClientValue}</div>
                      </div>
                      <div id="workspace-stat-sidebar" className="rounded-2xl border border-white/15 bg-white/10 p-4">
                        <div className="text-xs uppercase tracking-[0.24em] text-slate-200">{ui.sidebarLabel}</div>
                        <div className="mt-2 text-xl font-semibold">{sidebarCollapsed ? ui.collapsed : ui.expanded}</div>
                      </div>
                      <div id="workspace-stat-tabs" className="rounded-2xl border border-white/15 bg-white/10 p-4">
                        <div className="text-xs uppercase tracking-[0.24em] text-slate-200">{ui.tabsLabel}</div>
                        <div className="mt-2 text-xl font-semibold">{desktopTabs.length}</div>
                      </div>
                    </div>
                  }
                  className="border-white bg-gradient-to-br from-[#132C67] via-[#1F4FAE] to-[#4F72C8] text-white shadow-[0_24px_60px_rgba(35,58,122,0.28)]"
                  eyebrowClassName="text-slate-200"
                  titleClassName="text-4xl font-bold text-white"
                  descriptionClassName="text-base text-slate-100"
                />

                {mode === "client" ? (
                  <WorkspaceAttendanceCard
                    accessToken={accessToken}
                    locale={locale}
                    onUnauthorized={redirectToLogin}
                    onLoadingChange={setLoading}
                  />
                ) : (
                  <WorkspaceSectionPanel
                    id="workspace-rules-card"
                    eyebrow={ui.quickActions}
                    eyebrowId="workspace-rules-eyebrow"
                    contentClassName="space-y-3 text-sm text-slate-600"
                  >
                      <div id="workspace-rule-client-admin-switch" className="rounded-2xl bg-slate-50 p-4">{ui.ruleOne}</div>
                      <div id="workspace-rule-depth-menu" className="rounded-2xl bg-slate-50 p-4">{ui.ruleTwo}</div>
                      <div id="workspace-rule-mobile-simplify" className="rounded-2xl bg-slate-50 p-4">{ui.ruleThree}</div>
                  </WorkspaceSectionPanel>
                )}
              </div>

              <div id="workspace-kpi-grid" className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                {activeView.kpis.map((item, index) => (
                  <Card id={`workspace-kpi-card-${index + 1}`} key={`${item.label}-${index}`}>
                    <CardHeader className="pb-3">
                      <CardDescription id={`workspace-kpi-label-${index + 1}`} className="uppercase tracking-[0.2em]">{item.label}</CardDescription>
                      <CardTitle id={`workspace-kpi-value-${index + 1}`} className="font-brand text-4xl">{item.value}</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p id={`workspace-kpi-delta-${index + 1}`} className="text-sm text-slate-500">{item.delta}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>

              <div id="workspace-bottom-grid" className="mt-5 grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
                <WorkspaceSectionPanel
                  id="workspace-priority-card"
                  title={ui.focusTitle}
                  titleId="workspace-priority-title"
                  description={ui.focusDescription}
                  descriptionId="workspace-priority-description"
                  contentClassName="space-y-3"
                >
                    {activeView.highlights.map((item, index) => (
                      <div id={`workspace-highlight-card-${index + 1}`} key={`${item.title}-${index}`} className={cn("rounded-2xl border p-4", item.emphasis === "warning" ? "border-amber-200 bg-amber-50" : item.emphasis === "success" ? "border-emerald-200 bg-emerald-50" : "border-slate-200 bg-slate-50")}>
                        <div id={`workspace-highlight-title-${index + 1}`} className="font-medium text-slate-950">{item.title}</div>
                        <div id={`workspace-highlight-meta-${index + 1}`} className="mt-1 text-sm text-slate-500">{item.meta}</div>
                      </div>
                    ))}
                </WorkspaceSectionPanel>

                <WorkspaceSectionPanel
                  id="workspace-grid-card"
                  title={activeView.gridTitle}
                  titleId="workspace-grid-title"
                  description={ui.tableDescription}
                  descriptionId="workspace-grid-description"
                  contentClassName="space-y-3"
                >
                  <WorkspaceContentContainer id="workspace-grid-container" className="space-y-3 bg-slate-50/55">
                    <div id="workspace-grid-header" className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-3 rounded-2xl border border-slate-200 bg-slate-100 px-4 py-3 text-xs uppercase tracking-[0.22em] text-slate-500">
                      <div>{ui.name}</div>
                      <div>{ui.owner}</div>
                      <div>{ui.status}</div>
                      <div>{ui.updated}</div>
                    </div>
                    {activeView.gridRows.map((row, index) => (
                      <div id={`workspace-grid-row-${index + 1}`} key={`${row.name}-${row.updated}-${index}`} className="grid grid-cols-[1.7fr_1.1fr_1fr_0.9fr] gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-4 text-sm shadow-[0_8px_18px_rgba(148,163,184,0.06)]">
                        <div id={`workspace-grid-name-${index + 1}`} className="font-medium text-slate-950">{row.name}</div>
                        <div id={`workspace-grid-owner-${index + 1}`} className="text-slate-600">{row.owner}</div>
                        <div id={`workspace-grid-status-${index + 1}`} className="text-slate-700">{row.status}</div>
                        <div id={`workspace-grid-updated-${index + 1}`} className="text-slate-500">{row.updated}</div>
                      </div>
                    ))}
                  </WorkspaceContentContainer>
                </WorkspaceSectionPanel>
              </div>
            </div>
          </section>
        </div>

        <div id="workspace-mobile-layout" className="space-y-4 lg:hidden">
          <div id="workspace-mobile-header-card" className="rounded-[28px] border border-white/80 bg-white/80 p-4 shadow-sm backdrop-blur">
            <div className="flex items-center justify-between">
              <div>
                <p id="workspace-mobile-eyebrow" className="text-xs uppercase tracking-[0.28em] text-slate-500">{ui.mobileWeb}</p>
                <h2 id="workspace-mobile-title" className="font-brand text-2xl font-bold text-slate-950">{config.shellTitle}</h2>
              </div>
              <Button id="workspace-mobile-menu-toggle" variant="outline" size="icon" onClick={() => setMobileMenuOpen((current) => !current)}>
                <Menu className="h-4 w-4" />
              </Button>
            </div>
            {mobileMenuOpen ? (
              <div id="workspace-mobile-menu-list" className="mt-4 grid gap-2">
                {mobileMenus.map((menu) => (
                  <button id={`workspace-mobile-menu-${menu.id}`} key={menu.id} type="button" onClick={() => openMenuAsTab(menu)} className={cn("rounded-2xl border px-4 py-3 text-left", activeTabId === menu.id ? "border-[#233a7a] bg-[#233a7a] text-white" : "border-slate-200 bg-slate-50")}>
                    {menu.title}
                  </button>
                ))}
              </div>
            ) : null}
          </div>

          <Card id="workspace-mobile-hero-card" className="border-white bg-gradient-to-br from-[#132C67] via-[#1F4FAE] to-[#4F72C8] text-white">
            <CardHeader>
              <CardDescription id="workspace-mobile-hero-eyebrow" className="text-slate-200 uppercase tracking-[0.28em]">{activeView.eyebrow}</CardDescription>
              <CardTitle id="workspace-mobile-hero-title" className="font-brand text-3xl">{activeView.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <p id="workspace-mobile-hero-description" className="text-sm text-slate-100">{activeView.description}</p>
            </CardContent>
          </Card>

          <Card id="workspace-mobile-focus-card">
            <CardHeader>
              <CardTitle id="workspace-mobile-focus-title" className="font-brand text-2xl">{ui.mobileFocusTitle}</CardTitle>
              <CardDescription id="workspace-mobile-focus-description">{ui.mobileFocusDescription}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {activeView.highlights.map((item, index) => (
                <div id={`workspace-mobile-highlight-${index + 1}`} key={`${item.title}-${index}`} className="rounded-2xl bg-slate-50 p-4">
                  <div id={`workspace-mobile-highlight-title-${index + 1}`} className="font-medium text-slate-950">{item.title}</div>
                  <div id={`workspace-mobile-highlight-meta-${index + 1}`} className="mt-1 text-sm text-slate-500">{item.meta}</div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>
    </main>
  );
}

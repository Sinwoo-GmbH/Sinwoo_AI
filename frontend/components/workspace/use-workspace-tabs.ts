"use client";

import type { MouseEvent as ReactMouseEvent } from "react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import type { LoginLocale } from "@/lib/i18n/login-content";
import {
  findMenuTitle,
  type MenuNode,
  type TabItem,
  type WorkspaceMode,
} from "@/lib/workspace/platform-shell-data";
import {
  canonicalizeMenuId,
  findMenuTrailIds,
  resolveWorkspaceTabTitle,
} from "@/lib/workspace/workspace-menu-utils";
import {
  cloneWorkspaceTabs,
  PROFILE_TAB_ID,
  TAB_CONTEXT_MENU_WIDTH,
} from "@/lib/workspace/workspace-tab-utils";
import type { PersistedWorkspaceShellState } from "@/lib/workspace/workspace-storage";

type TabContextMenuState = {
  tabId: string;
} | null;

type RestoreModeStateParams = {
  nextMode: WorkspaceMode;
  nextLocale: LoginLocale;
  nextDefaultTabId: string;
  nextInitialExpandedMenuIds: string[];
  persistedState: PersistedWorkspaceShellState | null;
};

type UseWorkspaceTabsParams = {
  currentMenus: MenuNode[];
  defaultTabId: string;
  initialExpandedMenuIds: string[];
  initialPersistedState: PersistedWorkspaceShellState | null;
  isReady: boolean;
  locale: LoginLocale;
  mode: WorkspaceMode;
  onLoadingChange: (value: boolean) => void;
  profileTabTitle: string;
};

function simulateLoading(setLoading: (value: boolean) => void) {
  setLoading(true);
  window.setTimeout(() => setLoading(false), 280);
}

export function useWorkspaceTabs({
  currentMenus,
  defaultTabId,
  initialExpandedMenuIds,
  initialPersistedState,
  isReady,
  locale,
  mode,
  onLoadingChange,
  profileTabTitle,
}: UseWorkspaceTabsParams) {
  const bootstrappedRef = useRef(false);
  const tabScrollRef = useRef<HTMLDivElement | null>(null);
  const [openTabs, setOpenTabs] = useState<TabItem[]>([]);
  const [activeTabId, setActiveTabId] = useState("");
  const [expandedMenuIds, setExpandedMenuIds] = useState<string[]>([]);
  const [canScrollTabsLeft, setCanScrollTabsLeft] = useState(false);
  const [canScrollTabsRight, setCanScrollTabsRight] = useState(false);
  const [showTabScrollControls, setShowTabScrollControls] = useState(false);
  const [tabContextMenu, setTabContextMenu] = useState<TabContextMenuState>(null);

  const getDefaultTabTitle = useCallback(
    (targetMode: WorkspaceMode = mode, targetLocale: LoginLocale = locale, targetTabId = defaultTabId) =>
      findMenuTitle(targetMode, targetTabId, targetLocale),
    [defaultTabId, locale, mode]
  );

  const getTabTitle = useCallback(
    (tabId: string, fallbackTitle?: string) =>
      resolveWorkspaceTabTitle(
        currentMenus,
        mode,
        locale,
        tabId,
        PROFILE_TAB_ID,
        profileTabTitle,
        fallbackTitle
      ),
    [currentMenus, locale, mode, profileTabTitle]
  );

  const activePageId = useMemo(
    () =>
      activeTabId === PROFILE_TAB_ID
        ? PROFILE_TAB_ID
        : canonicalizeMenuId(currentMenus, activeTabId || defaultTabId),
    [activeTabId, currentMenus, defaultTabId]
  );

  const desktopTabs = useMemo(
    () =>
      openTabs.length
        ? openTabs
        : [{ id: defaultTabId, title: getDefaultTabTitle(mode, locale, defaultTabId) }],
    [defaultTabId, getDefaultTabTitle, locale, mode, openTabs]
  );

  useEffect(() => {
    if (!isReady || bootstrappedRef.current) return;

    setOpenTabs(
      initialPersistedState?.openTabs?.length
        ? cloneWorkspaceTabs(initialPersistedState.openTabs)
        : [{ id: defaultTabId, title: getDefaultTabTitle(mode, locale, defaultTabId) }]
    );
    setActiveTabId(initialPersistedState?.activeTabId ?? defaultTabId);
    setExpandedMenuIds(initialExpandedMenuIds);
    bootstrappedRef.current = true;
  }, [
    defaultTabId,
    getDefaultTabTitle,
    initialExpandedMenuIds,
    initialPersistedState,
    isReady,
    locale,
    mode,
  ]);

  useEffect(() => {
    if (!isReady) return;
    if (!openTabs.some((tab) => tab.id === activeTabId)) {
      setActiveTabId(openTabs[0]?.id ?? defaultTabId);
    }
  }, [activeTabId, defaultTabId, isReady, openTabs]);

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
        const nextDefaultTabId = canonicalizeMenuId(currentMenus, defaultTabId);
        nextTabs.push({
          id: nextDefaultTabId,
          title: getTabTitle(nextDefaultTabId, getDefaultTabTitle(mode, locale, defaultTabId)),
        });
        changed = true;
      }

      const canonicalActive =
        nextActiveTabId === PROFILE_TAB_ID
          ? PROFILE_TAB_ID
          : canonicalizeMenuId(currentMenus, nextActiveTabId || defaultTabId);

      nextActiveTabId = nextTabs.some((tab) => tab.id === canonicalActive)
        ? canonicalActive
        : nextTabs[0].id;

      return changed ? nextTabs : current;
    });

    if (nextActiveTabId !== activeTabId) {
      setActiveTabId(nextActiveTabId);
    }
  }, [activeTabId, currentMenus, defaultTabId, getDefaultTabTitle, getTabTitle, isReady, locale, mode]);

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
  }, [defaultTabId, openTabs.length]);

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

  const activateTab = useCallback(
    (tabId: string) => {
      setActiveTabId(tabId);
      simulateLoading(onLoadingChange);
    },
    [onLoadingChange]
  );

  const openMenuAsTab = useCallback(
    (menu: MenuNode) => {
      if (menu.children?.length) {
        setExpandedMenuIds((current) =>
          current.includes(menu.id)
            ? current.filter((id) => id !== menu.id)
            : [...current, menu.id]
        );
        return;
      }

      setOpenTabs((current) =>
        current.some((tab) => tab.id === menu.id)
          ? current
          : [...current, { id: menu.id, title: menu.title }]
      );
      setActiveTabId(menu.id);
      simulateLoading(onLoadingChange);
    },
    [onLoadingChange]
  );

  const openProfileTab = useCallback(() => {
    setOpenTabs((current) =>
      current.some((tab) => tab.id === PROFILE_TAB_ID)
        ? current
        : [...current, { id: PROFILE_TAB_ID, title: profileTabTitle }]
    );
    setActiveTabId(PROFILE_TAB_ID);
    simulateLoading(onLoadingChange);
  }, [onLoadingChange, profileTabTitle]);

  const closeTab = useCallback(
    (tabId: string) => {
      setTabContextMenu(null);
      setOpenTabs((current) => {
        const nextTabs = current.filter((tab) => tab.id !== tabId);
        if (activeTabId === tabId) {
          setActiveTabId(nextTabs[nextTabs.length - 1]?.id ?? defaultTabId);
        }

        return nextTabs.length
          ? nextTabs
          : [{ id: defaultTabId, title: getDefaultTabTitle(mode, locale, defaultTabId) }];
      });
    },
    [activeTabId, defaultTabId, getDefaultTabTitle, locale, mode]
  );

  const closeOtherTabs = useCallback(
    (tabId: string) => {
      setTabContextMenu(null);
      setOpenTabs((current) => {
        const selectedTab = current.find((tab) => tab.id === tabId);
        const defaultTab =
          current.find((tab) => tab.id === defaultTabId) ?? {
            id: defaultTabId,
            title: getTabTitle(defaultTabId, getDefaultTabTitle(mode, locale, defaultTabId)),
          };

        if (!selectedTab || selectedTab.id === defaultTab.id) {
          setActiveTabId(defaultTab.id);
          return [defaultTab];
        }

        setActiveTabId(selectedTab.id);
        return [defaultTab, selectedTab];
      });
      simulateLoading(onLoadingChange);
    },
    [defaultTabId, getDefaultTabTitle, getTabTitle, locale, mode, onLoadingChange]
  );

  const closeAllTabs = useCallback(() => {
    setTabContextMenu(null);
    const defaultTab = {
      id: defaultTabId,
      title: getTabTitle(defaultTabId, getDefaultTabTitle(mode, locale, defaultTabId)),
    };
    setOpenTabs([defaultTab]);
    setActiveTabId(defaultTab.id);
    simulateLoading(onLoadingChange);
  }, [defaultTabId, getDefaultTabTitle, getTabTitle, locale, mode, onLoadingChange]);

  const openTabContextMenu = useCallback((event: ReactMouseEvent<HTMLElement>, tabId: string) => {
    event.preventDefault();
    event.stopPropagation();
    setTabContextMenu({ tabId });
  }, []);

  const scrollTabs = useCallback((direction: "left" | "right") => {
    const tabScroller = tabScrollRef.current;
    if (!tabScroller) return;

    tabScroller.scrollBy({
      left: direction === "left" ? -220 : 220,
      behavior: "smooth",
    });
  }, []);

  const getTabContextMenuStyle = useCallback(() => {
    if (typeof window === "undefined" || !tabContextMenu) return undefined;

    const tabbarElement = document.getElementById("workspace-tabbar");
    const tabElement = document.getElementById(`workspace-tab-${tabContextMenu.tabId}`);
    if (!tabbarElement || !tabElement) return undefined;

    const tabbarRect = tabbarElement.getBoundingClientRect();
    const tabRect = tabElement.getBoundingClientRect();
    const left = Math.min(
      Math.max(
        tabRect.left - tabbarRect.left + tabRect.width / 2 - TAB_CONTEXT_MENU_WIDTH / 2,
        8
      ),
      tabbarRect.width - TAB_CONTEXT_MENU_WIDTH - 8
    );

    return {
      left,
      top: tabRect.bottom - tabbarRect.top + 6,
    };
  }, [tabContextMenu]);

  const restoreModeState = useCallback(
    ({
      nextMode,
      nextLocale,
      nextDefaultTabId,
      nextInitialExpandedMenuIds,
      persistedState,
    }: RestoreModeStateParams) => {
      setOpenTabs(
        persistedState?.openTabs?.length
          ? cloneWorkspaceTabs(persistedState.openTabs)
          : [
              {
                id: nextDefaultTabId,
                title: findMenuTitle(nextMode, nextDefaultTabId, nextLocale),
              },
            ]
      );
      setActiveTabId(persistedState?.activeTabId ?? nextDefaultTabId);
      setExpandedMenuIds(nextInitialExpandedMenuIds);
      setTabContextMenu(null);
      simulateLoading(onLoadingChange);
    },
    [onLoadingChange]
  );

  return {
    tabScrollRef,
    openTabs,
    activeTabId,
    activePageId,
    expandedMenuIds,
    desktopTabs,
    showTabScrollControls,
    canScrollTabsLeft,
    canScrollTabsRight,
    tabContextMenu,
    getTabTitle,
    getTabContextMenuStyle,
    setActiveTabId,
    activateTab,
    openMenuAsTab,
    openProfileTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
    openTabContextMenu,
    scrollTabs,
    restoreModeState,
  };
}

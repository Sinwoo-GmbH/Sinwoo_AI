"use client";

import type { MouseEvent as ReactMouseEvent } from "react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import type { LoginLocale } from "@/lib/i18n/login-cnt";
import {
  findMnuTitle,
  type MnuNode,
  type TabItem,
  type WspMode,
} from "@/lib/utils/wsp/platform-shell-data";
import {
  canonicalizeMnuId,
  findMnuTrailIds,
  resolveWspTabTitle,
} from "@/lib/utils/wsp/wsp-mnu-utils";
import {
  cloneWspTabs,
  PROFILE_TAB_ID,
  TAB_CTX_MNU_WIDTH,
} from "@/lib/utils/wsp/wsp-tab-utils";
import type { PersistedWspShellState } from "@/lib/utils/wsp/wsp-storage";

type TabContextMnuState = {
  tabId: string;
} | null;

type RestoreModeStateParams = {
  nextMode: WspMode;
  nextLocale: LoginLocale;
  nextDefaultTabId: string;
  nextInitialExpandedMnuIds: string[];
  persistedState: PersistedWspShellState | null;
};

type UseWspTabsParams = {
  currentMnus: MnuNode[];
  defaultTabId: string;
  initialExpandedMnuIds: string[];
  initialPersistedState: PersistedWspShellState | null;
  isReady: boolean;
  locale: LoginLocale;
  mode: WspMode;
  onLoadingChange: (value: boolean) => void;
  profileTabTitle: string;
};

function simulateLoading(setLoading: (value: boolean) => void) {
  setLoading(true);
  window.setTimeout(() => setLoading(false), 280);
}

export function useWspTabs({
  currentMnus,
  defaultTabId,
  initialExpandedMnuIds,
  initialPersistedState,
  isReady,
  locale,
  mode,
  onLoadingChange,
  profileTabTitle,
}: UseWspTabsParams) {
  const bootstrappedRef = useRef(false);
  const tabScrollRef = useRef<HTMLDivElement | null>(null);
  const [openTabs, setOpenTabs] = useState<TabItem[]>([]);
  const [activeTabId, setActiveTabId] = useState("");
  const [expandedMnuIds, setExpandedMnuIds] = useState<string[]>([]);
  const [canScrollTabsLeft, setCanScrollTabsLeft] = useState(false);
  const [canScrollTabsRight, setCanScrollTabsRight] = useState(false);
  const [showTabScrollControls, setShowTabScrollControls] = useState(false);
  const [tabContextMnu, setTabContextMnu] = useState<TabContextMnuState>(null);

  const getDefaultTabTitle = useCallback(
    (targetMode: WspMode = mode, targetLocale: LoginLocale = locale, targetTabId = defaultTabId) =>
      findMnuTitle(targetMode, targetTabId, targetLocale),
    [defaultTabId, locale, mode]
  );

  const getTabTitle = useCallback(
    (tabId: string, fallbackTitle?: string) =>
      resolveWspTabTitle(
        currentMnus,
        mode,
        locale,
        tabId,
        PROFILE_TAB_ID,
        profileTabTitle,
        fallbackTitle
      ),
    [currentMnus, locale, mode, profileTabTitle]
  );

  const activePageId = useMemo(
    () =>
      activeTabId === PROFILE_TAB_ID
        ? PROFILE_TAB_ID
        : canonicalizeMnuId(currentMnus, activeTabId || defaultTabId),
    [activeTabId, currentMnus, defaultTabId]
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
        ? cloneWspTabs(initialPersistedState.openTabs)
        : [{ id: defaultTabId, title: getDefaultTabTitle(mode, locale, defaultTabId) }]
    );
    setActiveTabId(initialPersistedState?.activeTabId ?? defaultTabId);
    setExpandedMnuIds(initialExpandedMnuIds);
    bootstrappedRef.current = true;
  }, [
    defaultTabId,
    getDefaultTabTitle,
    initialExpandedMnuIds,
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
    if (!isReady || !currentMnus.length) return;

    let nextActiveTabId = activeTabId;

    setOpenTabs((current) => {
      let changed = false;
      const seen = new Set<string>();
      const nextTabs: TabItem[] = [];

      for (const tab of current) {
        const nextId =
          tab.id === PROFILE_TAB_ID ? PROFILE_TAB_ID : canonicalizeMnuId(currentMnus, tab.id);
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
        const nextDefaultTabId = canonicalizeMnuId(currentMnus, defaultTabId);
        nextTabs.push({
          id: nextDefaultTabId,
          title: getTabTitle(nextDefaultTabId, getDefaultTabTitle(mode, locale, defaultTabId)),
        });
        changed = true;
      }

      const canonicalActive =
        nextActiveTabId === PROFILE_TAB_ID
          ? PROFILE_TAB_ID
          : canonicalizeMnuId(currentMnus, nextActiveTabId || defaultTabId);

      nextActiveTabId = nextTabs.some((tab) => tab.id === canonicalActive)
        ? canonicalActive
        : nextTabs[0].id;

      return changed ? nextTabs : current;
    });

    if (nextActiveTabId !== activeTabId) {
      setActiveTabId(nextActiveTabId);
    }
  }, [activeTabId, currentMnus, defaultTabId, getDefaultTabTitle, getTabTitle, isReady, locale, mode]);

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
    if (!tabContextMnu) return;

    const closeContextMnu = () => setTabContextMnu(null);
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setTabContextMnu(null);
      }
    };

    window.addEventListener("click", closeContextMnu);
    window.addEventListener("contextmenu", closeContextMnu);
    window.addEventListener("keydown", closeOnEscape);

    return () => {
      window.removeEventListener("click", closeContextMnu);
      window.removeEventListener("contextmenu", closeContextMnu);
      window.removeEventListener("keydown", closeOnEscape);
    };
  }, [tabContextMnu]);

  useEffect(() => {
    if (!isReady || !activeTabId) return;

    const trail = findMnuTrailIds(currentMnus, activeTabId);
    if (!trail?.length) return;

    const parentIds = trail.slice(0, -1);
    if (!parentIds.length) return;

    setExpandedMnuIds((current) => {
      const merged = [...new Set([...current, ...parentIds])];

      if (
        merged.length === current.length &&
        merged.every((value, index) => value === current[index])
      ) {
        return current;
      }

      return merged;
    });
  }, [activeTabId, currentMnus, isReady]);

  const activateTab = useCallback(
    (tabId: string) => {
      setActiveTabId(tabId);
      simulateLoading(onLoadingChange);
    },
    [onLoadingChange]
  );

  const openMnuAsTab = useCallback(
    (mnu: MnuNode) => {
      if (mnu.children?.length) {
        setExpandedMnuIds((current) =>
          current.includes(mnu.id)
            ? current.filter((id) => id !== mnu.id)
            : [...current, mnu.id]
        );
        return;
      }

      setOpenTabs((current) =>
        current.some((tab) => tab.id === mnu.id)
          ? current
          : [...current, { id: mnu.id, title: mnu.title }]
      );
      setActiveTabId(mnu.id);
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
      setTabContextMnu(null);
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
      setTabContextMnu(null);
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
    setTabContextMnu(null);
    const defaultTab = {
      id: defaultTabId,
      title: getTabTitle(defaultTabId, getDefaultTabTitle(mode, locale, defaultTabId)),
    };
    setOpenTabs([defaultTab]);
    setActiveTabId(defaultTab.id);
    simulateLoading(onLoadingChange);
  }, [defaultTabId, getDefaultTabTitle, getTabTitle, locale, mode, onLoadingChange]);

  const openTabContextMnu = useCallback((event: ReactMouseEvent<HTMLElement>, tabId: string) => {
    event.preventDefault();
    event.stopPropagation();
    setTabContextMnu({ tabId });
  }, []);

  const scrollTabs = useCallback((direction: "left" | "right") => {
    const tabScroller = tabScrollRef.current;
    if (!tabScroller) return;

    tabScroller.scrollBy({
      left: direction === "left" ? -220 : 220,
      behavior: "smooth",
    });
  }, []);

  const getTabContextMnuStyle = useCallback(() => {
    if (typeof window === "undefined" || !tabContextMnu) return undefined;

    const tabbarElement = document.getElementById("wsp-tabbar");
    const tabElement = document.getElementById(`wsp-tab-${tabContextMnu.tabId}`);
    if (!tabbarElement || !tabElement) return undefined;

    const tabbarRect = tabbarElement.getBoundingClientRect();
    const tabRect = tabElement.getBoundingClientRect();
    const left = Math.min(
      Math.max(
        tabRect.left - tabbarRect.left + tabRect.width / 2 - TAB_CTX_MNU_WIDTH / 2,
        8
      ),
      tabbarRect.width - TAB_CTX_MNU_WIDTH - 8
    );

    return {
      left,
      top: tabRect.bottom - tabbarRect.top + 6,
    };
  }, [tabContextMnu]);

  const restoreModeState = useCallback(
    ({
      nextMode,
      nextLocale,
      nextDefaultTabId,
      nextInitialExpandedMnuIds,
      persistedState,
    }: RestoreModeStateParams) => {
      setOpenTabs(
        persistedState?.openTabs?.length
          ? cloneWspTabs(persistedState.openTabs)
          : [
              {
                id: nextDefaultTabId,
                title: findMnuTitle(nextMode, nextDefaultTabId, nextLocale),
              },
            ]
      );
      setActiveTabId(persistedState?.activeTabId ?? nextDefaultTabId);
      setExpandedMnuIds(nextInitialExpandedMnuIds);
      setTabContextMnu(null);
      simulateLoading(onLoadingChange);
    },
    [onLoadingChange]
  );

  return {
    tabScrollRef,
    openTabs,
    activeTabId,
    activePageId,
    expandedMnuIds,
    desktopTabs,
    showTabScrollControls,
    canScrollTabsLeft,
    canScrollTabsRight,
    tabContextMnu,
    getTabTitle,
    getTabContextMnuStyle,
    setActiveTabId,
    activateTab,
    openMnuAsTab,
    openProfileTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
    openTabContextMnu,
    scrollTabs,
    restoreModeState,
  };
}

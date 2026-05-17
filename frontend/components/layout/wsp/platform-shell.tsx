"use client";

import { useCallback, useEffect, useMemo } from "react";

import { WspBody } from "@/components/layout/wsp/wsp-body";
import { WspHdr } from "@/components/layout/wsp/wsp-hdr";
import { ToastContainer } from "@/components/ui/toast";
import { WspSidebar } from "@/components/layout/wsp/wsp-sidebar";
import { WspTabCtxMnu } from "@/components/layout/wsp/wsp-tab-ctx-mnu";
import { WspTabbar } from "@/components/layout/wsp/wsp-tabbar";
import { useWspMnus } from "@/components/layout/wsp/use-wsp-mnus";
import { useWspShellState } from "@/components/layout/wsp/use-wsp-shell-state";
import { useWspTabs } from "@/components/layout/wsp/use-wsp-tabs";
import { getLoginMsgs } from "@/lib/i18n/login-cnt";
import {
  getWspShellCnt,
  getWspTabCtxMnuCnt,
} from "@/lib/i18n/wsp-shell-cnt";
import { cn } from "@/lib/utils";
import {
  getLocalizedWspFallbackViewModel,
  getWspModeConfig,
} from "@/lib/utils/wsp/platform-shell-data";
import { PROFILE_TAB_ID } from "@/lib/utils/wsp/wsp-tab-utils";
import { writePersistedWspState } from "@/lib/utils/wsp/wsp-storage";

function getInitialExpandedMnuIds(
  mnus: ReturnType<typeof getWspModeConfig>["mnus"]
) {
  const firstExpandableMnu = mnus.find((mnu) => mnu.children?.length);
  if (!firstExpandableMnu) {
    return [] as string[];
  }

  return [firstExpandableMnu.id, firstExpandableMnu.children?.[0]?.id].filter(Boolean) as string[];
}

export function PlatformShell() {
  const {
    accessToken,
    handleLogout,
    handleUnauthorized,
    initialPersistedState,
    isReady,
    loading,
    locale,
    mode,
    restorePersistedState,
    setLoading,
    setLocale,
    setMode,
    setSidebarCollapsed,
    sidebarCollapsed,
    storageActorKey,
    currentUsr,
  } = useWspShellState();

  const localeMsgs = useMemo(() => getLoginMsgs(locale), [locale]);
  const shellCnt = useMemo(() => getWspShellCnt(locale), [locale]);
  const tabContextLabels = useMemo(() => getWspTabCtxMnuCnt(locale), [locale]);
  const config = useMemo(() => getWspModeConfig(mode, locale), [locale, mode]);
  const initialExpandedMnuIds = useMemo(() => getInitialExpandedMnuIds(config.mnus), [config.mnus]);

  const { currentMnus } = useWspMnus({
    accessToken,
    fallbackMnus: config.mnus,
    isReady,
    locale,
    mode,
    onUnauthorized: handleUnauthorized,
    roleCds: currentUsr?.roleCds,
  });

  const tabs = useWspTabs({
    currentMnus,
    defaultTabId: config.defaultTabId,
    initialExpandedMnuIds,
    initialPersistedState,
    isReady,
    locale,
    mode,
    onLoadingChange: setLoading,
    profileTabTitle: shellCnt.profileTab,
  });

  useEffect(() => {
    if (!isReady) return;

    writePersistedWspState(storageActorKey, mode, {
      mode,
      sidebarCollapsed,
      openTabs: tabs.openTabs,
      activeTabId: tabs.activeTabId,
    });
  }, [isReady, mode, sidebarCollapsed, storageActorKey, tabs.activeTabId, tabs.openTabs]);

  const handleModeChange = useCallback(
    (nextMode: "client" | "admin") => {
      if (nextMode === mode) return;

      const persistedState = restorePersistedState(nextMode);
      const nextConfig = getWspModeConfig(nextMode, locale);
      const nextInitialExpandedMnuIds = getInitialExpandedMnuIds(nextConfig.mnus);

      setMode(nextMode);
      setSidebarCollapsed(persistedState?.sidebarCollapsed ?? false);
      tabs.restoreModeState({
        nextMode,
        nextLocale: locale,
        nextDefaultTabId: nextConfig.defaultTabId,
        nextInitialExpandedMnuIds,
        persistedState,
      });
    },
    [locale, mode, restorePersistedState, setMode, setSidebarCollapsed, tabs]
  );

  const activeRuntimePageId =
    tabs.activePageId === PROFILE_TAB_ID ? null : tabs.activePageId;
  const activeFallbackView = useMemo(
    () => getLocalizedWspFallbackViewModel(tabs.activePageId, locale),
    [locale, tabs.activePageId]
  );

  if (!isReady) {
    return null;
  }

  return (
    <main
      id="wsp-shell"
      className="wsp-shell min-h-screen bg-[#eef1f4]"
    >
      <div
        id="wsp-global-loading-bar"
        className="fixed left-0 top-0 z-50 h-1 w-full bg-transparent"
      >
        <div
          className={cn(
            "h-full bg-[#31588f] transition-all duration-300",
            loading ? "w-full opacity-100" : "w-0 opacity-0"
          )}
        />
      </div>

      <div
        id="wsp-shell-cntr"
        className="mx-auto flex min-h-screen max-w-[1700px] flex-col px-2 pb-2 pt-1.5 lg:h-[100dvh] lg:min-h-0 lg:px-3 lg:pb-3 lg:pt-1.5"
      >
        <WspHdr
          locale={locale}
          localeLabel={localeMsgs.localeLabel}
          localeNames={localeMsgs.localeNames}
          mode={mode}
          shellCnt={shellCnt}
          shellTitle={config.shellTitle}
          onLocaleChange={setLocale}
          onModeChange={handleModeChange}
          onOpenProfile={tabs.openProfileTab}
          onLogout={handleLogout}
        />

        <div id="wsp-desktop-layout" className="flex min-h-0 flex-1 gap-2">
          <WspSidebar
            activeTabId={tabs.activeTabId}
            expandedMnuIds={tabs.expandedMnuIds}
            label={config.label}
            mnus={currentMnus}
            onMnuSelect={tabs.openMnuAsTab}
            onToggleCollapsed={() => setSidebarCollapsed((current) => !current)}
            sidebarCollapsed={sidebarCollapsed}
            shellCnt={shellCnt}
          />

          <section
            id="wsp-main-panel"
            className="relative flex min-w-0 min-h-0 flex-1 flex-col rounded-[4px] border border-slate-300 bg-[#f8f9fb]"
          >
            <WspTabbar
              activeTabId={tabs.activeTabId}
              canScrollTabsLeft={tabs.canScrollTabsLeft}
              canScrollTabsRight={tabs.canScrollTabsRight}
              defaultTabId={config.defaultTabId}
              desktopTabs={tabs.desktopTabs}
              getTabTitle={tabs.getTabTitle}
              onCloseTab={tabs.closeTab}
              onOpenContextMnu={tabs.openTabContextMnu}
              onScrollTabs={tabs.scrollTabs}
              onSelectTab={tabs.activateTab}
              showTabScrollControls={tabs.showTabScrollControls}
              tabScrollRef={tabs.tabScrollRef}
            />

            {tabs.tabContextMnu ? (
              <WspTabCtxMnu
                defaultTabId={config.defaultTabId}
                labels={tabContextLabels}
                onCloseAllTabs={tabs.closeAllTabs}
                onCloseOtherTabs={tabs.closeOtherTabs}
                onCloseTab={tabs.closeTab}
                style={tabs.getTabContextMnuStyle()}
                tabId={tabs.tabContextMnu.tabId}
              />
            ) : null}

            <WspBody
              activeRuntimePageId={activeRuntimePageId}
              accessToken={accessToken}
              desktopTabsCount={tabs.desktopTabs.length}
              locale={locale}
              mode={mode}
              onLoadingChange={setLoading}
              onUnauthorized={handleUnauthorized}
              shellCnt={shellCnt}
              sidebarCollapsed={sidebarCollapsed}
              view={activeFallbackView}
            />
          </section>
        </div>
      </div>
      <ToastContainer />
    </main>
  );
}

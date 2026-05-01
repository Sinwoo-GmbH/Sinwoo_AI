"use client";

import { useCallback, useEffect, useMemo } from "react";

import { WorkspaceBody } from "@/components/workspace/workspace-body";
import { WorkspaceHeader } from "@/components/workspace/workspace-header";
import { WorkspaceSidebar } from "@/components/workspace/workspace-sidebar";
import { WorkspaceTabContextMenu } from "@/components/workspace/workspace-tab-context-menu";
import { WorkspaceTabbar } from "@/components/workspace/workspace-tabbar";
import { useWorkspaceMenus } from "@/components/workspace/use-workspace-menus";
import { useWorkspaceShellState } from "@/components/workspace/use-workspace-shell-state";
import { useWorkspaceTabs } from "@/components/workspace/use-workspace-tabs";
import { getLoginMessages } from "@/lib/i18n/login-content";
import {
  getWorkspaceShellContent,
  getWorkspaceTabContextMenuContent,
} from "@/lib/i18n/workspace-shell-content";
import { cn } from "@/lib/utils";
import {
  getLocalizedWorkspaceFallbackViewModel,
  getWorkspaceModeConfig,
} from "@/lib/workspace/platform-shell-data";
import { PROFILE_TAB_ID } from "@/lib/workspace/workspace-tab-utils";
import { writePersistedWorkspaceState } from "@/lib/workspace/workspace-storage";

function getInitialExpandedMenuIds(
  menus: ReturnType<typeof getWorkspaceModeConfig>["menus"]
) {
  const firstExpandableMenu = menus.find((menu) => menu.children?.length);
  if (!firstExpandableMenu) {
    return [] as string[];
  }

  return [firstExpandableMenu.id, firstExpandableMenu.children?.[0]?.id].filter(Boolean) as string[];
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
    currentUser,
  } = useWorkspaceShellState();

  const localeMessages = useMemo(() => getLoginMessages(locale), [locale]);
  const shellContent = useMemo(() => getWorkspaceShellContent(locale), [locale]);
  const tabContextLabels = useMemo(() => getWorkspaceTabContextMenuContent(locale), [locale]);
  const config = useMemo(() => getWorkspaceModeConfig(mode, locale), [locale, mode]);
  const initialExpandedMenuIds = useMemo(() => getInitialExpandedMenuIds(config.menus), [config.menus]);

  const { currentMenus } = useWorkspaceMenus({
    accessToken,
    fallbackMenus: config.menus,
    isReady,
    locale,
    mode,
    onUnauthorized: handleUnauthorized,
    roleCds: currentUser?.roleCds,
  });

  const tabs = useWorkspaceTabs({
    currentMenus,
    defaultTabId: config.defaultTabId,
    initialExpandedMenuIds,
    initialPersistedState,
    isReady,
    locale,
    mode,
    onLoadingChange: setLoading,
    profileTabTitle: shellContent.profileTab,
  });

  useEffect(() => {
    if (!isReady) return;

    writePersistedWorkspaceState(storageActorKey, mode, {
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
      const nextConfig = getWorkspaceModeConfig(nextMode, locale);
      const nextInitialExpandedMenuIds = getInitialExpandedMenuIds(nextConfig.menus);

      setMode(nextMode);
      setSidebarCollapsed(persistedState?.sidebarCollapsed ?? false);
      tabs.restoreModeState({
        nextMode,
        nextLocale: locale,
        nextDefaultTabId: nextConfig.defaultTabId,
        nextInitialExpandedMenuIds,
        persistedState,
      });
    },
    [locale, mode, restorePersistedState, setMode, setSidebarCollapsed, tabs]
  );

  const activeRuntimePageId =
    tabs.activePageId === PROFILE_TAB_ID ? null : tabs.activePageId;
  const activeFallbackView = useMemo(
    () => getLocalizedWorkspaceFallbackViewModel(tabs.activePageId, locale),
    [locale, tabs.activePageId]
  );

  if (!isReady) {
    return null;
  }

  return (
    <main
      id="workspace-shell"
      className="workspace-shell min-h-screen bg-[#eef1f4]"
    >
      <div
        id="workspace-global-loading-bar"
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
        id="workspace-shell-container"
        className="mx-auto flex min-h-screen max-w-[1700px] flex-col px-2 pb-2 pt-1.5 lg:h-[100dvh] lg:min-h-0 lg:px-3 lg:pb-3 lg:pt-1.5"
      >
        <WorkspaceHeader
          locale={locale}
          localeLabel={localeMessages.localeLabel}
          localeNames={localeMessages.localeNames}
          mode={mode}
          shellContent={shellContent}
          shellTitle={config.shellTitle}
          onLocaleChange={setLocale}
          onModeChange={handleModeChange}
          onOpenProfile={tabs.openProfileTab}
          onLogout={handleLogout}
        />

        <div id="workspace-desktop-layout" className="flex min-h-0 flex-1 gap-2">
          <WorkspaceSidebar
            activeTabId={tabs.activeTabId}
            expandedMenuIds={tabs.expandedMenuIds}
            label={config.label}
            menus={currentMenus}
            onMenuSelect={tabs.openMenuAsTab}
            onToggleCollapsed={() => setSidebarCollapsed((current) => !current)}
            sidebarCollapsed={sidebarCollapsed}
            shellContent={shellContent}
          />

          <section
            id="workspace-main-panel"
            className="relative flex min-w-0 min-h-0 flex-1 flex-col rounded-[4px] border border-slate-300 bg-[#f8f9fb]"
          >
            <WorkspaceTabbar
              activeTabId={tabs.activeTabId}
              canScrollTabsLeft={tabs.canScrollTabsLeft}
              canScrollTabsRight={tabs.canScrollTabsRight}
              defaultTabId={config.defaultTabId}
              desktopTabs={tabs.desktopTabs}
              getTabTitle={tabs.getTabTitle}
              onCloseTab={tabs.closeTab}
              onOpenContextMenu={tabs.openTabContextMenu}
              onScrollTabs={tabs.scrollTabs}
              onSelectTab={tabs.activateTab}
              showTabScrollControls={tabs.showTabScrollControls}
              tabScrollRef={tabs.tabScrollRef}
            />

            {tabs.tabContextMenu ? (
              <WorkspaceTabContextMenu
                defaultTabId={config.defaultTabId}
                labels={tabContextLabels}
                onCloseAllTabs={tabs.closeAllTabs}
                onCloseOtherTabs={tabs.closeOtherTabs}
                onCloseTab={tabs.closeTab}
                style={tabs.getTabContextMenuStyle()}
                tabId={tabs.tabContextMenu.tabId}
              />
            ) : null}

            <WorkspaceBody
              activeRuntimePageId={activeRuntimePageId}
              accessToken={accessToken}
              desktopTabsCount={tabs.desktopTabs.length}
              locale={locale}
              mode={mode}
              onLoadingChange={setLoading}
              onUnauthorized={handleUnauthorized}
              shellContent={shellContent}
              sidebarCollapsed={sidebarCollapsed}
              view={activeFallbackView}
            />
          </section>
        </div>
      </div>
    </main>
  );
}

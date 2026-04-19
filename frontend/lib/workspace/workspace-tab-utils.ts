import type { TabItem } from "@/lib/workspace/platform-shell-data";

export const PROFILE_TAB_ID = "my-profile";
export const TAB_CONTEXT_MENU_WIDTH = 164;

export function cloneWorkspaceTabs(tabs: TabItem[]) {
  return tabs.map((tab) => ({ ...tab }));
}

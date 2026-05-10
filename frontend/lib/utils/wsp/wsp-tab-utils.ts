import type { TabItem } from "@/lib/utils/wsp/platform-shell-data";

export const PROFILE_TAB_ID = "my-profile";
export const TAB_CTX_MNU_WIDTH = 164;

export function cloneWspTabs(tabs: TabItem[]) {
  return tabs.map((tab) => ({ ...tab }));
}

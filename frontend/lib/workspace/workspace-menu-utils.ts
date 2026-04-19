import type { LoginLocale } from "@/lib/i18n/login-content";
import {
  findMenuTitle,
  getFallbackMenuPresentation,
  type MenuNode,
  type WorkspaceMode,
} from "@/lib/workspace/platform-shell-data";
import {
  CLIENT_MENU_RUNTIME_REDIRECTS,
  MENU_ID_COMPAT_ALIASES,
  REVERSE_MENU_ID_COMPAT_ALIASES,
} from "@/lib/workspace/workspace-menu-compat";
import type { MenuNodeRes } from "@/lib/api/menu-contract";

const CUSTOMER_ADMIN_ROLE_IDS = [
  "ROLE_CUSTOMER_ADMIN_MEMBER",
  "ROLE_CUSTOMER_ADMIN_LEADER",
  "ROLE_PLATFORM_SUPER_ADMIN",
] as const;

type ClientRuntimeChildDefinition = {
  id: string;
  sourceIds: readonly string[];
  requiresCustomerAdmin?: boolean;
  alwaysShow?: boolean;
};

type ClientRuntimeGroupDefinition = {
  id: string;
  childList: readonly ClientRuntimeChildDefinition[];
  requiresCustomerAdmin?: boolean;
};

const CLIENT_RUNTIME_MENU_GROUPS: readonly ClientRuntimeGroupDefinition[] = [
  {
    id: "MNU_CUSTOMER_ATTENDANCE",
    childList: [
      {
        id: "MNU_CUSTOMER_MY_TIME",
        sourceIds: ["MNU_CUSTOMER_MY_TIME", "my-time"],
        alwaysShow: true,
      },
      {
        id: "MNU_CUSTOMER_TEAM_TIME",
        sourceIds: ["MNU_CUSTOMER_TEAM_TIME", "team-time"],
        requiresCustomerAdmin: true,
        alwaysShow: true,
      },
    ],
  },
  {
    id: "MNU_CUSTOMER_REPORTS",
    childList: [
      {
        id: "MNU_CUSTOMER_WORK_TIME",
        sourceIds: ["MNU_CUSTOMER_WORK_TIME", "MNU_CUSTOMER_WORK_TIME_HISTORY"],
        alwaysShow: true,
      },
    ],
  },
  {
    id: "MNU_CUSTOMER_ADMIN",
    requiresCustomerAdmin: true,
    childList: [
      {
        id: "MNU_CUSTOMER_ADMIN_HOME",
        sourceIds: [],
        requiresCustomerAdmin: true,
        alwaysShow: true,
      },
    ],
  },
] as const;

export function hasWorkspaceCustomerAdminAccess(roleCds: readonly string[] = []) {
  return roleCds.some((roleCd) =>
    (CUSTOMER_ADMIN_ROLE_IDS as readonly string[]).includes(roleCd)
  );
}

export function findMenuTitleFromNodes(menus: MenuNode[], targetId: string): string | null {
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

export function findMenuTrailIds(
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

export function menuExists(menus: MenuNode[], targetId: string) {
  return Boolean(findMenuTitleFromNodes(menus, targetId));
}

export function canonicalizeMenuId(menus: MenuNode[], targetId: string) {
  const redirected = CLIENT_MENU_RUNTIME_REDIRECTS[targetId];
  if (redirected && menuExists(menus, redirected)) {
    return redirected;
  }

  if (menuExists(menus, targetId)) {
    return targetId;
  }

  const alias = MENU_ID_COMPAT_ALIASES[targetId];
  if (alias && menuExists(menus, alias)) {
    return alias;
  }

  return targetId;
}

export function resolveFallbackMenuId(targetId: string) {
  return CLIENT_MENU_RUNTIME_REDIRECTS[targetId] ?? REVERSE_MENU_ID_COMPAT_ALIASES[targetId] ?? targetId;
}

export function resolveWorkspaceTabTitle(
  menus: MenuNode[],
  mode: WorkspaceMode,
  locale: LoginLocale,
  tabId: string,
  profileTabId: string,
  profileTabTitle: string,
  fallbackTitle?: string
) {
  if (tabId === profileTabId) return profileTabTitle;

  const resolvedTitle = findMenuTitleFromNodes(menus, tabId);
  if (resolvedTitle) {
    return resolvedTitle;
  }

  const fallbackMenuId = resolveFallbackMenuId(tabId);
  const fallbackMenuTitle = findMenuTitle(mode, fallbackMenuId, locale);
  if (fallbackMenuTitle !== fallbackMenuId) {
    return fallbackMenuTitle;
  }

  return fallbackTitle ?? tabId;
}

// Developer note:
// Backend menu structure is primary. Frontend fallback only contributes
// presentation metadata when icon information is absent.
export function normalizeWorkspaceApiMenus(items: MenuNodeRes[]): MenuNode[] {
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

function findFirstMenuNodeByIds(menus: MenuNode[], ids: readonly string[]): MenuNode | null {
  for (const menu of menus) {
    if (ids.includes(menu.id)) {
      return menu;
    }

    if (menu.children?.length) {
      const hit = findFirstMenuNodeByIds(menu.children, ids);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

function buildClientRuntimeLeafNode(
  childDefinition: ClientRuntimeChildDefinition,
  menus: MenuNode[],
  locale: LoginLocale,
  sourceNode?: MenuNode | null
): MenuNode | null {
  const resolvedSourceNode =
    sourceNode ??
    (childDefinition.sourceIds.length
      ? findFirstMenuNodeByIds(menus, childDefinition.sourceIds)
      : null);
  if (!resolvedSourceNode && !childDefinition.alwaysShow) {
    return null;
  }

  const fallbackPresentation = getFallbackMenuPresentation(childDefinition.id);
  return {
    id: childDefinition.id,
    title: resolvedSourceNode?.title ?? findMenuTitle("client", childDefinition.id, locale),
    icon: resolvedSourceNode?.icon ?? fallbackPresentation?.icon,
    closable: resolvedSourceNode?.closable ?? fallbackPresentation?.closable,
  };
}

export function projectClientRuntimeMenus(
  menus: MenuNode[],
  locale: LoginLocale,
  roleCds: readonly string[] = []
): MenuNode[] {
  const customerAdminAccess = hasWorkspaceCustomerAdminAccess(roleCds);

  return CLIENT_RUNTIME_MENU_GROUPS.map((groupDefinition) => {
    const sourceNode = findFirstMenuNodeByIds(menus, [groupDefinition.id]);
    if (groupDefinition.requiresCustomerAdmin && !customerAdminAccess && !sourceNode) {
      return null;
    }

    const childList = groupDefinition.childList
      .map((childDefinition) => {
        const childSourceNode = childDefinition.sourceIds.length
          ? findFirstMenuNodeByIds(menus, childDefinition.sourceIds)
          : null;

        if (childDefinition.requiresCustomerAdmin && !customerAdminAccess && !childSourceNode) {
          return null;
        }

        return buildClientRuntimeLeafNode(childDefinition, menus, locale, childSourceNode);
      })
      .filter(Boolean) as MenuNode[];

    if (!childList.length) {
      return null;
    }

    const fallbackPresentation = getFallbackMenuPresentation(groupDefinition.id);

    return {
      id: groupDefinition.id,
      title: sourceNode?.title ?? findMenuTitle("client", groupDefinition.id, locale),
      icon: sourceNode?.icon ?? fallbackPresentation?.icon,
      closable: false,
      children: childList,
    } satisfies MenuNode;
  }).filter(Boolean) as MenuNode[];
}

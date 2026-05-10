import type { LoginLocale } from "@/lib/i18n/login-cnt";
import {
  findMnuTitle,
  getFallbackMnuPresentation,
  type MnuNode,
  type WspMode,
} from "@/lib/utils/wsp/platform-shell-data";
import {
  CLIENT_MNU_RUNTIME_REDIRECTS,
  MNU_ID_COMPAT_ALIASES,
  REVERSE_MNU_ID_COMPAT_ALIASES,
} from "@/lib/utils/wsp/wsp-mnu-compat";
import type { MnuNodeResponse } from "@/lib/api/mnu-contract";

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

const CLIENT_RUNTIME_ROOT_MNUS: readonly ClientRuntimeChildDefinition[] = [
  {
    id: "MNU_CUSTOMER_DASH",
    sourceIds: ["MNU_CUSTOMER_DASH", "client-dashboard"],
    alwaysShow: true,
  },
] as const;

type ClientRuntimeGroupDefinition = {
  id: string;
  childList: readonly ClientRuntimeChildDefinition[];
  requiresCustomerAdmin?: boolean;
};

const CLIENT_RUNTIME_MNU_GROUPS: readonly ClientRuntimeGroupDefinition[] = [
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
      {
        id: "MNU_CUSTOMER_LEAVE",
        sourceIds: ["MNU_CUSTOMER_LEAVE", "leave"],
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

export function hasWspCustomerAdminAccess(roleCds: readonly string[] = []) {
  return roleCds.some((roleCd) =>
    (CUSTOMER_ADMIN_ROLE_IDS as readonly string[]).includes(roleCd)
  );
}

export function findMnuTitleFromNodes(mnus: MnuNode[], targetId: string): string | null {
  for (const mnu of mnus) {
    if (mnu.id === targetId) {
      return mnu.title;
    }

    if (mnu.children?.length) {
      const hit = findMnuTitleFromNodes(mnu.children, targetId);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

export function findMnuTrailIds(
  mnus: MnuNode[],
  targetId: string,
  trail: string[] = []
): string[] | null {
  for (const mnu of mnus) {
    const nextTrail = [...trail, mnu.id];

    if (mnu.id === targetId) {
      return nextTrail;
    }

    if (mnu.children?.length) {
      const hit = findMnuTrailIds(mnu.children, targetId, nextTrail);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

export function mnuExists(mnus: MnuNode[], targetId: string) {
  return Boolean(findMnuTitleFromNodes(mnus, targetId));
}

export function canonicalizeMnuId(mnus: MnuNode[], targetId: string) {
  const redirected = CLIENT_MNU_RUNTIME_REDIRECTS[targetId];
  if (redirected && mnuExists(mnus, redirected)) {
    return redirected;
  }

  if (mnuExists(mnus, targetId)) {
    return targetId;
  }

  const alias = MNU_ID_COMPAT_ALIASES[targetId];
  if (alias && mnuExists(mnus, alias)) {
    return alias;
  }

  return targetId;
}

export function resolveFallbackMnuId(targetId: string) {
  return CLIENT_MNU_RUNTIME_REDIRECTS[targetId] ?? REVERSE_MNU_ID_COMPAT_ALIASES[targetId] ?? targetId;
}

export function resolveWspTabTitle(
  mnus: MnuNode[],
  mode: WspMode,
  locale: LoginLocale,
  tabId: string,
  profileTabId: string,
  profileTabTitle: string,
  fallbackTitle?: string
) {
  if (tabId === profileTabId) return profileTabTitle;

  const resolvedTitle = findMnuTitleFromNodes(mnus, tabId);
  if (resolvedTitle) {
    return resolvedTitle;
  }

  const fallbackMnuId = resolveFallbackMnuId(tabId);
  const fallbackMnuTitle = findMnuTitle(mode, fallbackMnuId, locale);
  if (fallbackMnuTitle !== fallbackMnuId) {
    return fallbackMnuTitle;
  }

  return fallbackTitle ?? tabId;
}

// Developer note:
// Backend mnu structure is primary. Frontend fallback only contributes
// presentation metadata when icon information is absent.
export function normalizeWspApiMnus(items: MnuNodeResponse[]): MnuNode[] {
  const walk = (nodes: MnuNodeResponse[]): MnuNode[] =>
    nodes.map((node) => {
      const fallbackPresentation = getFallbackMnuPresentation(node.mnuCd);
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

function findFirstMnuNodeByIds(mnus: MnuNode[], ids: readonly string[]): MnuNode | null {
  for (const mnu of mnus) {
    if (ids.includes(mnu.id)) {
      return mnu;
    }

    if (mnu.children?.length) {
      const hit = findFirstMnuNodeByIds(mnu.children, ids);
      if (hit) {
        return hit;
      }
    }
  }

  return null;
}

function buildClientRuntimeLeafNode(
  childDefinition: ClientRuntimeChildDefinition,
  mnus: MnuNode[],
  locale: LoginLocale,
  sourceNode?: MnuNode | null
): MnuNode | null {
  const resolvedSourceNode =
    sourceNode ??
    (childDefinition.sourceIds.length
      ? findFirstMnuNodeByIds(mnus, childDefinition.sourceIds)
      : null);
  if (!resolvedSourceNode && !childDefinition.alwaysShow) {
    return null;
  }

  const fallbackPresentation = getFallbackMnuPresentation(childDefinition.id);
  return {
    id: childDefinition.id,
    title: resolvedSourceNode?.title ?? findMnuTitle("client", childDefinition.id, locale),
    icon: resolvedSourceNode?.icon ?? fallbackPresentation?.icon,
    closable: resolvedSourceNode?.closable ?? fallbackPresentation?.closable,
  };
}

export function projectClientRuntimeMnus(
  mnus: MnuNode[],
  locale: LoginLocale,
  roleCds: readonly string[] = []
): MnuNode[] {
  if (mnus.length) {
    return mnus;
  }

  const customerAdminAccess = hasWspCustomerAdminAccess(roleCds);
  const rootMnus = CLIENT_RUNTIME_ROOT_MNUS.map((mnuDefinition) =>
    buildClientRuntimeLeafNode(mnuDefinition, mnus, locale)
  ).filter(Boolean) as MnuNode[];

  const groupMnus = CLIENT_RUNTIME_MNU_GROUPS.map((groupDefinition) => {
    const sourceNode = findFirstMnuNodeByIds(mnus, [groupDefinition.id]);
    if (groupDefinition.requiresCustomerAdmin && !customerAdminAccess && !sourceNode) {
      return null;
    }

    const childList = groupDefinition.childList
      .map((childDefinition) => {
        const childSourceNode = childDefinition.sourceIds.length
          ? findFirstMnuNodeByIds(mnus, childDefinition.sourceIds)
          : null;

        if (childDefinition.requiresCustomerAdmin && !customerAdminAccess && !childSourceNode) {
          return null;
        }

        return buildClientRuntimeLeafNode(childDefinition, mnus, locale, childSourceNode);
      })
      .filter(Boolean) as MnuNode[];

    if (!childList.length) {
      return null;
    }

    const fallbackPresentation = getFallbackMnuPresentation(groupDefinition.id);

    return {
      id: groupDefinition.id,
      title: sourceNode?.title ?? findMnuTitle("client", groupDefinition.id, locale),
      icon: sourceNode?.icon ?? fallbackPresentation?.icon,
      closable: false,
      children: childList,
    } satisfies MnuNode;
  }).filter(Boolean) as MnuNode[];

  return [...rootMnus, ...groupMnus];
}

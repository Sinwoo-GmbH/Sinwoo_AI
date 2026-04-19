import type { LoginLocale } from "@/lib/i18n/login-content";

export type WorkspaceShellContent = {
  eyebrow: string;
  client: string;
  admin: string;
  navigation: string;
  navShort: string;
  profileTab: string;
  quickActions: string;
  ruleOne: string;
  ruleTwo: string;
  ruleThree: string;
  modeLabel: string;
  sidebarLabel: string;
  tabsLabel: string;
  modeAdminValue: string;
  modeClientValue: string;
  collapsed: string;
  expanded: string;
  focusTitle: string;
  focusDescription: string;
  tableDescription: string;
  name: string;
  owner: string;
  status: string;
  updated: string;
};

export type WorkspaceTabContextMenuContent = {
  close: string;
  closeOther: string;
  closeAll: string;
};

const workspaceShellContent: Record<LoginLocale, WorkspaceShellContent> = {
  en: {
    eyebrow: "OneGate workspace shell",
    client: "Client",
    admin: "Admin",
    navigation: "Navigation",
    navShort: "Nav",
    profileTab: "My Profile",
    quickActions: "Quick actions",
    ruleOne: "Admins can start in client mode and switch to admin mode when needed.",
    ruleTwo: "Menus support four depths and are designed for admin-side configuration.",
    ruleThree: "Desktop uses tabs and keeps the active workspace visible at all times.",
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
  },
  de: {
    eyebrow: "OneGate workspace shell",
    client: "Client",
    admin: "Admin",
    navigation: "Navigation",
    navShort: "Nav",
    profileTab: "Persönliche Daten",
    quickActions: "Schnellaktionen",
    ruleOne: "Administratoren können im Kundenmodus starten und danach in den Admin-Modus wechseln.",
    ruleTwo: "Menüs unterstützen bis zu vier Ebenen und werden über die Admin-Seite verwaltet.",
    ruleThree: "Desktop verwendet Tabs und hält den aktiven Arbeitsbereich jederzeit sichtbar.",
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
  },
  ko: {
    eyebrow: "OneGate workspace shell",
    client: "고객",
    admin: "관리자",
    navigation: "메뉴",
    navShort: "메뉴",
    profileTab: "개인정보변경",
    quickActions: "빠른 작업",
    ruleOne: "관리자는 먼저 고객 모드로 진입한 뒤 필요 시 관리자 모드로 전환합니다.",
    ruleTwo: "메뉴는 최대 4뎁스를 지원하며 관리자 페이지에서 관리되는 구조를 전제로 합니다.",
    ruleThree: "데스크톱은 탭 중심으로 구성되어 현재 작업 중인 화면을 항상 유지합니다.",
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
  },
};

const workspaceTabContextMenuContent: Record<LoginLocale, WorkspaceTabContextMenuContent> = {
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
    close: "닫기",
    closeOther: "다른 탭 닫기",
    closeAll: "모든 탭 닫기",
  },
};

export function getWorkspaceShellContent(locale: LoginLocale): WorkspaceShellContent {
  return workspaceShellContent[locale];
}

export function getWorkspaceTabContextMenuContent(
  locale: LoginLocale
): WorkspaceTabContextMenuContent {
  return workspaceTabContextMenuContent[locale];
}

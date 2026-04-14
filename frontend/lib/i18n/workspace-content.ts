import type { LoginLocale } from "@/lib/i18n/login-content";

export type WorkspaceShellMessages = {
  eyebrow: string;
  client: string;
  admin: string;
  navigation: string;
  navShort: string;
  logout: string;
  profileTab: string;
  badgeClient: string;
  badgeAdmin: string;
  localState: string;
  quickActions: string;
  rulesTitle: string;
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
  mobileWeb: string;
  mobileFocusTitle: string;
  mobileFocusDescription: string;
};

export type WorkspaceTabContextMenuLabels = {
  close: string;
  closeOther: string;
  closeAll: string;
};

export type WorkspaceAttendanceMessages = {
  eyebrow: string;
  title: string;
  loading: string;
  retry: string;
  weekdays: string[];
  manualDate: string;
  manualCheckIn: string;
  manualCheckOut: string;
  manualSaveIn: string;
  manualSaveOut: string;
  leaveChip: string;
  businessTripChip: string;
};

export type WorkspaceWorkTimeHistoryMessages = {
  eyebrow: string;
  title: string;
  description: string;
  currentScope: string;
  ownOnly: string;
  scopedAll: string;
  month: string;
  employee: string;
  department: string;
  keyword: string;
  allEmployees: string;
  allDepartments: string;
  keywordPlaceholder: string;
  search: string;
  reset: string;
  exportExcel: string;
  exportPdf: string;
  total: string;
  rows: string;
  filtersTitle: string;
  filtersDescription: string;
  resultsTitle: string;
  date: string;
  status: string;
  checkIn: string;
  checkOut: string;
  workTime: string;
  noDataTitle: string;
  noDataDescription: string;
  modeAdmin: string;
  modeClient: string;
};

const workspaceShellMessages: Record<LoginLocale, WorkspaceShellMessages> = {
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
    tableDescription:
      "Desktop web should feel like a real browser workspace, not a mobile card stack.",
    name: "Name",
    owner: "Owner",
    status: "Status",
    updated: "Updated",
    mobileWeb: "Mobile web",
    mobileFocusTitle: "Mobile focus",
    mobileFocusDescription:
      "On mobile web, tabs are removed and the current menu becomes the main view.",
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
    ruleOne:
      "Administratoren können im Kundenmodus starten und danach in den Admin-Modus wechseln.",
    ruleTwo:
      "Menüs unterstützen bis zu vier Ebenen und werden über die Admin-Seite verwaltet.",
    ruleThree:
      "Desktop verwendet Tabs. Mobile Web reduziert die Oberfläche auf eine aktive Ansicht.",
    modeLabel: "Modus",
    sidebarLabel: "Sidebar",
    tabsLabel: "Offene Tabs",
    modeAdminValue: "Admin-Steuerung",
    modeClientValue: "Kundenportal",
    collapsed: "Eingeklappt",
    expanded: "Erweitert",
    focusTitle: "Prioritätsfluss",
    focusDescription: "Diese Karten zeigen Warteschlangen, Warnungen und Upgrade-Auslöser.",
    tableDescription:
      "Desktop-Web soll wie ein ernsthafter Arbeitsbereich wirken, nicht wie ein mobiler Kartenstapel.",
    name: "Name",
    owner: "Verantwortlich",
    status: "Status",
    updated: "Aktualisiert",
    mobileWeb: "Mobile Web",
    mobileFocusTitle: "Mobile Fokusansicht",
    mobileFocusDescription:
      "Im Mobile Web werden Tabs entfernt. Die aktive Auswahl bleibt die einzige Arbeitsansicht.",
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
    ruleOne:
      "관리자는 먼저 고객 모드로 진입한 뒤 필요 시 관리자 모드로 전환합니다.",
    ruleTwo:
      "메뉴는 최대 4뎁스를 지원하며 관리자 페이지에서 관리되는 구조를 전제로 합니다.",
    ruleThree:
      "데스크톱은 탭을 사용하고, 모바일 웹은 하나의 활성 화면 중심으로 단순화합니다.",
    modeLabel: "모드",
    sidebarLabel: "사이드바",
    tabsLabel: "열린 탭",
    modeAdminValue: "관리자 제어",
    modeClientValue: "고객 포털",
    collapsed: "접힘",
    expanded: "펼침",
    focusTitle: "우선 처리 흐름",
    focusDescription: "큐, 경고, 업그레이드 유도 항목을 이 카드 영역에 배치합니다.",
    tableDescription:
      "데스크톱 웹은 모바일 카드 화면이 아니라 진짜 업무용 브라우저 워크스페이스처럼 보여야 합니다.",
    name: "이름",
    owner: "담당",
    status: "상태",
    updated: "갱신",
    mobileWeb: "모바일 웹",
    mobileFocusTitle: "모바일 집중 화면",
    mobileFocusDescription:
      "모바일 웹에서는 탭을 제거하고 현재 선택한 화면 하나에 집중합니다.",
  },
};

const workspaceTabContextMenuLabels: Record<LoginLocale, WorkspaceTabContextMenuLabels> = {
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

const workspaceAttendanceMessages: Record<LoginLocale, WorkspaceAttendanceMessages> = {
  en: {
    eyebrow: "attendance desk",
    title: "Calendar",
    loading: "Loading attendance...",
    retry: "Unable to load attendance data.",
    weekdays: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
    manualDate: "Date",
    manualCheckIn: "Check-in",
    manualCheckOut: "Check-out",
    manualSaveIn: "Save in",
    manualSaveOut: "Save out",
    leaveChip: "Leave",
    businessTripChip: "Trip",
  },
  de: {
    eyebrow: "arbeitszeit",
    title: "Kalender",
    loading: "Arbeitszeit wird geladen...",
    retry: "Arbeitszeitdaten konnten nicht geladen werden.",
    weekdays: ["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"],
    manualDate: "Datum",
    manualCheckIn: "Start",
    manualCheckOut: "Ende",
    manualSaveIn: "Start speichern",
    manualSaveOut: "Ende speichern",
    leaveChip: "Urlaub",
    businessTripChip: "Reise",
  },
  ko: {
    eyebrow: "출퇴근 기록",
    title: "캘린더",
    loading: "출퇴근 정보를 불러오는 중입니다.",
    retry: "출퇴근 정보를 불러오지 못했습니다.",
    weekdays: ["월", "화", "수", "목", "금", "토", "일"],
    manualDate: "일자",
    manualCheckIn: "출근",
    manualCheckOut: "퇴근",
    manualSaveIn: "출근 저장",
    manualSaveOut: "퇴근 저장",
    leaveChip: "휴가",
    businessTripChip: "출장",
  },
};

const workspaceWorkTimeHistoryMessages: Record<LoginLocale, WorkspaceWorkTimeHistoryMessages> = {
  en: {
    eyebrow: "reports",
    title: "Work time history",
    description:
      "Review monthly work records, filter by employee or department, and export the result for reporting usage.",
    currentScope: "Scope",
    ownOnly: "Own records only",
    scopedAll: "All employees in scope",
    month: "Target month",
    employee: "Employee",
    department: "Department",
    keyword: "Keyword",
    allEmployees: "All employees",
    allDepartments: "All departments",
    keywordPlaceholder: "Name, department, status, or time",
    search: "Search",
    reset: "Reset",
    exportExcel: "Export Excel",
    exportPdf: "Export PDF",
    total: "Total",
    rows: "rows",
    filtersTitle: "Filters",
    filtersDescription:
      "Narrow the month and employee scope before exporting or reviewing work records.",
    resultsTitle: "History records",
    date: "Date",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Work time",
    noDataTitle: "No records found",
    noDataDescription:
      "Adjust the filters or switch the month to review another work history range.",
    modeAdmin: "Admin",
    modeClient: "Client",
  },
  de: {
    eyebrow: "berichte",
    title: "Arbeitszeitverlauf",
    description:
      "Monatliche Arbeitszeitdaten prüfen, nach Mitarbeiter oder Abteilung filtern und für Berichte exportieren.",
    currentScope: "Bereich",
    ownOnly: "Nur eigene Einträge",
    scopedAll: "Alle Mitarbeiter im Bereich",
    month: "Monat",
    employee: "Mitarbeiter",
    department: "Abteilung",
    keyword: "Stichwort",
    allEmployees: "Alle Mitarbeiter",
    allDepartments: "Alle Abteilungen",
    keywordPlaceholder: "Name, Abteilung, Status oder Zeit",
    search: "Suchen",
    reset: "Zurücksetzen",
    exportExcel: "Excel exportieren",
    exportPdf: "PDF exportieren",
    total: "Gesamt",
    rows: "Zeilen",
    filtersTitle: "Filter",
    filtersDescription:
      "Monat und Mitarbeiterbereich eingrenzen, bevor Arbeitszeitdaten geprüft oder exportiert werden.",
    resultsTitle: "Verlaufsdaten",
    date: "Datum",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Arbeitszeit",
    noDataTitle: "Keine Datensätze gefunden",
    noDataDescription:
      "Passen Sie die Filter an oder wechseln Sie den Monat, um einen anderen Zeitraum zu prüfen.",
    modeAdmin: "Admin",
    modeClient: "Client",
  },
  ko: {
    eyebrow: "리포트",
    title: "근태 이력",
    description:
      "월간 근태 기록을 직원·부서별로 조회하고, 보고용으로 Excel 또는 PDF로 내보낼 수 있습니다.",
    currentScope: "조회 범위",
    ownOnly: "본인 기록만",
    scopedAll: "권한 범위 내 전체 직원",
    month: "대상 월",
    employee: "직원",
    department: "부서",
    keyword: "키워드",
    allEmployees: "전체 직원",
    allDepartments: "전체 부서",
    keywordPlaceholder: "이름, 부서, 상태, 시간",
    search: "조회",
    reset: "초기화",
    exportExcel: "Excel 내보내기",
    exportPdf: "PDF 내보내기",
    total: "총",
    rows: "건",
    filtersTitle: "필터",
    filtersDescription:
      "대상 월과 직원/부서 범위를 조정한 뒤 근태 기록을 확인하거나 내보낼 수 있습니다.",
    resultsTitle: "기록 목록",
    date: "일자",
    status: "상태",
    checkIn: "출근",
    checkOut: "퇴근",
    workTime: "근무시간",
    noDataTitle: "조회된 기록이 없습니다",
    noDataDescription:
      "필터를 조정하거나 대상 월을 바꿔 다른 기간을 확인해 주세요.",
    modeAdmin: "관리자",
    modeClient: "고객",
  },
};

export function getWorkspaceShellMessages(locale: LoginLocale): WorkspaceShellMessages {
  return workspaceShellMessages[locale];
}

export function getWorkspaceTabContextMenuLabels(
  locale: LoginLocale
): WorkspaceTabContextMenuLabels {
  return workspaceTabContextMenuLabels[locale];
}

export function getWorkspaceAttendanceMessages(
  locale: LoginLocale
): WorkspaceAttendanceMessages {
  return workspaceAttendanceMessages[locale];
}

export function getWorkspaceWorkTimeHistoryMessages(
  locale: LoginLocale
): WorkspaceWorkTimeHistoryMessages {
  return workspaceWorkTimeHistoryMessages[locale];
}

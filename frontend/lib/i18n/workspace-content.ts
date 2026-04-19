import type { LoginLocale } from "@/lib/i18n/login-content";

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

export type WorkspaceMyWorkTimeMessages = {
  title: string;
  sectionTitle: string;
  pointList: string[];
};

export type WorkspaceTeamWorkTimeMessages = {
  title: string;
  tableTitle: string;
  employee: string;
  department: string;
  status: string;
  checkIn: string;
  checkOut: string;
  placeholderTitle: string;
  placeholderDescription: string;
};

export type WorkspaceAdminAreaMessages = {
  title: string;
  sectionTitle: string;
  placeholderTitle: string;
  placeholderDescription: string;
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

const workspaceMyWorkTimeMessages: Record<LoginLocale, WorkspaceMyWorkTimeMessages> = {
  en: {
    title: "My Work Time",
    sectionTitle: "Coverage",
    pointList: [
      "Review personal check-in and check-out entries.",
      "Keep holiday and leave markers visible in the monthly calendar.",
      "Use this area as the default personal attendance workspace.",
    ],
  },
  de: {
    title: "Meine Arbeitszeit",
    sectionTitle: "Abdeckung",
    pointList: [
      "Eigene Check-in- und Check-out-Einträge prüfen.",
      "Feiertage und Urlaubsmarkierungen im Monatskalender sichtbar halten.",
      "Diesen Bereich als persönlichen Standard für die Zeiterfassung verwenden.",
    ],
  },
  ko: {
    title: "내 근무시간",
    sectionTitle: "안내",
    pointList: [
      "개인 출근·퇴근 기록을 확인합니다.",
      "월간 캘린더에서 휴일과 휴가 표시를 함께 봅니다.",
      "이 영역을 개인 근태 기본 화면으로 사용합니다.",
    ],
  },
};

const workspaceTeamWorkTimeMessages: Record<LoginLocale, WorkspaceTeamWorkTimeMessages> = {
  en: {
    title: "Team Work Time",
    tableTitle: "Team overview",
    employee: "Employee",
    department: "Department",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    placeholderTitle: "Team work time view is ready for expansion",
    placeholderDescription:
      "Use this section for department-level attendance review, exception tracking, and future operational filters.",
  },
  de: {
    title: "Team Arbeitszeit",
    tableTitle: "Teamübersicht",
    employee: "Mitarbeiter",
    department: "Abteilung",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    placeholderTitle: "Die Team-Arbeitszeitansicht ist für den Ausbau vorbereitet",
    placeholderDescription:
      "Dieser Bereich ist für die Prüfung auf Abteilungsebene, Ausnahmefälle und spätere operative Filter vorgesehen.",
  },
  ko: {
    title: "팀 근무시간",
    tableTitle: "팀 현황",
    employee: "직원",
    department: "부서",
    status: "상태",
    checkIn: "출근",
    checkOut: "퇴근",
    placeholderTitle: "팀 근무시간 화면은 확장 가능한 기본 구조로 준비되어 있습니다",
    placeholderDescription:
      "이 영역은 부서 단위 근태 검토, 예외 확인, 향후 운영 필터를 위한 팀 화면입니다.",
  },
};

const workspaceAdminAreaMessages: Record<LoginLocale, WorkspaceAdminAreaMessages> = {
  en: {
    title: "Admin",
    sectionTitle: "Admin area",
    placeholderTitle: "Customer admin menus will expand here",
    placeholderDescription:
      "Use this area for future customer-admin functions such as company policy, work locations, and role-scoped configuration.",
  },
  de: {
    title: "Admin",
    sectionTitle: "Admin-Bereich",
    placeholderTitle: "Kunden-Admin-Menüs werden hier erweitert",
    placeholderDescription:
      "Dieser Bereich ist für zukünftige Funktionen wie Unternehmensrichtlinien, Einsatzorte und rollenbezogene Konfiguration vorgesehen.",
  },
  ko: {
    title: "관리",
    sectionTitle: "관리 영역",
    placeholderTitle: "고객 관리자 메뉴가 이 영역으로 확장됩니다",
    placeholderDescription:
      "회사 정책, 근무지 관리, 권한별 설정 같은 고객 관리자 기능을 이 영역에 배치합니다.",
  },
};

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

export function getWorkspaceMyWorkTimeMessages(
  locale: LoginLocale
): WorkspaceMyWorkTimeMessages {
  return workspaceMyWorkTimeMessages[locale];
}

export function getWorkspaceTeamWorkTimeMessages(
  locale: LoginLocale
): WorkspaceTeamWorkTimeMessages {
  return workspaceTeamWorkTimeMessages[locale];
}

export function getWorkspaceAdminAreaMessages(
  locale: LoginLocale
): WorkspaceAdminAreaMessages {
  return workspaceAdminAreaMessages[locale];
}

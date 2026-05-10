import type { LoginLocale } from "@/lib/i18n/login-cnt";

export type WspAttndMsgs = {
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

export type WspWorkTimeHistMsgs = {
  eyebrow: string;
  title: string;
  desc: string;
  currentScope: string;
  ownOnly: string;
  scopedAll: string;
  month: string;
  emp: string;
  dept: string;
  keyword: string;
  allEmps: string;
  allDepts: string;
  keywordPh: string;
  search: string;
  reset: string;
  exportExcel: string;
  exportPdf: string;
  total: string;
  rows: string;
  filtsTitle: string;
  filtsDesc: string;
  resultsTitle: string;
  date: string;
  status: string;
  checkIn: string;
  checkOut: string;
  workTime: string;
  noDataTitle: string;
  noDataDesc: string;
  modeAdmin: string;
  modeClient: string;
};

export type WspMyWorkTimeMsgs = {
  title: string;
  sectionTitle: string;
  pointList: string[];
};

export type WspTeamWorkTimeMsgs = {
  title: string;
  tableTitle: string;
  emp: string;
  dept: string;
  status: string;
  checkIn: string;
  checkOut: string;
  phTitle: string;
  phDesc: string;
};

export type WspAdminAreaMsgs = {
  title: string;
  sectionTitle: string;
  phTitle: string;
  phDesc: string;
};

const wspAttndMsgs: Record<LoginLocale, WspAttndMsgs> = {
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

const wspWorkTimeHistMsgs: Record<LoginLocale, WspWorkTimeHistMsgs> = {
  en: {
    eyebrow: "reports",
    title: "Work time history",
    desc:
      "Review monthly work records, filter by emp or dept, and export the result for reporting usage.",
    currentScope: "Scope",
    ownOnly: "Own records only",
    scopedAll: "All emps in scope",
    month: "Target month",
    emp: "Emp",
    dept: "Dept",
    keyword: "Keyword",
    allEmps: "All emps",
    allDepts: "All depts",
    keywordPh: "Name, dept, status, or time",
    search: "Search",
    reset: "Reset",
    exportExcel: "Export Excel",
    exportPdf: "Export PDF",
    total: "Total",
    rows: "rows",
    filtsTitle: "Filters",
    filtsDesc:
      "Narrow the month and emp scope before exporting or reviewing work records.",
    resultsTitle: "History records",
    date: "Date",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Work time",
    noDataTitle: "No records found",
    noDataDesc:
      "Adjust the filters or switch the month to review another work history range.",
    modeAdmin: "Admin",
    modeClient: "Client",
  },
  de: {
    eyebrow: "berichte",
    title: "Arbeitszeitverlauf",
    desc:
      "Monatliche Arbeitszeitdaten prüfen, nach Mitarbeiter oder Abteilung filtern und für Berichte exportieren.",
    currentScope: "Bereich",
    ownOnly: "Nur eigene Einträge",
    scopedAll: "Alle Mitarbeiter im Bereich",
    month: "Monat",
    emp: "Mitarbeiter",
    dept: "Abteilung",
    keyword: "Stichwort",
    allEmps: "Alle Mitarbeiter",
    allDepts: "Alle Abteilungen",
    keywordPh: "Name, Abteilung, Status oder Zeit",
    search: "Suchen",
    reset: "Zurücksetzen",
    exportExcel: "Excel exportieren",
    exportPdf: "PDF exportieren",
    total: "Gesamt",
    rows: "Zeilen",
    filtsTitle: "Filter",
    filtsDesc:
      "Monat und Mitarbeiterbereich eingrenzen, bevor Arbeitszeitdaten geprüft oder exportiert werden.",
    resultsTitle: "Verlaufsdaten",
    date: "Datum",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    workTime: "Arbeitszeit",
    noDataTitle: "Keine Datensätze gefunden",
    noDataDesc:
      "Passen Sie die Filter an oder wechseln Sie den Monat, um einen anderen Zeitraum zu prüfen.",
    modeAdmin: "Admin",
    modeClient: "Client",
  },
  ko: {
    eyebrow: "리포트",
    title: "근태 이력",
    desc:
      "월간 근태 기록을 직원·부서별로 조회하고, 보고용으로 Excel 또는 PDF로 내보낼 수 있습니다.",
    currentScope: "조회 범위",
    ownOnly: "본인 기록만",
    scopedAll: "권한 범위 내 전체 직원",
    month: "대상 월",
    emp: "직원",
    dept: "부서",
    keyword: "키워드",
    allEmps: "전체 직원",
    allDepts: "전체 부서",
    keywordPh: "이름, 부서, 상태, 시간",
    search: "조회",
    reset: "초기화",
    exportExcel: "Excel 내보내기",
    exportPdf: "PDF 내보내기",
    total: "총",
    rows: "건",
    filtsTitle: "필터",
    filtsDesc:
      "대상 월과 직원/부서 범위를 조정한 뒤 근태 기록을 확인하거나 내보낼 수 있습니다.",
    resultsTitle: "기록 목록",
    date: "일자",
    status: "상태",
    checkIn: "출근",
    checkOut: "퇴근",
    workTime: "근무시간",
    noDataTitle: "조회된 기록이 없습니다",
    noDataDesc:
      "필터를 조정하거나 대상 월을 바꿔 다른 기간을 확인해 주세요.",
    modeAdmin: "관리자",
    modeClient: "고객",
  },
};

const wspMyWorkTimeMsgs: Record<LoginLocale, WspMyWorkTimeMsgs> = {
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

const wspTeamWorkTimeMsgs: Record<LoginLocale, WspTeamWorkTimeMsgs> = {
  en: {
    title: "Team Work Time",
    tableTitle: "Team overview",
    emp: "Emp",
    dept: "Dept",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    phTitle: "Team work time view is ready for expansion",
    phDesc:
      "Use this section for dept-level attendance review, exception tracking, and future operational filters.",
  },
  de: {
    title: "Team Arbeitszeit",
    tableTitle: "Teamübersicht",
    emp: "Mitarbeiter",
    dept: "Abteilung",
    status: "Status",
    checkIn: "Check-in",
    checkOut: "Check-out",
    phTitle: "Die Team-Arbeitszeitansicht ist für den Ausbau vorbereitet",
    phDesc:
      "Dieser Bereich ist für die Prüfung auf Abteilungsebene, Ausnahmefälle und spätere operative Filter vorgesehen.",
  },
  ko: {
    title: "팀 근무시간",
    tableTitle: "팀 현황",
    emp: "직원",
    dept: "부서",
    status: "상태",
    checkIn: "출근",
    checkOut: "퇴근",
    phTitle: "팀 근무시간 화면은 확장 가능한 기본 구조로 준비되어 있습니다",
    phDesc:
      "이 영역은 부서 단위 근태 검토, 예외 확인, 향후 운영 필터를 위한 팀 화면입니다.",
  },
};

const wspAdminAreaMsgs: Record<LoginLocale, WspAdminAreaMsgs> = {
  en: {
    title: "Admin",
    sectionTitle: "Admin area",
    phTitle: "Customer admin mnus will expand here",
    phDesc:
      "Use this area for future customer-admin functions such as company policy, work locations, and role-scoped configuration.",
  },
  de: {
    title: "Admin",
    sectionTitle: "Admin-Bereich",
    phTitle: "Kunden-Admin-Menüs werden hier erweitert",
    phDesc:
      "Dieser Bereich ist für zukünftige Funktionen wie Unternehmensrichtlinien, Einsatzorte und rollenbezogene Konfiguration vorgesehen.",
  },
  ko: {
    title: "관리",
    sectionTitle: "관리 영역",
    phTitle: "고객 관리자 메뉴가 이 영역으로 확장됩니다",
    phDesc:
      "회사 정책, 근무지 관리, 권한별 설정 같은 고객 관리자 기능을 이 영역에 배치합니다.",
  },
};

export function getWspAttndMsgs(
  locale: LoginLocale
): WspAttndMsgs {
  return wspAttndMsgs[locale];
}

export function getWspWorkTimeHistMsgs(
  locale: LoginLocale
): WspWorkTimeHistMsgs {
  return wspWorkTimeHistMsgs[locale];
}

export function getWspMyWorkTimeMsgs(
  locale: LoginLocale
): WspMyWorkTimeMsgs {
  return wspMyWorkTimeMsgs[locale];
}

export function getWspTeamWorkTimeMsgs(
  locale: LoginLocale
): WspTeamWorkTimeMsgs {
  return wspTeamWorkTimeMsgs[locale];
}

export function getWspAdminAreaMsgs(
  locale: LoginLocale
): WspAdminAreaMsgs {
  return wspAdminAreaMsgs[locale];
}

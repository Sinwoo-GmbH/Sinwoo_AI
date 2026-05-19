import type { LoginLocale } from "@/lib/i18n/login-cnt";

export type DeptMgtMsgs = {
  title: string;
  treeTitle: string;
  detailTitle: string;
  addRoot: string;
  addChild: string;
  edit: string;
  del: string;
  save: string;
  cancel: string;
  // form labels
  deptCd: string;
  deptNm: string;
  parentDept: string;
  regionCd: string;
  vacCnt: string;
  vacInc: string;
  dspOrd: string;
  stsCd: string;
  // status
  active: string;
  inactive: string;
  // tree
  noData: string;
  noDeptSelected: string;
  rootLevel: string;
  empCount: string;
  // validation
  requiredFields: string;
  deptCdRequired: string;
  deptNmRequired: string;
  // confirm
  confirmDelTitle: string;
  confirmDelMsg: string;
  confirmDelBtn: string;
  cannotDelChild: string;
  cannotDelEmp: string;
  // toast
  toastCreated: string;
  toastUpdated: string;
  toastDeleted: string;
  toastError: string;
};

const deptMgtMsgs: Record<LoginLocale, DeptMgtMsgs> = {
  ko: {
    title: "부서관리",
    treeTitle: "조직도",
    detailTitle: "부서 상세",
    addRoot: "최상위 부서 추가",
    addChild: "하위 부서 추가",
    edit: "수정",
    del: "삭제",
    save: "저장",
    cancel: "취소",
    deptCd: "부서코드",
    deptNm: "부서명",
    parentDept: "상위부서",
    regionCd: "지역코드",
    vacCnt: "기본 휴가일수",
    vacInc: "근속 가산일수",
    dspOrd: "정렬순서",
    stsCd: "상태",
    active: "활성",
    inactive: "비활성",
    noData: "등록된 부서가 없습니다.",
    noDeptSelected: "좌측 조직도에서 부서를 선택하세요.",
    rootLevel: "(최상위)",
    empCount: "소속 직원",
    requiredFields: "필수 항목을 확인해주세요.",
    deptCdRequired: "부서코드를 입력해주세요.",
    deptNmRequired: "부서명을 입력해주세요.",
    confirmDelTitle: "부서 삭제",
    confirmDelMsg: "이 부서를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
    confirmDelBtn: "삭제",
    cannotDelChild: "하위 부서가 존재하여 삭제할 수 없습니다.",
    cannotDelEmp: "소속 직원이 존재하여 삭제할 수 없습니다.",
    toastCreated: "부서가 등록되었습니다.",
    toastUpdated: "부서가 수정되었습니다.",
    toastDeleted: "부서가 삭제되었습니다.",
    toastError: "처리 중 오류가 발생했습니다.",
  },
  en: {
    title: "Department Management",
    treeTitle: "Organization Chart",
    detailTitle: "Department Detail",
    addRoot: "Add Root Department",
    addChild: "Add Child Department",
    edit: "Edit",
    del: "Delete",
    save: "Save",
    cancel: "Cancel",
    deptCd: "Department Code",
    deptNm: "Department Name",
    parentDept: "Parent Department",
    regionCd: "Region Code",
    vacCnt: "Default Vacation Days",
    vacInc: "Seniority Increment",
    dspOrd: "Display Order",
    stsCd: "Status",
    active: "Active",
    inactive: "Inactive",
    noData: "No departments registered.",
    noDeptSelected: "Select a department from the organization chart.",
    rootLevel: "(Root Level)",
    empCount: "Employees",
    requiredFields: "Please check required fields.",
    deptCdRequired: "Department code is required.",
    deptNmRequired: "Department name is required.",
    confirmDelTitle: "Delete Department",
    confirmDelMsg: "Are you sure you want to delete this department? This cannot be undone.",
    confirmDelBtn: "Delete",
    cannotDelChild: "Cannot delete: child departments exist.",
    cannotDelEmp: "Cannot delete: employees belong to this department.",
    toastCreated: "Department created successfully.",
    toastUpdated: "Department updated successfully.",
    toastDeleted: "Department deleted successfully.",
    toastError: "An error occurred while processing.",
  },
  de: {
    title: "Abteilungsverwaltung",
    treeTitle: "Organigramm",
    detailTitle: "Abteilungsdetail",
    addRoot: "Hauptabteilung hinzufügen",
    addChild: "Unterabteilung hinzufügen",
    edit: "Bearbeiten",
    del: "Löschen",
    save: "Speichern",
    cancel: "Abbrechen",
    deptCd: "Abteilungscode",
    deptNm: "Abteilungsname",
    parentDept: "Übergeordnete Abteilung",
    regionCd: "Regionscode",
    vacCnt: "Standard-Urlaubstage",
    vacInc: "Dienstjahre-Zuschlag",
    dspOrd: "Anzeigereihenfolge",
    stsCd: "Status",
    active: "Aktiv",
    inactive: "Inaktiv",
    noData: "Keine Abteilungen registriert.",
    noDeptSelected: "Wählen Sie eine Abteilung aus dem Organigramm.",
    rootLevel: "(Hauptebene)",
    empCount: "Mitarbeiter",
    requiredFields: "Bitte überprüfen Sie die Pflichtfelder.",
    deptCdRequired: "Abteilungscode ist erforderlich.",
    deptNmRequired: "Abteilungsname ist erforderlich.",
    confirmDelTitle: "Abteilung löschen",
    confirmDelMsg: "Möchten Sie diese Abteilung wirklich löschen? Dies kann nicht rückgängig gemacht werden.",
    confirmDelBtn: "Löschen",
    cannotDelChild: "Löschen nicht möglich: Unterabteilungen vorhanden.",
    cannotDelEmp: "Löschen nicht möglich: Mitarbeiter gehören zu dieser Abteilung.",
    toastCreated: "Abteilung erfolgreich erstellt.",
    toastUpdated: "Abteilung erfolgreich aktualisiert.",
    toastDeleted: "Abteilung erfolgreich gelöscht.",
    toastError: "Bei der Verarbeitung ist ein Fehler aufgetreten.",
  },
};

export function getDeptMgtMsgs(locale: LoginLocale): DeptMgtMsgs {
  return deptMgtMsgs[locale];
}

/* ── Region label helper ─────────────────────────────────── */

const REGION_LABELS: Record<string, Record<LoginLocale, string>> = {
  HE: { ko: "헤센", en: "Hessen", de: "Hessen" },
  BY: { ko: "바이에른", en: "Bavaria", de: "Bayern" },
  BE: { ko: "베를린", en: "Berlin", de: "Berlin" },
  NW: { ko: "노르트라인-베스트팔렌", en: "North Rhine-Westphalia", de: "Nordrhein-Westfalen" },
  BW: { ko: "바덴-뷔르템베르크", en: "Baden-Württemberg", de: "Baden-Württemberg" },
  NI: { ko: "니더작센", en: "Lower Saxony", de: "Niedersachsen" },
  SN: { ko: "작센", en: "Saxony", de: "Sachsen" },
  RP: { ko: "라인란트-팔츠", en: "Rhineland-Palatinate", de: "Rheinland-Pfalz" },
  SH: { ko: "슐레스비히-홀슈타인", en: "Schleswig-Holstein", de: "Schleswig-Holstein" },
  TH: { ko: "튀링엔", en: "Thuringia", de: "Thüringen" },
  BB: { ko: "브란덴부르크", en: "Brandenburg", de: "Brandenburg" },
  ST: { ko: "작센-안할트", en: "Saxony-Anhalt", de: "Sachsen-Anhalt" },
  MV: { ko: "메클렌부르크-포어포메른", en: "Mecklenburg-Vorpommern", de: "Mecklenburg-Vorpommern" },
  SL: { ko: "자를란트", en: "Saarland", de: "Saarland" },
  HH: { ko: "함부르크", en: "Hamburg", de: "Hamburg" },
  HB: { ko: "브레멘", en: "Bremen", de: "Bremen" },
  ALL: { ko: "전국", en: "All Regions", de: "Alle Regionen" },
};

export function regionLabel(locale: LoginLocale, cd: string | null | undefined): string {
  if (!cd) return "-";
  return REGION_LABELS[cd]?.[locale] ?? cd;
}

export const REGION_OPTIONS = Object.keys(REGION_LABELS).filter((k) => k !== "ALL");

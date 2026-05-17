import type { LoginLocale } from "@/lib/i18n/login-cnt";

/* ── Leave Type ── */
export type LeaveTypeCd = "Annual Leave" | "Sick Leave" | "Marriage Leave" | "Bereavement Leave" | "Unpaid Leave" | "Special Leave";

const LEAVE_TYPE_LABELS: Record<LoginLocale, Record<LeaveTypeCd, string>> = {
  ko: {
    "Annual Leave": "연차",
    "Sick Leave": "병가",
    "Marriage Leave": "결혼휴가",
    "Bereavement Leave": "경조휴가",
    "Unpaid Leave": "무급휴가",
    "Special Leave": "특별휴가",
  },
  en: {
    "Annual Leave": "Annual Leave",
    "Sick Leave": "Sick Leave",
    "Marriage Leave": "Marriage Leave",
    "Bereavement Leave": "Bereavement Leave",
    "Unpaid Leave": "Unpaid Leave",
    "Special Leave": "Special Leave",
  },
  de: {
    "Annual Leave": "Jahresurlaub",
    "Sick Leave": "Krankheitsurlaub",
    "Marriage Leave": "Hochzeitsurlaub",
    "Bereavement Leave": "Trauerurlaub",
    "Unpaid Leave": "Unbezahlter Urlaub",
    "Special Leave": "Sonderurlaub",
  },
};

/** 짧은 DB CD → 영문 풀네임 매핑 (백엔드는 "AN"/"SK"… 반환, 프론트 라벨은 풀네임 키 기반) */
const LEAVE_TYPE_CD_ALIAS: Record<string, LeaveTypeCd> = {
  AN: "Annual Leave",
  SK: "Sick Leave",
  MR: "Marriage Leave",
  BV: "Bereavement Leave",
  UP: "Unpaid Leave",
  SP: "Special Leave",
};

export function leaveTypeLabel(locale: LoginLocale, cd: string): string {
  const key = (LEAVE_TYPE_CD_ALIAS[cd] ?? cd) as LeaveTypeCd;
  return LEAVE_TYPE_LABELS[locale]?.[key] ?? cd;
}

/* ── Deduction Type ── */
export type DeductionTypeCd = "Deducted Leave" | "Non-deducted Leave";

const DEDUCTION_TYPE_LABELS: Record<LoginLocale, Record<DeductionTypeCd, string>> = {
  ko: { "Deducted Leave": "차감", "Non-deducted Leave": "비차감" },
  en: { "Deducted Leave": "Deducted", "Non-deducted Leave": "Non-deducted" },
  de: { "Deducted Leave": "Abzugsfähig", "Non-deducted Leave": "Nicht abzugsfähig" },
};

const DEDUCTION_TYPE_CD_ALIAS: Record<string, DeductionTypeCd> = {
  DD: "Deducted Leave",
  ND: "Non-deducted Leave",
};

export function deductionTypeLabel(locale: LoginLocale, cd: string): string {
  const key = (DEDUCTION_TYPE_CD_ALIAS[cd] ?? cd) as DeductionTypeCd;
  return DEDUCTION_TYPE_LABELS[locale]?.[key] ?? cd;
}

/* ── Leave Unit ── */
export type LeaveUnitCd = "Full Day" | "Half Day AM" | "Half Day PM";

const LEAVE_UNIT_LABELS: Record<LoginLocale, Record<LeaveUnitCd, string>> = {
  ko: { "Full Day": "종일", "Half Day AM": "오전 반차", "Half Day PM": "오후 반차" },
  en: { "Full Day": "Full Day", "Half Day AM": "Half Day AM", "Half Day PM": "Half Day PM" },
  de: { "Full Day": "Ganztägig", "Half Day AM": "Halbtags Vorm.", "Half Day PM": "Halbtags Nachm." },
};

const LEAVE_UNIT_CD_ALIAS: Record<string, LeaveUnitCd> = {
  FD: "Full Day",
  AM: "Half Day AM",
  PM: "Half Day PM",
};

export function leaveUnitLabel(locale: LoginLocale, cd: string): string {
  const key = (LEAVE_UNIT_CD_ALIAS[cd] ?? cd) as LeaveUnitCd;
  return LEAVE_UNIT_LABELS[locale]?.[key] ?? cd;
}

/* ── Leave Status ── */
export type LeaveStatusCd = "All" | "Draft" | "Requested" | "Approved" | "Rejected" | "Cancelled" | "Admin Cancelled";

const LEAVE_STATUS_LABELS: Record<LoginLocale, Record<LeaveStatusCd, string>> = {
  ko: {
    All: "전체", Draft: "임시저장", Requested: "요청", Approved: "승인",
    Rejected: "반려", Cancelled: "취소", "Admin Cancelled": "관리자취소",
  },
  en: {
    All: "All", Draft: "Draft", Requested: "Requested", Approved: "Approved",
    Rejected: "Rejected", Cancelled: "Cancelled", "Admin Cancelled": "Admin Cancelled",
  },
  de: {
    All: "Alle", Draft: "Entwurf", Requested: "Beantragt", Approved: "Genehmigt",
    Rejected: "Abgelehnt", Cancelled: "Storniert", "Admin Cancelled": "Admin Storniert",
  },
};

const LEAVE_STATUS_CD_ALIAS: Record<string, LeaveStatusCd> = {
  DRF: "Draft",
  REQ: "Requested",
  APR: "Approved",
  REJ: "Rejected",
  CAN: "Cancelled",
  ADC: "Admin Cancelled",
};

export function leaveStatusLabel(locale: LoginLocale, cd: string): string {
  const key = (LEAVE_STATUS_CD_ALIAS[cd] ?? cd) as LeaveStatusCd;
  return LEAVE_STATUS_LABELS[locale]?.[key] ?? cd;
}

/* ── Approver Status ── */
const APPROVER_STATUS_BASE: Record<LoginLocale, Record<string, string>> = {
  ko: { "No Approver": "결재자 없음", Waiting: "대기중", Rejected: "반려" },
  en: { "No Approver": "No Approver", Waiting: "Waiting", Rejected: "Rejected" },
  de: { "No Approver": "Kein Genehmiger", Waiting: "Wartend", Rejected: "Abgelehnt" },
};

const APPROVED_BY_TEMPLATE: Record<LoginLocale, (n: string) => string> = {
  ko: (n) => `${n}차 승인`,
  en: (n) => `Approved by ${n}`,
  de: (n) => `Genehmigt (${n})`,
};

export function approverStatusLabel(locale: LoginLocale, status: string): string {
  const match = status.match(/^Approved by (\d+)$/);
  if (match) return APPROVED_BY_TEMPLATE[locale](match[1]);
  return APPROVER_STATUS_BASE[locale]?.[status] ?? status;
}

/* ── Page / Section / Field Labels ── */
export type LeavePageMsgs = {
  pageTitle: string;
  pageDesc: string;
  available: string;
  afterRequest: string;
  /* filter */
  filtFrom: string;
  filtTo: string;
  filtStatus: string;
  btnCreate: string;
  btnSearch: string;
  /* table headers */
  thNo: string;
  thLeaveType: string;
  thDeductionType: string;
  thStartDate: string;
  thEndDate: string;
  thDays: string;
  thApproverStatus: string;
  thStatus: string;
  thCreatedAt: string;
  thActions: string;
  /* table actions */
  actEdit: string;
  actView: string;
  actCancel: string;
  actDelete: string;
  actApprove: string;
  actReject: string;
  /* table empty */
  emptyMsg: string;
  /* pagination */
  rows: string;
  allRows: string;
  /* modal titles */
  modalCreate: string;
  modalEdit: string;
  modalView: string;
  /* modal sections */
  secApplicant: string;
  secLeaveSummary: string;
  secLeaveInfo: string;
  secApprovalLine: string;
  /* modal fields */
  fldName: string;
  fldDept: string;
  fldPosition: string;
  fldLeaveType: string;
  fldDeductionType: string;
  fldLeaveUnit: string;
  fldStartDate: string;
  fldEndDate: string;
  fldDays: string;
  fldAttachment: string;
  fldReason: string;
  fldUpload: string;
  fldNoAttachment: string;
  /* modal buttons */
  btnClose: string;
  btnCancel: string;
  btnSaveDraft: string;
  btnSubmit: string;
  /* validation */
  valLeaveType: string;
  valDeductionType: string;
  valLeaveUnit: string;
  valStartDate: string;
  valEndDate: string;
  valEndDateAfterStart: string;
  valReasonSpecial: string;
  valApprover: string;
  valToast: string;
  infoAutoConfirm: string;
  confirmAutoConfirmTitle: string;
  confirmAutoConfirmBtn: string;
  /* confirm dialogs */
  confirmCancelTitle: string;
  confirmApproveTitle: string;
  confirmRejectTitle: string;
  confirmCancelBtn: string;
  confirmApproveBtn: string;
  confirmRejectBtn: string;
  confirmCloseBtn: string;
  rejectReasonPlaceholder: string;
  confirmDeleteTitle: string;
  confirmDeleteDesc: string;
  confirmDeleteBtn: string;
  toastDraftSaved: string;
};

const leavePageMsgs: Record<LoginLocale, LeavePageMsgs> = {
  ko: {
    pageTitle: "휴가 신청",
    pageDesc: "휴가 신청 및 조회",
    available: "잔여일수",
    afterRequest: "신청 후",
    filtFrom: "시작일(From)",
    filtTo: "시작일(To)",
    filtStatus: "상태",
    btnCreate: "신규",
    btnSearch: "검색",
    thNo: "No",
    thLeaveType: "휴가유형",
    thDeductionType: "차감유형",
    thStartDate: "시작일",
    thEndDate: "종료일",
    thDays: "일수",
    thApproverStatus: "결재상태",
    thStatus: "상태",
    thCreatedAt: "생성일",
    thActions: "",
    actEdit: "수정",
    actView: "조회",
    actCancel: "취소",
    actDelete: "삭제",
    actApprove: "승인",
    actReject: "반려",
    emptyMsg: "조건에 맞는 휴가 신청이 없습니다.",
    rows: "행",
    allRows: "전체",
    modalCreate: "휴가 신청",
    modalEdit: "휴가 수정",
    modalView: "휴가 상세",
    secApplicant: "신청자 정보",
    secLeaveSummary: "휴가 요약",
    secLeaveInfo: "휴가 정보",
    secApprovalLine: "결재선",
    fldName: "이름",
    fldDept: "부서",
    fldPosition: "직위",
    fldLeaveType: "휴가유형",
    fldDeductionType: "차감유형",
    fldLeaveUnit: "휴가단위",
    fldStartDate: "시작일",
    fldEndDate: "종료일",
    fldDays: "일수",
    fldAttachment: "첨부파일",
    fldReason: "사유",
    fldUpload: "업로드",
    fldNoAttachment: "첨부파일 없음",
    btnClose: "닫기",
    btnCancel: "취소",
    btnSaveDraft: "임시저장",
    btnSubmit: "제출",
    valLeaveType: "휴가유형을 선택해주세요",
    valDeductionType: "차감유형을 선택해주세요",
    valLeaveUnit: "휴가단위를 선택해주세요",
    valStartDate: "시작일을 입력해주세요",
    valEndDate: "종료일을 입력해주세요",
    valEndDateAfterStart: "종료일은 시작일 이후여야 합니다",
    valReasonSpecial: "특별휴가는 사유를 입력해주세요",
    valApprover: "최소 1명의 결재자가 필요합니다",
    valToast: "필수 항목을 확인해주세요",
    infoAutoConfirm: "결재자가 없습니다. 자동 승인 처리됩니다. 계속하시겠습니까?",
    confirmAutoConfirmTitle: "자동 승인 확인",
    confirmAutoConfirmBtn: "확인",
    confirmCancelTitle: "휴가 취소",
    confirmApproveTitle: "휴가 승인",
    confirmRejectTitle: "휴가 반려",
    confirmCancelBtn: "취소",
    confirmApproveBtn: "승인",
    confirmRejectBtn: "반려",
    confirmCloseBtn: "닫기",
    rejectReasonPlaceholder: "반려 사유",
    confirmDeleteTitle: "휴가 신청 삭제",
    confirmDeleteDesc: "이 임시저장 건을 삭제하시겠습니까? 삭제 후 복구할 수 없습니다.",
    confirmDeleteBtn: "삭제",
    toastDraftSaved: "임시저장 되었습니다",
  },
  en: {
    pageTitle: "Request Leave",
    pageDesc: "Create and review leave requests",
    available: "Available",
    afterRequest: "After Request",
    filtFrom: "Start Date From",
    filtTo: "Start Date To",
    filtStatus: "Status",
    btnCreate: "Create",
    btnSearch: "Search",
    thNo: "No",
    thLeaveType: "Leave Type",
    thDeductionType: "Deduction",
    thStartDate: "Start Date",
    thEndDate: "End Date",
    thDays: "Days",
    thApproverStatus: "Approver",
    thStatus: "Status",
    thCreatedAt: "Created At",
    thActions: "",
    actEdit: "Edit",
    actView: "View",
    actCancel: "Cancel",
    actDelete: "Delete",
    actApprove: "Approve",
    actReject: "Reject",
    emptyMsg: "No leave requests match the current filters.",
    rows: "Rows",
    allRows: "All",
    modalCreate: "Create Leave Request",
    modalEdit: "Edit Leave Request",
    modalView: "Leave Request Details",
    secApplicant: "Applicant Info",
    secLeaveSummary: "Leave Summary",
    secLeaveInfo: "Leave Info",
    secApprovalLine: "Approval Line",
    fldName: "Name",
    fldDept: "Dept",
    fldPosition: "Position",
    fldLeaveType: "Leave Type",
    fldDeductionType: "Deduction Type",
    fldLeaveUnit: "Leave Unit",
    fldStartDate: "Start Date",
    fldEndDate: "End Date",
    fldDays: "Days",
    fldAttachment: "Attachment",
    fldReason: "Reason",
    fldUpload: "Upload",
    fldNoAttachment: "No attachment selected",
    btnClose: "Close",
    btnCancel: "Cancel",
    btnSaveDraft: "Save Draft",
    btnSubmit: "Submit Request",
    valLeaveType: "Leave Type is required",
    valDeductionType: "Deduction Type is required",
    valLeaveUnit: "Leave Unit is required",
    valStartDate: "Start Date is required",
    valEndDate: "End Date is required",
    valEndDateAfterStart: "End Date must be on or after Start Date",
    valReasonSpecial: "Reason is required for Special Leave",
    valApprover: "At least one approver is required",
    valToast: "Please check the required fields",
    infoAutoConfirm: "No approvers assigned. This request will be auto-confirmed. Continue?",
    confirmAutoConfirmTitle: "Auto-Confirm",
    confirmAutoConfirmBtn: "Confirm",
    confirmCancelTitle: "Cancel Leave Request",
    confirmApproveTitle: "Approve Leave Request",
    confirmRejectTitle: "Reject Leave Request",
    confirmCancelBtn: "Cancel Request",
    confirmApproveBtn: "Approve",
    confirmRejectBtn: "Reject",
    confirmCloseBtn: "Close",
    rejectReasonPlaceholder: "Reject reason",
    confirmDeleteTitle: "Delete Leave Request",
    confirmDeleteDesc: "Delete this draft? This action cannot be undone.",
    confirmDeleteBtn: "Delete",
    toastDraftSaved: "Draft saved",
  },
  de: {
    pageTitle: "Urlaubsantrag",
    pageDesc: "Urlaubsanträge erstellen und prüfen",
    available: "Verfügbar",
    afterRequest: "Nach Antrag",
    filtFrom: "Startdatum Von",
    filtTo: "Startdatum Bis",
    filtStatus: "Status",
    btnCreate: "Erstellen",
    btnSearch: "Suchen",
    thNo: "Nr.",
    thLeaveType: "Urlaubsart",
    thDeductionType: "Abzugsart",
    thStartDate: "Startdatum",
    thEndDate: "Enddatum",
    thDays: "Tage",
    thApproverStatus: "Genehmiger",
    thStatus: "Status",
    thCreatedAt: "Erstellt am",
    thActions: "",
    actEdit: "Bearbeiten",
    actView: "Anzeigen",
    actCancel: "Stornieren",
    actDelete: "Löschen",
    actApprove: "Genehmigen",
    actReject: "Ablehnen",
    emptyMsg: "Keine Urlaubsanträge für die aktuellen Filter.",
    rows: "Zeilen",
    allRows: "Alle",
    modalCreate: "Urlaubsantrag erstellen",
    modalEdit: "Urlaubsantrag bearbeiten",
    modalView: "Urlaubsantrag Details",
    secApplicant: "Antragsteller",
    secLeaveSummary: "Urlaubsübersicht",
    secLeaveInfo: "Urlaubsinfo",
    secApprovalLine: "Genehmigungslinie",
    fldName: "Name",
    fldDept: "Abteilung",
    fldPosition: "Position",
    fldLeaveType: "Urlaubsart",
    fldDeductionType: "Abzugsart",
    fldLeaveUnit: "Urlaubseinheit",
    fldStartDate: "Startdatum",
    fldEndDate: "Enddatum",
    fldDays: "Tage",
    fldAttachment: "Anhang",
    fldReason: "Grund",
    fldUpload: "Hochladen",
    fldNoAttachment: "Kein Anhang ausgewählt",
    btnClose: "Schließen",
    btnCancel: "Abbrechen",
    btnSaveDraft: "Entwurf speichern",
    btnSubmit: "Antrag einreichen",
    valLeaveType: "Urlaubsart ist erforderlich",
    valDeductionType: "Abzugsart ist erforderlich",
    valLeaveUnit: "Urlaubseinheit ist erforderlich",
    valStartDate: "Startdatum ist erforderlich",
    valEndDate: "Enddatum ist erforderlich",
    valEndDateAfterStart: "Enddatum muss nach dem Startdatum liegen",
    valReasonSpecial: "Grund ist für Sonderurlaub erforderlich",
    valApprover: "Mindestens ein Genehmiger ist erforderlich",
    valToast: "Bitte überprüfen Sie die Pflichtfelder",
    infoAutoConfirm: "Kein Genehmiger zugewiesen. Der Antrag wird automatisch genehmigt. Fortfahren?",
    confirmAutoConfirmTitle: "Automatische Genehmigung",
    confirmAutoConfirmBtn: "Bestätigen",
    confirmCancelTitle: "Urlaubsantrag stornieren",
    confirmApproveTitle: "Urlaubsantrag genehmigen",
    confirmRejectTitle: "Urlaubsantrag ablehnen",
    confirmCancelBtn: "Antrag stornieren",
    confirmApproveBtn: "Genehmigen",
    confirmRejectBtn: "Ablehnen",
    confirmCloseBtn: "Schließen",
    rejectReasonPlaceholder: "Ablehnungsgrund",
    confirmDeleteTitle: "Urlaubsantrag löschen",
    confirmDeleteDesc: "Diesen Entwurf löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
    confirmDeleteBtn: "Löschen",
    toastDraftSaved: "Als Entwurf gespeichert",
  },
};

export function getLeavePageMsgs(locale: LoginLocale): LeavePageMsgs {
  return leavePageMsgs[locale];
}

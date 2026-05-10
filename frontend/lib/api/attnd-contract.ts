export interface AttndTodayResponse {
  attndDt: string;
  attndStsCd: string;
  attndStsNm: string;
  chkinDtm?: string | null;
  chkoutDtm?: string | null;
  checkInAvailYn: boolean;
  checkOutAvailYn: boolean;
  holidayYn: boolean;
  holidayNm?: string | null;
}

export interface AttndManualEntryRequest {
  attndDt: string;
  chkinTm?: string | null;
  chkoutTm?: string | null;
  attndStsCd?: string | null;
}

export interface AttndCalDayResponse {
  dt: string;
  dayNo: number;
  inMonthYn: boolean;
  todayYn: boolean;
  weekendYn: boolean;
  holidayYn: boolean;
  holidayNm?: string | null;
  attndStsCd: string;
  chkinTm?: string | null;
  chkoutTm?: string | null;
}

export interface AttndWidgetResponse {
  yearMonth: string;
  regionCd: string;
  policy: {
    bizTmznId: string;
    dfltChkinTm: string;
    dfltChkoutTm: string;
    attndFlagGrpCd: string;
    chkinStsCd: string;
    chkoutStsCd: string;
    leaveStsCd: string;
    bizTripStsCd: string;
  };
  today: AttndTodayResponse;
  summary: {
    workedDayCnt: number;
    holidayCnt: number;
    weekendCnt: number;
  };
  holidayFocus?: {
    holiDt: string;
    holiNm: string;
  } | null;
  dayList: AttndCalDayResponse[];
}

export const ATTND_API_PATH = "/api/v1/attendance";

export interface AttndWorkTimeHistQuery {
  yearMonth?: string;
  empNm?: string;
  deptNm?: string;
  keyword?: string;
}

export interface AttndWorkTimeFiltOptResponse {
  refId: number;
  refCd: string;
  refNm: string;
  refSubNm?: string | null;
}

export interface AttndWorkTimeFiltOptsResponse {
  ownOnlyYn: boolean;
  empTotCnt: number;
  empList: AttndWorkTimeFiltOptResponse[];
  deptTotCnt: number;
  deptList: AttndWorkTimeFiltOptResponse[];
}

export interface AttndWorkTimeHistRowResponse {
  attndId: number;
  attndDt: string;
  usrId: number;
  empId?: number | null;
  empNo?: string | null;
  empNm: string;
  deptId?: number | null;
  deptNm?: string | null;
  attndStsCd: string;
  attndStsNm: string;
  chkinTm?: string | null;
  chkoutTm?: string | null;
  workMinuteCnt: number;
}

export interface AttndWorkTimeHistListResponse {
  yearMonth: string;
  ownOnlyYn: boolean;
  totCnt: number;
  itemList: AttndWorkTimeHistRowResponse[];
}

export const ATTND_RPT_API_PATH = "/api/v1/attendance/reports/work-time";

export interface AttendanceTodayRes {
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

export interface AttendanceManualEntryRequest {
  attndDt: string;
  chkinTm?: string | null;
  chkoutTm?: string | null;
  attndStsCd?: string | null;
}

export interface AttendanceCalendarDayRes {
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

export interface AttendanceWidgetRes {
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
  today: AttendanceTodayRes;
  summary: {
    workedDayCnt: number;
    holidayCnt: number;
    weekendCnt: number;
  };
  holidayFocus?: {
    holiDt: string;
    holiNm: string;
  } | null;
  dayList: AttendanceCalendarDayRes[];
}

export const ATTENDANCE_API_PATH = "/api/v1/attendance";

export interface AttendanceWorkTimeHistoryQuery {
  yearMonth?: string;
  empNm?: string;
  deptNm?: string;
  keyword?: string;
}

export interface AttendanceWorkTimeFilterOptionRes {
  refId: number;
  refCd: string;
  refNm: string;
}

export interface AttendanceWorkTimeFilterOptionsRes {
  ownOnlyYn: boolean;
  empTotCnt: number;
  empList: AttendanceWorkTimeFilterOptionRes[];
  deptTotCnt: number;
  deptList: AttendanceWorkTimeFilterOptionRes[];
}

export interface AttendanceWorkTimeHistoryRowRes {
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

export interface AttendanceWorkTimeHistoryListRes {
  yearMonth: string;
  ownOnlyYn: boolean;
  totCnt: number;
  itemList: AttendanceWorkTimeHistoryRowRes[];
}

export const ATTENDANCE_REPORT_API_PATH = "/api/v1/attendance/reports/work-time";

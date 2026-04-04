export interface CreateEmployeeReq {
  tenantId: number;
  coId: number;
  usrId?: number | null;
  deptId?: number | null;
  mgrEmpId?: number | null;
  empNo: string;
  empNm: string;
  teamRoleCd?: string;
  jobTtlNm?: string | null;
  hireDt?: string | null;
  retrDt?: string | null;
  stsCd?: string;
}

export interface EmployeeRes {
  empId: number;
  tenantId: number;
  coId: number;
  usrId?: number | null;
  deptId?: number | null;
  mgrEmpId?: number | null;
  empNo: string;
  empNm: string;
  teamRoleCd: string;
  jobTtlNm?: string | null;
  hireDt?: string | null;
  retrDt?: string | null;
  stsCd: string;
  crtDtm: string;
  updDtm: string;
}

export interface EmployeeListRes {
  totCnt: number;
  itemList: EmployeeRes[];
}

export const EMPLOYEE_API_PATH = "/api/v1/employees";

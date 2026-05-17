export interface CreateEmpRequest {
  tenantId: number;
  coId: number;
  usrId?: number | null;
  deptId?: number | null;
  mgrEmpId?: number | null;
  empNo: string;
  empNm: string;
  teamRoleCd?: string;
  jobTtlCd?: string | null;
  hireDt?: string | null;
  retrDt?: string | null;
  stsCd?: string;
}

export interface EmpResponse {
  empId: number;
  tenantId: number;
  coId: number;
  usrId?: number | null;
  deptId?: number | null;
  mgrEmpId?: number | null;
  empNo: string;
  empNm: string;
  teamRoleCd: string;
  jobTtlCd?: string | null;
  hireDt?: string | null;
  retrDt?: string | null;
  stsCd: string;
  crtDtm: string;
  updDtm: string;
}

export interface EmpListResponse {
  totCnt: number;
  itemList: EmpResponse[];
}

export const EMP_API_PATH = "/api/v1/employees";

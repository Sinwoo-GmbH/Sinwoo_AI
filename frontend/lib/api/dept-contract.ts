/* ── Department API contract ─────────────────────────────── */

export interface DeptRequest {
  tenantId: number;
  coId: number;
  deptCd: string;
  deptNm: string;
  upDeptId?: number | null;
  stsCd?: string;
  regionCd?: string | null;
  vacCnt?: number | null;
  vacInc?: number | null;
  dspOrd?: number | null;
}

export interface DeptResponse {
  deptId: number;
  tenantId: number;
  coId: number;
  deptCd: string;
  deptNm: string;
  upDeptId?: number | null;
  deptLvlNo: number;
  stsCd: string;
  regionCd?: string | null;
  vacCnt?: number | null;
  vacInc?: number | null;
  dspOrd?: number | null;
  crtDtm: string;
  updDtm: string;
}

export interface DeptNodeResponse {
  deptId: number;
  deptCd: string;
  deptNm: string;
  upDeptId?: number | null;
  deptLvlNo: number;
  stsCd: string;
  regionCd?: string | null;
  vacCnt?: number | null;
  vacInc?: number | null;
  dspOrd?: number | null;
  childList: DeptNodeResponse[];
}

export interface DeptListResponse {
  totCnt: number;
  itemList: DeptResponse[];
}

export interface DeptTreeResponse {
  totCnt: number;
  itemList: DeptNodeResponse[];
}

export interface DeptEmpCountResponse {
  deptId: number;
  empCnt: number;
}

export const DEPT_API_PATH = "/api/v1/departments";

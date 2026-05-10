export interface CreateDeptRequest {
  tenantId: number;
  coId: number;
  deptCd: string;
  deptNm: string;
  upDeptId?: number | null;
  stsCd?: string;
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
  crtDtm: string;
  updDtm: string;
}

export interface DeptNodeResponse extends DeptResponse {
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

export const DEPT_API_PATH = "/api/v1/departments";

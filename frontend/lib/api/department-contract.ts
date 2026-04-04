export interface CreateDepartmentReq {
  tenantId: number;
  coId: number;
  deptCd: string;
  deptNm: string;
  upDeptId?: number | null;
  stsCd?: string;
}

export interface DepartmentRes {
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

export interface DepartmentNodeRes extends DepartmentRes {
  childList: DepartmentNodeRes[];
}

export interface DepartmentListRes {
  totCnt: number;
  itemList: DepartmentRes[];
}

export interface DepartmentTreeRes {
  totCnt: number;
  itemList: DepartmentNodeRes[];
}

export const DEPARTMENT_API_PATH = "/api/v1/departments";

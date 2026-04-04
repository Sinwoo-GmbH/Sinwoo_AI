export interface CreateRoleReq {
  roleCd: string;
  roleNm: string;
  roleGrpCd?: string;
  roleLvlCd?: string;
  roleScopeCd?: string;
  roleD1Cd?: string;
  roleD2Cd?: string;
  roleD3Cd?: string;
}

export interface RoleRes {
  roleId: number;
  roleCd: string;
  roleNm: string;
  roleGrpCd?: string | null;
  roleLvlCd?: string | null;
  roleScopeCd?: string | null;
  roleD1Cd?: string | null;
  roleD2Cd?: string | null;
  roleD3Cd?: string | null;
  crtDtm: string;
  updDtm: string;
}

export interface RoleListRes {
  totCnt: number;
  itemList: RoleRes[];
}

export const ROLE_API_PATH = "/api/v1/roles";

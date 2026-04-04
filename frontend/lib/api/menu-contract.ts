export interface CreateMenuReq {
  mnuCd: string;
  mnuNm: string;
  mnuScopeCd: string;
  upMnuId?: number | null;
  pathUri?: string | null;
  iconNm?: string | null;
  dspOrd?: number;
  useYn?: "Y" | "N";
}

export interface MenuRes {
  mnuId: number;
  mnuCd: string;
  mnuNm: string;
  mnuScopeCd: string;
  upMnuId?: number | null;
  pathUri?: string | null;
  iconNm?: string | null;
  dspOrd: number;
  useYn: "Y" | "N";
  crtDtm: string;
  updDtm: string;
}

export interface MenuNodeRes extends MenuRes {
  childList: MenuNodeRes[];
}

export interface MenuListRes {
  totCnt: number;
  itemList: MenuRes[];
}

export interface MenuTreeRes {
  totCnt: number;
  itemList: MenuNodeRes[];
}

export interface RoleMenuGrantReq {
  mnuCd: string;
  viewYn?: "Y" | "N";
  crtYn?: "Y" | "N";
  updYn?: "Y" | "N";
  delYn?: "Y" | "N";
  aprvYn?: "Y" | "N";
  exprtYn?: "Y" | "N";
}

export interface UpsertRoleMenuAuthReq {
  roleCd: string;
  grantList: RoleMenuGrantReq[];
}

export interface RoleMenuAuthRes {
  roleCd: string;
  roleNm: string;
  mnuCd: string;
  mnuNm: string;
  mnuScopeCd: string;
  pathUri?: string | null;
  viewYn: "Y" | "N";
  crtYn: "Y" | "N";
  updYn: "Y" | "N";
  delYn: "Y" | "N";
  aprvYn: "Y" | "N";
  exprtYn: "Y" | "N";
}

export interface RoleMenuAuthListRes {
  totCnt: number;
  itemList: RoleMenuAuthRes[];
}

export const MENU_API_PATH = "/api/v1/menus";
export const ROLE_MENU_AUTH_API_PATH = "/api/v1/role-menu-auths";

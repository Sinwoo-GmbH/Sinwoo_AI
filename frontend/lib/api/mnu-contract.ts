export interface CreateMnuRequest {
  mnuCd: string;
  mnuNm: string;
  mnuScopeCd: string;
  upMnuId?: number | null;
  pathUri?: string | null;
  iconNm?: string | null;
  dspOrd?: number;
  billGateCd?: string | null;
  useYn?: "Y" | "N";
}

export interface MnuResponse {
  mnuId: number;
  mnuCd: string;
  mnuNm: string;
  mnuScopeCd: string;
  upMnuId?: number | null;
  pathUri?: string | null;
  iconNm?: string | null;
  dspOrd: number;
  billGateCd?: string | null;
  useYn: "Y" | "N";
  crtDtm: string;
  updDtm: string;
}

export interface MnuNodeResponse extends MnuResponse {
  childList: MnuNodeResponse[];
}

export interface MnuListResponse {
  totCnt: number;
  itemList: MnuResponse[];
}

export interface MnuTreeResponse {
  totCnt: number;
  itemList: MnuNodeResponse[];
}

export interface RoleMnuGrantRequest {
  mnuCd: string;
  viewYn?: "Y" | "N";
  crtYn?: "Y" | "N";
  updYn?: "Y" | "N";
  delYn?: "Y" | "N";
  aprvYn?: "Y" | "N";
  exprtYn?: "Y" | "N";
}

export interface UpsertRoleMnuAuthRequest {
  roleCd: string;
  grantList: RoleMnuGrantRequest[];
}

export interface RoleMnuAuthResponse {
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

export interface RoleMnuAuthListResponse {
  totCnt: number;
  itemList: RoleMnuAuthResponse[];
}

export const MNU_API_PATH = "/api/v1/menus";
export const ROLE_MNU_AUTH_API_PATH = "/api/v1/role-mnu-auths";

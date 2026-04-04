export interface CreateTenantReq {
  tenantCd: string;
  tenantNm: string;
  tenantTpCd?: string;
  billFreeYn?: "Y" | "N";
}

export interface TenantRes {
  tenantId: number;
  tenantCd: string;
  tenantNm: string;
  tenantTpCd: string;
  billFreeYn: "Y" | "N";
  stsCd: string;
  crtDtm: string;
  updDtm: string;
}

export interface TenantListRes {
  totCnt: number;
  itemList: TenantRes[];
}

export const TENANT_API_PATH = "/api/v1/tenants";

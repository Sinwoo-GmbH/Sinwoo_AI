export interface CreateTenantRequest {
  tenantCd: string;
  tenantNm: string;
  emlDomn?: string;
  tenantTpCd?: string;
  billFreeYn?: "Y" | "N";
}

export interface TenantResponse {
  tenantId: number;
  tenantCd: string;
  tenantNm: string;
  emlDomn: string | null;
  tenantTpCd: string;
  billFreeYn: "Y" | "N";
  stsCd: string;
  crtDtm: string;
  updDtm: string;
}

export interface TenantListResponse {
  totCnt: number;
  itemList: TenantResponse[];
}

export const TENANT_API_PATH = "/api/v1/tenants";

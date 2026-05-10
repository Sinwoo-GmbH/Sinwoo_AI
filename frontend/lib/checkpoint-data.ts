import type { DeptListResponse } from "@/lib/api/dept-contract";
import type { EmpListResponse } from "@/lib/api/emp-contract";
import type { SubscrPlanListResponse } from "@/lib/api/bill-contract";
import type { MnuListResponse, MnuTreeResponse } from "@/lib/api/mnu-contract";
import type { RoleListResponse } from "@/lib/api/role-contract";
import type { TenantListResponse } from "@/lib/api/tenant-contract";

// Developer note:
// Checkpoint data is a local inspection/dev preview helper only.
// It must not be treated as runtime source-of-truth for workspace UI state.

type CoResponse = {
  coId: number;
  tenantId: number;
  coCd: string;
  coNm: string;
  regNo?: string | null;
  stsCd: string;
  crtDtm: string;
  updDtm: string;
};

type CoListResponse = {
  totCnt: number;
  itemList: CoResponse[];
};

type UsrResponse = {
  usrId: number;
  tenantId: number;
  coId?: number | null;
  lgnId: string;
  eml: string;
  dspNm: string;
  loclCd: string;
  telNo?: string | null;
  authGrpCd?: string | null;
  authLvlCd?: string | null;
  stsCd: string;
  roleCds: string[];
  crtDtm: string;
  updDtm: string;
};

type UsrListResponse = {
  totCnt: number;
  itemList: UsrResponse[];
};

type HealthResponse = {
  status: string;
};

export type CheckpointData = {
  apiBaseUrl: string;
  connected: boolean;
  health?: HealthResponse;
  error?: string;
  tenants?: TenantListResponse;
  cos?: CoListResponse;
  depts?: DeptListResponse;
  emps?: EmpListResponse;
  usrs?: UsrListResponse;
  roles?: RoleListResponse;
  adminMnus?: MnuListResponse;
  customerMnus?: MnuListResponse;
  userVisibleMnus?: MnuTreeResponse;
  plans?: SubscrPlanListResponse;
};

const API_BASE_URL = process.env.SINWOO_API_BASE_URL ?? "http://localhost:8080";

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`${path} -> ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export async function getCheckpointData(): Promise<CheckpointData> {
  try {
    const [health, tenants, roles, plans, adminMnus, customerMnus] = await Promise.all([
      fetchJson<HealthResponse>("/actuator/health"),
      fetchJson<TenantListResponse>("/api/v1/tenants"),
      fetchJson<RoleListResponse>("/api/v1/roles"),
      fetchJson<SubscrPlanListResponse>("/api/v1/subscription-plans"),
      fetchJson<MnuListResponse>("/api/v1/menus?mnuScopeCd=ADMIN"),
      fetchJson<MnuListResponse>("/api/v1/menus?mnuScopeCd=CUSTOMER"),
    ]);

    const firstTenant = tenants.itemList[0];
    if (!firstTenant) {
      return {
        apiBaseUrl: API_BASE_URL,
        connected: true,
        health,
        tenants,
        roles,
        plans,
        adminMnus,
        customerMnus,
      };
    }

    const cos = await fetchJson<CoListResponse>(`/api/v1/companies?tenantId=${firstTenant.tenantId}`);
    const firstCo = cos.itemList[0];

    let depts: DeptListResponse | undefined;
    let emps: EmpListResponse | undefined;
    let usrs: UsrListResponse | undefined;
    let userVisibleMnus: MnuTreeResponse | undefined;

    if (firstCo) {
      [depts, emps, usrs] = await Promise.all([
        fetchJson<DeptListResponse>(`/api/v1/departments?tenantId=${firstTenant.tenantId}&coId=${firstCo.coId}`),
        fetchJson<EmpListResponse>(`/api/v1/employees?tenantId=${firstTenant.tenantId}&coId=${firstCo.coId}`),
        fetchJson<UsrListResponse>(`/api/v1/users?tenantId=${firstTenant.tenantId}&coId=${firstCo.coId}`),
      ]);

      const firstUsr = usrs.itemList[0];
      if (firstUsr) {
        userVisibleMnus = await fetchJson<MnuTreeResponse>(
          `/api/v1/menus/visible-by-login?tenantCd=${firstTenant.tenantCd}&lgnId=${firstUsr.lgnId}&mnuScopeCd=CUSTOMER`,
        );
      }
    }

    return {
      apiBaseUrl: API_BASE_URL,
      connected: true,
      health,
      tenants,
      cos,
      depts,
      emps,
      usrs,
      roles,
      adminMnus,
      customerMnus,
      userVisibleMnus,
      plans,
    };
  } catch (error) {
    return {
      apiBaseUrl: API_BASE_URL,
      connected: false,
      error: error instanceof Error ? error.message : "Unknown error",
    };
  }
}

import type { DepartmentListRes } from "@/lib/api/department-contract";
import type { EmployeeListRes } from "@/lib/api/employee-contract";
import type { SubscriptionPlanListRes } from "@/lib/api/billing-contract";
import type { MenuListRes, MenuTreeRes } from "@/lib/api/menu-contract";
import type { RoleListRes } from "@/lib/api/role-contract";
import type { TenantListRes } from "@/lib/api/tenant-contract";

// Developer note:
// Checkpoint data is a local inspection/dev preview helper only.
// It must not be treated as runtime source-of-truth for workspace UI state.

type CompanyRes = {
  coId: number;
  tenantId: number;
  coCd: string;
  coNm: string;
  regNo?: string | null;
  stsCd: string;
  crtDtm: string;
  updDtm: string;
};

type CompanyListRes = {
  totCnt: number;
  itemList: CompanyRes[];
};

type UserRes = {
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

type UserListRes = {
  totCnt: number;
  itemList: UserRes[];
};

type HealthRes = {
  status: string;
};

export type CheckpointData = {
  apiBaseUrl: string;
  connected: boolean;
  health?: HealthRes;
  error?: string;
  tenants?: TenantListRes;
  companies?: CompanyListRes;
  departments?: DepartmentListRes;
  employees?: EmployeeListRes;
  users?: UserListRes;
  roles?: RoleListRes;
  adminMenus?: MenuListRes;
  customerMenus?: MenuListRes;
  userVisibleMenus?: MenuTreeRes;
  plans?: SubscriptionPlanListRes;
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
    const [health, tenants, roles, plans, adminMenus, customerMenus] = await Promise.all([
      fetchJson<HealthRes>("/actuator/health"),
      fetchJson<TenantListRes>("/api/v1/tenants"),
      fetchJson<RoleListRes>("/api/v1/roles"),
      fetchJson<SubscriptionPlanListRes>("/api/v1/subscription-plans"),
      fetchJson<MenuListRes>("/api/v1/menus?mnuScopeCd=ADMIN"),
      fetchJson<MenuListRes>("/api/v1/menus?mnuScopeCd=CUSTOMER"),
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
        adminMenus,
        customerMenus,
      };
    }

    const companies = await fetchJson<CompanyListRes>(`/api/v1/companies?tenantId=${firstTenant.tenantId}`);
    const firstCompany = companies.itemList[0];

    let departments: DepartmentListRes | undefined;
    let employees: EmployeeListRes | undefined;
    let users: UserListRes | undefined;
    let userVisibleMenus: MenuTreeRes | undefined;

    if (firstCompany) {
      [departments, employees, users] = await Promise.all([
        fetchJson<DepartmentListRes>(`/api/v1/departments?tenantId=${firstTenant.tenantId}&coId=${firstCompany.coId}`),
        fetchJson<EmployeeListRes>(`/api/v1/employees?tenantId=${firstTenant.tenantId}&coId=${firstCompany.coId}`),
        fetchJson<UserListRes>(`/api/v1/users?tenantId=${firstTenant.tenantId}&coId=${firstCompany.coId}`),
      ]);

      const firstUser = users.itemList[0];
      if (firstUser) {
        userVisibleMenus = await fetchJson<MenuTreeRes>(
          `/api/v1/menus/visible-by-user?usrId=${firstUser.usrId}&mnuScopeCd=CUSTOMER`,
        );
      }
    }

    return {
      apiBaseUrl: API_BASE_URL,
      connected: true,
      health,
      tenants,
      companies,
      departments,
      employees,
      users,
      roles,
      adminMenus,
      customerMenus,
      userVisibleMenus,
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

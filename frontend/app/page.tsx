import { Activity, BadgeCheck, Building2, CreditCard, ShieldCheck, Users2, Workflow } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getCheckpointData } from "@/lib/checkpoint-data";

export const dynamic = "force-dynamic";

function StatCard({
  title,
  value,
  description,
  icon: Icon,
}: {
  title: string;
  value: string | number;
  description: string;
  icon: React.ComponentType<{ className?: string }>;
}) {
  return (
    <Card className="border-white bg-white/90">
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-3">
        <div>
          <CardDescription>{title}</CardDescription>
          <CardTitle className="mt-2 text-3xl">{value}</CardTitle>
        </div>
        <div className="rounded-2xl bg-slate-950 p-3 text-white">
          <Icon className="h-5 w-5" />
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-sm text-slate-500">{description}</p>
      </CardContent>
    </Card>
  );
}

export default async function HomePage() {
  const data = await getCheckpointData();

  const tenantCount = data.tenants?.totCnt ?? 0;
  const internalCount = data.tenants?.itemList.filter((tenant) => tenant.tenantTpCd === "INTERNAL").length ?? 0;
  const customerCount = data.tenants?.itemList.filter((tenant) => tenant.tenantTpCd === "CUSTOMER").length ?? 0;
  const billFreeCount = data.tenants?.itemList.filter((tenant) => tenant.billFreeYn === "Y").length ?? 0;

  const companyCount = data.companies?.totCnt ?? 0;
  const departmentCount = data.departments?.totCnt ?? 0;
  const employeeCount = data.employees?.totCnt ?? 0;
  const userCount = data.users?.totCnt ?? 0;
  const roleCount = data.roles?.totCnt ?? 0;
  const planCount = data.plans?.totCnt ?? 0;

  return (
    <main className="min-h-screen">
      <div className="mx-auto flex min-h-screen max-w-[1600px] px-4 py-6 lg:px-6">
        <section className="flex-1">
          <div className="overflow-hidden rounded-[32px] border border-white/70 bg-white/80 shadow-panel backdrop-blur">
            <header className="border-b border-slate-200/70 px-6 py-6">
              <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
                <div>
                  <p className="text-sm uppercase tracking-[0.28em] text-slate-500">Sinwoo Next-Gen Checkpoint</p>
                  <h1 className="mt-2 text-3xl font-semibold tracking-tight text-slate-950">
                    B2B Platform Mid-Review Dashboard
                  </h1>
                  <p className="mt-3 max-w-3xl text-sm text-slate-600">
                    This screen reads the running backend and shows whether the current next-generation foundation
                    is really connected at the web layer.
                  </p>
                </div>
                <div className="flex flex-wrap items-center gap-3">
                  <Badge className={data.connected ? "bg-success text-success-foreground" : "bg-destructive text-white"}>
                    {data.connected ? `Backend ${data.health?.status ?? "UP"}` : "Backend Disconnected"}
                  </Badge>
                  <Badge variant="secondary">{data.apiBaseUrl}</Badge>
                </div>
              </div>
            </header>

            <div className="space-y-6 p-6">
              {!data.connected ? (
                <Card className="border-destructive/20 bg-red-50">
                  <CardHeader>
                    <CardTitle>Backend connection is not available</CardTitle>
                    <CardDescription>
                      Start the backend on port 8080, then refresh the frontend page on port 3000.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="text-sm text-slate-700">{data.error}</CardContent>
                </Card>
              ) : null}

              <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <StatCard
                  title="Tenants"
                  value={tenantCount}
                  description={`Internal ${internalCount} / Customer ${customerCount}`}
                  icon={Building2}
                />
                <StatCard
                  title="Organizations"
                  value={`${companyCount} / ${departmentCount} / ${employeeCount}`}
                  description="Company / Department / Employee"
                  icon={Workflow}
                />
                <StatCard
                  title="Authorization"
                  value={`${roleCount} / ${userCount}`}
                  description="Role count / User count"
                  icon={ShieldCheck}
                />
                <StatCard
                  title="Billing"
                  value={`${planCount} / ${billFreeCount}`}
                  description="Plan count / Billing-free tenants"
                  icon={CreditCard}
                />
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
                <Card>
                  <CardHeader>
                    <CardTitle>Tenant and Billing Policy</CardTitle>
                    <CardDescription>
                      Internal tenants are forced to billing-free mode. Customer tenants can use the paid plan flow.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {data.tenants?.itemList.map((tenant) => (
                      <div key={tenant.tenantId} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div>
                            <p className="font-medium text-slate-950">{tenant.tenantNm}</p>
                            <p className="text-sm text-slate-500">{tenant.tenantCd}</p>
                          </div>
                          <div className="flex flex-wrap gap-2">
                            <Badge variant="secondary">{tenant.tenantTpCd}</Badge>
                            <Badge className={tenant.billFreeYn === "Y" ? "bg-success text-success-foreground" : ""}>
                              {tenant.billFreeYn === "Y" ? "Billing Free" : "Paid Tenant"}
                            </Badge>
                            <Badge variant="outline">{tenant.stsCd}</Badge>
                          </div>
                        </div>
                      </div>
                    )) ?? <p className="text-sm text-slate-500">No tenant data</p>}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Role Depth Model</CardTitle>
                    <CardDescription>Depth 1 to depth 3 structure is already reflected in the backend role model.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {data.roles?.itemList.map((role) => (
                      <div key={role.roleId} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="mb-2 flex items-center justify-between gap-3">
                          <p className="font-medium text-slate-950">{role.roleNm}</p>
                          <Badge variant="outline">{role.roleCd}</Badge>
                        </div>
                        <div className="flex flex-wrap gap-2 text-xs">
                          {role.roleScopeCd ? <Badge variant="secondary">{role.roleScopeCd}</Badge> : null}
                          {role.roleD1Cd ? <Badge variant="secondary">{role.roleD1Cd}</Badge> : null}
                          {role.roleD2Cd ? <Badge variant="secondary">{role.roleD2Cd}</Badge> : null}
                          {role.roleD3Cd ? <Badge variant="secondary">{role.roleD3Cd}</Badge> : null}
                        </div>
                      </div>
                    )) ?? <p className="text-sm text-slate-500">No role data</p>}
                  </CardContent>
                </Card>
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
                <Card>
                  <CardHeader>
                    <CardTitle>Company Hierarchy Check</CardTitle>
                    <CardDescription>
                      This section verifies the company, department, employee, and user hierarchy for the first available company.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-2">
                      <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="mb-2 text-xs uppercase tracking-[0.24em] text-slate-500">Companies</div>
                        {(data.companies?.itemList ?? []).map((company) => (
                          <div key={company.coId} className="mb-3 last:mb-0">
                            <p className="font-medium text-slate-950">{company.coNm}</p>
                            <p className="text-sm text-slate-500">{company.coCd}</p>
                          </div>
                        ))}
                      </div>
                      <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="mb-2 text-xs uppercase tracking-[0.24em] text-slate-500">Departments</div>
                        {(data.departments?.itemList ?? []).map((department) => (
                          <div key={department.deptId} className="mb-3 last:mb-0">
                            <p className="font-medium text-slate-950">{department.deptNm}</p>
                            <p className="text-sm text-slate-500">
                              {department.deptCd} / Level {department.deptLvlNo}
                            </p>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                      <div className="mb-3 flex items-center gap-2 text-xs uppercase tracking-[0.24em] text-slate-500">
                        <Users2 className="h-4 w-4" />
                        Employees and Linked Users
                      </div>
                      <div className="space-y-3">
                        {(data.employees?.itemList ?? []).map((employee) => (
                          <div key={employee.empId} className="rounded-2xl bg-white p-4">
                            <div className="flex flex-wrap items-center justify-between gap-3">
                              <div>
                                <p className="font-medium text-slate-950">{employee.empNm}</p>
                                <p className="text-sm text-slate-500">
                                  {employee.empNo} / {employee.teamRoleCd}
                                </p>
                              </div>
                              <Badge variant="outline">{employee.jobTtlNm ?? "No Job Title"}</Badge>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Customer Menu Visibility</CardTitle>
                    <CardDescription>
                      The system now controls customer menus by assigned roles and by actual user mapping.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                      <div className="mb-2 text-xs uppercase tracking-[0.24em] text-slate-500">Menu Master</div>
                      <div className="flex flex-wrap gap-2">
                        {(data.customerMenus?.itemList ?? []).map((menu) => (
                          <Badge key={menu.mnuId} variant="secondary">
                            {menu.mnuCd}
                          </Badge>
                        ))}
                      </div>
                    </div>

                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                      <div className="mb-2 text-xs uppercase tracking-[0.24em] text-slate-500">Visible By First User</div>
                      <div className="flex flex-wrap gap-2">
                        {(data.userVisibleMenus?.itemList ?? []).map((menu) => (
                          <Badge key={menu.mnuId} className="bg-slate-950 text-white">
                            {menu.mnuCd}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </section>

              <section className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
                <Card>
                  <CardHeader>
                    <CardTitle>Plan Catalog</CardTitle>
                    <CardDescription>The billing model is already split between internal and customer tenants.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {(data.plans?.itemList ?? []).map((plan) => (
                      <div key={plan.planId} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div>
                            <p className="font-medium text-slate-950">{plan.planNm}</p>
                            <p className="text-sm text-slate-500">{plan.planCd}</p>
                          </div>
                          <div className="flex flex-wrap gap-2">
                            <Badge variant="secondary">{plan.tenantTpCd}</Badge>
                            <Badge variant="outline">{plan.currCd}</Badge>
                            <Badge className="bg-success text-success-foreground">{plan.baseAmt}</Badge>
                          </div>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Checkpoint Summary</CardTitle>
                    <CardDescription>This is the current point where the web screen can already verify real backend progress.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3 text-sm text-slate-600">
                    <div className="rounded-2xl bg-slate-50 p-4">
                      <div className="mb-2 flex items-center gap-2 text-slate-950">
                        <Activity className="h-4 w-4" />
                        Runtime
                      </div>
                      Backend health, tenant data, roles, plans, companies, departments, employees, and user-based menu visibility
                      are all readable from the web layer.
                    </div>
                    <div className="rounded-2xl bg-slate-50 p-4">
                      <div className="mb-2 flex items-center gap-2 text-slate-950">
                        <BadgeCheck className="h-4 w-4" />
                        Verified Structure
                      </div>
                      Tenant policy, role depth, menu authorization, department tree, employee hierarchy, and billing plan split are
                      all represented in the running platform.
                    </div>
                    <div className="rounded-2xl bg-slate-50 p-4">
                      The next logical screen after this checkpoint is a real admin console page and a customer portal page that render
                      different menus from the same authorization tables.
                    </div>
                  </CardContent>
                </Card>
              </section>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}

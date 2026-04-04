import {
  Bell,
  Building2,
  FileText,
  Globe2,
  LayoutDashboard,
  ReceiptText,
  TimerReset,
  Users,
} from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const metricCards = [
  { label: "승인 대기 문서", value: "128", delta: "+14 today" },
  { label: "활성 직원", value: "486", delta: "3 offices" },
  { label: "이번 달 정산", value: "EUR 184K", delta: "+8.2%" },
  { label: "처리 성공률", value: "98.4%", delta: "OCR stable" },
];

const documents = [
  { name: "March VAT Invoice Batch", status: "Pending", company: "SINWOO GmbH", amount: "EUR 12,480" },
  { name: "Payroll Supplement Review", status: "Review", company: "SINWOO Europe", amount: "EUR 8,240" },
  { name: "Attendance Correction", status: "Approved", company: "SINWOO Berlin", amount: "-" },
];

const menuItems = [
  { label: "Dashboard", icon: LayoutDashboard },
  { label: "Documents", icon: FileText },
  { label: "Bookkeeping", icon: ReceiptText },
  { label: "Attendance", icon: TimerReset },
  { label: "Employees", icon: Users },
  { label: "Companies", icon: Building2 },
];

export default function HomePage() {
  return (
    <main className="min-h-screen">
      <div className="mx-auto flex min-h-screen max-w-[1600px] gap-6 px-4 py-4 lg:px-6">
        <aside className="hidden w-[280px] shrink-0 rounded-[28px] bg-slate-950 p-6 text-slate-100 shadow-panel lg:flex lg:flex-col">
          <div className="mb-10 flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10 text-lg font-semibold">
              S
            </div>
            <div>
              <p className="text-xs uppercase tracking-[0.32em] text-slate-400">Sinwoo</p>
              <h1 className="text-xl font-semibold">Operations Cloud</h1>
            </div>
          </div>

          <nav className="space-y-2">
            {menuItems.map(({ label, icon: Icon }, index) => (
              <button
                key={label}
                className={`flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm transition ${
                  index === 0
                    ? "bg-white text-slate-950"
                    : "text-slate-300 hover:bg-white/10 hover:text-white"
                }`}
              >
                <Icon className="h-4 w-4" />
                <span>{label}</span>
              </button>
            ))}
          </nav>

          <Card className="mt-auto border-white/10 bg-white/5 text-slate-100">
            <CardHeader>
              <CardTitle className="text-base">3-Language Ready</CardTitle>
              <CardDescription className="text-slate-300">
                Korean, English, German onboarding prepared from day one.
              </CardDescription>
            </CardHeader>
          </Card>
        </aside>

        <section className="flex-1">
          <div className="overflow-hidden rounded-[32px] border border-white/70 bg-white/80 shadow-panel backdrop-blur">
            <header className="flex flex-col gap-4 border-b border-slate-200/70 px-6 py-5 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm text-slate-500">Global B2B Operations Platform</p>
                <h2 className="text-2xl font-semibold tracking-tight text-slate-950">Executive Dashboard</h2>
              </div>
              <div className="flex items-center gap-3">
                <Button variant="outline" className="gap-2">
                  <Globe2 className="h-4 w-4" />
                  KO / EN / DE
                </Button>
                <Button variant="outline" size="icon">
                  <Bell className="h-4 w-4" />
                </Button>
                <Button>New Document</Button>
              </div>
            </header>

            <div className="space-y-6 p-6">
              <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                {metricCards.map((item) => (
                  <Card key={item.label} className="border-white bg-gradient-to-br from-white to-slate-50">
                    <CardHeader className="pb-3">
                      <CardDescription>{item.label}</CardDescription>
                      <CardTitle className="text-3xl">{item.value}</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <Badge variant="secondary">{item.delta}</Badge>
                    </CardContent>
                  </Card>
                ))}
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.5fr_1fr]">
                <Card className="overflow-hidden">
                  <CardHeader className="flex flex-row items-start justify-between space-y-0">
                    <div>
                      <CardTitle>OCR Processing Overview</CardTitle>
                      <CardDescription>문서 수집부터 검수까지 한 화면에서 확인합니다.</CardDescription>
                    </div>
                    <Badge className="bg-success text-success-foreground hover:bg-success">Stable</Badge>
                  </CardHeader>
                  <CardContent>
                    <div className="grid gap-4 md:grid-cols-3">
                      {[
                        { label: "Uploaded", value: 214, tone: "bg-slate-950" },
                        { label: "In Review", value: 43, tone: "bg-amber-400" },
                        { label: "Posted", value: 171, tone: "bg-emerald-500" },
                      ].map((bar) => (
                        <div key={bar.label} className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                          <div className="mb-6 flex items-end justify-between">
                            <span className="text-sm text-slate-500">{bar.label}</span>
                            <span className="text-2xl font-semibold">{bar.value}</span>
                          </div>
                          <div className="h-32 rounded-2xl bg-white p-3">
                            <div className={`h-full rounded-xl ${bar.tone}`} />
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>

                <Card className="bg-slate-950 text-slate-100">
                  <CardHeader>
                    <CardTitle>Today&apos;s Queue</CardTitle>
                    <CardDescription className="text-slate-300">
                      운영팀이 바로 처리할 우선순위 작업입니다.
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {[
                      "독일 법인 신규 테넌트 검토",
                      "Payroll attachment OCR 재분류",
                      "Attendance exception 승인 대기",
                    ].map((task, index) => (
                      <div key={task} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                        <div className="mb-2 text-xs uppercase tracking-[0.24em] text-slate-400">
                          Task {index + 1}
                        </div>
                        <p className="text-sm text-slate-100">{task}</p>
                      </div>
                    ))}
                  </CardContent>
                </Card>
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
                <Card>
                  <CardHeader>
                    <CardTitle>Recent Documents</CardTitle>
                    <CardDescription>관리자 페이지에서 이어질 문서 워크플로우 미리보기입니다.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    {documents.map((document) => (
                      <div
                        key={document.name}
                        className="grid gap-3 rounded-2xl border border-slate-200 p-4 md:grid-cols-[1.8fr_0.8fr_0.8fr_0.6fr]"
                      >
                        <div>
                          <p className="font-medium text-slate-950">{document.name}</p>
                          <p className="text-sm text-slate-500">{document.company}</p>
                        </div>
                        <div className="text-sm text-slate-600">{document.amount}</div>
                        <div className="text-sm text-slate-600">Assigned to Finance</div>
                        <div>
                          <Badge
                            variant={document.status === "Approved" ? "default" : "secondary"}
                            className={
                              document.status === "Approved"
                                ? "bg-success text-success-foreground"
                                : document.status === "Review"
                                  ? "bg-warning text-warning-foreground"
                                  : ""
                            }
                          >
                            {document.status}
                          </Badge>
                        </div>
                      </div>
                    ))}
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>Platform Direction</CardTitle>
                    <CardDescription>첫 프론트 부트스트랩에 반영된 설계 원칙입니다.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3 text-sm text-slate-600">
                    <div className="rounded-2xl bg-slate-50 p-4">
                      고객 포털과 관리자 콘솔을 하나의 디자인 시스템으로 분리 운영합니다.
                    </div>
                    <div className="rounded-2xl bg-slate-50 p-4">
                      다국어, 테넌트 컨텍스트, 권한 기반 내비게이션을 초기에 고려합니다.
                    </div>
                    <div className="rounded-2xl bg-slate-50 p-4">
                      shadcn/ui 스타일 컴포넌트로 커스터마이징 여지를 크게 남깁니다.
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

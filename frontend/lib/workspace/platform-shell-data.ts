import type { LoginLocale } from "@/lib/i18n/login-content";

export type WorkspaceMode = "client" | "admin";
export const WORKSPACE_RUNTIME_PAGE_IDS = [
  "MNU_CUSTOMER_MY_TIME",
  "MNU_CUSTOMER_TEAM_TIME",
  "MNU_CUSTOMER_LEAVE",
  "MNU_CUSTOMER_WORK_TIME",
  "MNU_CUSTOMER_WORK_TIME_HISTORY",
  "MNU_CUSTOMER_ADMIN_HOME",
  "MNU_ADMIN_WORK_TIME_HISTORY",
] as const;
export type WorkspaceRuntimePageId = (typeof WORKSPACE_RUNTIME_PAGE_IDS)[number];

type FallbackMenuSeed = {
  id: string;
  children?: FallbackMenuSeed[];
};

export type MenuNode = {
  id: string;
  title: string;
  icon?: string;
  closable?: boolean;
  children?: MenuNode[];
};

export type TabItem = {
  id: string;
  title: string;
};

export type KpiItem = {
  label: string;
  value: string;
  delta: string;
};

export type FeedItem = {
  title: string;
  meta: string;
  emphasis?: "default" | "warning" | "success";
};

export type ViewModel = {
  eyebrow: string;
  title: string;
  description: string;
  kpis: KpiItem[];
  highlights: FeedItem[];
  gridTitle: string;
  gridRows: Array<{ name: string; owner: string; status: string; updated: string }>;
};

export type WorkspaceModeConfig = {
  mode: WorkspaceMode;
  label: string;
  shellTitle: string;
  shellSubtitle: string;
  menus: MenuNode[];
  defaultTabId: string;
};

type WorkspaceModeSeed = {
  mode: WorkspaceMode;
  defaultTabId: string;
  menus: FallbackMenuSeed[];
};

type MenuPresentationMetadata = Pick<MenuNode, "icon" | "closable">;

// Developer note:
// The API menu tree is the primary source of truth.
// This fallback seed intentionally keeps only the minimum structure needed
// to boot the shell safely when menu API data is missing or unavailable.
const fallbackWorkspaceModeSeeds: WorkspaceModeSeed[] = [
  {
    mode: "client",
    defaultTabId: "MNU_CUSTOMER_DASH",
    menus: [
      {
        id: "MNU_CUSTOMER_DASH",
      },
      {
        id: "MNU_CUSTOMER_ATTENDANCE",
        children: [
          { id: "MNU_CUSTOMER_MY_TIME" },
          { id: "MNU_CUSTOMER_TEAM_TIME" },
          { id: "MNU_CUSTOMER_LEAVE" },
        ],
      },
      {
        id: "MNU_CUSTOMER_REPORTS",
        children: [{ id: "MNU_CUSTOMER_WORK_TIME" }],
      },
      {
        id: "MNU_CUSTOMER_ADMIN",
        children: [{ id: "MNU_CUSTOMER_ADMIN_HOME" }],
      },
    ],
  },
  {
    mode: "admin",
    defaultTabId: "admin-overview",
    menus: [
      { id: "admin-overview" },
      {
        id: "tenant-control",
        children: [
          { id: "tenant-list" },
          {
            id: "tenant-settings",
            children: [
              { id: "company-profile" },
              { id: "workspace-policy" },
              { id: "menu-policy" },
            ],
          },
        ],
      },
      {
        id: "authorization",
        children: [
          {
            id: "menu-management",
            children: [
              { id: "menu-tree" },
              { id: "tab-policy" },
              {
                id: "depth-policy",
                children: [{ id: "depth-editor" }],
              },
            ],
          },
          { id: "role-policy" },
        ],
      },
      {
        id: "billing-ops",
        children: [
          { id: "plan-catalog" },
          { id: "payment-gates" },
          { id: "upgrade-queue" },
        ],
      },
      {
        id: "audit-center",
        children: [
          { id: "change-history" },
          { id: "access-logs" },
          { id: "compliance" },
        ],
      },
      {
        id: "MNU_ADMIN_REPORTS",
        children: [
          {
            id: "MNU_ADMIN_WORK_TIME",
            children: [{ id: "MNU_ADMIN_WORK_TIME_HISTORY" }],
          },
        ],
      },
    ],
  },
];

const fallbackMenuPresentationMetadata: Record<string, MenuPresentationMetadata> = {
  "client-dashboard": { icon: "grid" },
  MNU_CUSTOMER_DASH: { icon: "grid" },
  MNU_CUSTOMER_ATTENDANCE: { icon: "briefcase" },
  MNU_CUSTOMER_REPORTS: { icon: "grid" },
  MNU_CUSTOMER_ADMIN: { icon: "shield" },
  MNU_CUSTOMER_MY_TIME: { icon: "briefcase" },
  MNU_CUSTOMER_TEAM_TIME: { icon: "users" },
  MNU_CUSTOMER_LEAVE: { icon: "briefcase" },
  MNU_CUSTOMER_WORK_TIME: { icon: "grid" },
  MNU_CUSTOMER_ADMIN_HOME: { icon: "shield" },
  workspace: { icon: "briefcase" },
  people: { icon: "users" },
  "billing-center": { icon: "credit-card" },
  "admin-overview": { icon: "shield" },
  "tenant-control": { icon: "building" },
  authorization: { icon: "key" },
  "billing-ops": { icon: "wallet" },
  "audit-center": { icon: "activity" },
  MNU_ADMIN_REPORTS: { icon: "grid" },
};

const fallbackMenuTitleTranslations: Partial<Record<string, Partial<Record<LoginLocale, string>>>> = {
  "client-dashboard": { en: "Dashboard", de: "Dashboard", ko: "대시보드" },
  MNU_CUSTOMER_DASH: { en: "Dashboard", de: "Dashboard", ko: "대시보드" },
  MNU_CUSTOMER_ATTENDANCE: { en: "Attendance", de: "Zeiterfassung", ko: "근태" },
  MNU_CUSTOMER_REPORTS: { en: "Reports", de: "Berichte", ko: "리포트" },
  MNU_CUSTOMER_ADMIN: { en: "Admin", de: "Admin", ko: "관리" },
  MNU_CUSTOMER_MY_TIME: { en: "My Work Time", de: "Meine Arbeitszeit", ko: "내 근무시간" },
  MNU_CUSTOMER_TEAM_TIME: { en: "Team Work Time", de: "Team Arbeitszeit", ko: "팀 근무시간" },
  MNU_CUSTOMER_LEAVE: { en: "Request Leave", de: "Urlaub beantragen", ko: "휴가 신청" },
  MNU_CUSTOMER_WORK_TIME: { en: "Work Time", de: "Arbeitszeit", ko: "근무시간" },
  MNU_CUSTOMER_ADMIN_HOME: { en: "Admin Area", de: "Admin-Bereich", ko: "관리 영역" },
  workspace: { en: "Workspace", de: "Workspace", ko: "워크스페이스" },
  documents: { en: "Documents", de: "Dokumente", ko: "문서" },
  "ocr-inbox": { en: "OCR Inbox", de: "OCR Inbox", ko: "OCR 받은함" },
  "expense-review": { en: "Expense Review", de: "Kostenprüfung", ko: "비용 검토" },
  archive: { en: "Archive", de: "Archiv", ko: "보관함" },
  attendance: { en: "Attendance", de: "Zeiterfassung", ko: "근태" },
  "my-time": { en: "My Time", de: "Meine Zeiten", ko: "내 근무시간" },
  "team-time": { en: "Team Time", de: "Team-Zeiten", ko: "팀 근무현황" },
  leave: { en: "Leave Requests", de: "Urlaubsanträge", ko: "휴가 요청" },
  people: { en: "People", de: "Mitarbeitende", ko: "인원" },
  employees: { en: "Employees", de: "Mitarbeiter", ko: "직원" },
  organization: { en: "Organization", de: "Organisation", ko: "조직" },
  departments: { en: "Departments", de: "Abteilungen", ko: "부서" },
  roles: { en: "Roles", de: "Rollen", ko: "권한" },
  "billing-center": { en: "Billing Center", de: "Billing Center", ko: "결제 센터" },
  subscription: { en: "Subscription", de: "Abonnement", ko: "구독" },
  payments: { en: "Payments", de: "Zahlungen", ko: "결제" },
  MNU_CUSTOMER_WORK_TIME_HISTORY: { en: "History", de: "Verlauf", ko: "이력" },
  "admin-overview": { en: "Overview", de: "Überblick", ko: "개요" },
  "tenant-control": { en: "Tenant Control", de: "Mandantenverwaltung", ko: "테넌트 관리" },
  "tenant-list": { en: "Tenant List", de: "Mandanten 목록", ko: "테넌트 목록" },
  "tenant-settings": { en: "Tenant Settings", de: "Mandanten 설정", ko: "테넌트 설정" },
  "company-profile": { en: "Company Profile", de: "Unternehmensprofil", ko: "회사 프로필" },
  "workspace-policy": { en: "Workspace Policy", de: "Workspace 정책", ko: "워크스페이스 정책" },
  "menu-policy": { en: "Menu Policy", de: "Menü 정책", ko: "메뉴 정책" },
  authorization: { en: "Authorization", de: "권한 관리", ko: "권한 관리" },
  "menu-management": { en: "Menu Management", de: "Menü 관리", ko: "메뉴 관리" },
  "menu-tree": { en: "Menu Tree", de: "Menü 트리", ko: "메뉴 트리" },
  "tab-policy": { en: "Tab Policy", de: "Tab 정책", ko: "탭 정책" },
  "depth-policy": { en: "Depth Policy", de: "Depth 정책", ko: "뎁스 정책" },
  "depth-editor": { en: "Depth 1-4 Editor", de: "Depth 1-4 Editor", ko: "1-4뎁스 편집기" },
  "role-policy": { en: "Role Policy", de: "Rollen 정책", ko: "권한 정책" },
  "billing-ops": { en: "Billing Ops", de: "결제 운영", ko: "결제 운영" },
  "plan-catalog": { en: "Plan Catalog", de: "Plan-Katalog", ko: "플랜 카탈로그" },
  "payment-gates": { en: "Payment Gates", de: "Payment Gates", ko: "결제 게이트" },
  "upgrade-queue": { en: "Upgrade Queue", de: "Upgrade Queue", ko: "업그레이드 큐" },
  "audit-center": { en: "Audit Center", de: "감사 센터", ko: "감사 센터" },
  "change-history": { en: "Change History", de: "Änderungsverlauf", ko: "변경 이력" },
  "access-logs": { en: "Access Logs", de: "Zugriffsprotokolle", ko: "접속 로그" },
  compliance: { en: "Compliance Desk", de: "Compliance Desk", ko: "컴플라이언스 데스크" },
  MNU_ADMIN_REPORTS: { en: "Reports", de: "Berichte", ko: "리포트" },
  MNU_ADMIN_WORK_TIME: { en: "Work Time", de: "Arbeitszeit", ko: "근태" },
  MNU_ADMIN_WORK_TIME_HISTORY: { en: "History", de: "Verlauf", ko: "이력" },
};

// Developer note:
// These are fallback preview models only.
// They exist to keep the shell readable until a tab gets a real API/runtime page.
// They must never override an API-backed page component at runtime.
const fallbackWorkspaceViewModels: Record<string, ViewModel> = {
  "my-profile": {
    eyebrow: "profile settings",
    title: "Personal profile",
    description: "Update your personal information, account preferences, language, and security settings from one dedicated tab.",
    kpis: [
      { label: "Account status", value: "Active", delta: "Verified" },
      { label: "Locale", value: "3", delta: "DE / EN / KO" },
      { label: "Security", value: "Good", delta: "Password active" },
      { label: "Last update", value: "Today", delta: "Profile synced" },
    ],
    highlights: [
      { title: "Edit display name, contact number, and profile details", meta: "Personal information" },
      { title: "Change password and security options", meta: "Account security", emphasis: "warning" },
      { title: "Manage notification and locale preferences", meta: "Workspace preferences", emphasis: "success" },
    ],
    gridTitle: "Profile settings",
    gridRows: [
      { name: "Display name", owner: "Personal info", status: "Editable", updated: "Now" },
      { name: "Password", owner: "Security", status: "Protected", updated: "Today" },
      { name: "Locale preferences", owner: "Preferences", status: "Available", updated: "Today" },
    ],
  },
  "client-dashboard": {
    eyebrow: "customer overview",
    title: "Workspace pulse",
    description: "A clean first screen for customer members and customer admins. Keep the product feeling strong and the task density controlled.",
    kpis: [
      { label: "Open approvals", value: "18", delta: "+4 today" },
      { label: "OCR queue", value: "42", delta: "7 flagged" },
      { label: "Attendance", value: "96%", delta: "On-time check-in" },
      { label: "Billing", value: "Growth", delta: "Healthy" },
    ],
    highlights: [
      { title: "4 invoices require approval", meta: "Finance admin · OCR review", emphasis: "warning" },
      { title: "2 leave requests need attention", meta: "HR admin · Team review" },
      { title: "Subscription renewed until 2026-06-30", meta: "Billing center", emphasis: "success" },
    ],
    gridTitle: "Recent activity",
    gridRows: [
      { name: "Invoice 2026-0412", owner: "Kim Jiyoung", status: "Needs review", updated: "8 min ago" },
      { name: "Annual leave request", owner: "Anna Schmidt", status: "Waiting", updated: "13 min ago" },
      { name: "Attendance sync", owner: "System", status: "Completed", updated: "26 min ago" },
    ],
  },
  MNU_CUSTOMER_DASH: {
    eyebrow: "customer overview",
    title: "Workspace pulse",
    description: "A clean first screen for customer members and customer admins. Keep the product feeling strong and the task density controlled.",
    kpis: [
      { label: "Open approvals", value: "18", delta: "+4 today" },
      { label: "OCR queue", value: "42", delta: "7 flagged" },
      { label: "Attendance", value: "96%", delta: "On-time check-in" },
      { label: "Billing", value: "Growth", delta: "Healthy" },
    ],
    highlights: [
      { title: "4 invoices require approval", meta: "Finance admin · OCR review", emphasis: "warning" },
      { title: "2 leave requests need attention", meta: "HR admin · Team review" },
      { title: "Subscription renewed until 2026-06-30", meta: "Billing center", emphasis: "success" },
    ],
    gridTitle: "Recent activity",
    gridRows: [
      { name: "Invoice 2026-0412", owner: "Kim Jiyoung", status: "Needs review", updated: "8 min ago" },
      { name: "Annual leave request", owner: "Anna Schmidt", status: "Waiting", updated: "13 min ago" },
      { name: "Attendance sync", owner: "System", status: "Completed", updated: "26 min ago" },
    ],
  },
  documents: {
    eyebrow: "documents",
    title: "Document workspace",
    description: "OCR inbox, review queue, and archive operations live in one place with minimal clutter.",
    kpis: [
      { label: "New uploads", value: "26", delta: "Today" },
      { label: "Mapped", value: "14", delta: "Ready for posting" },
      { label: "Rejected", value: "3", delta: "Needs correction" },
      { label: "Archive size", value: "1.2 TB", delta: "GoBD tracked" },
    ],
    highlights: [
      { title: "OCR mismatch on supplier VAT", meta: "Invoice #DE-2031", emphasis: "warning" },
      { title: "Archive retention synced", meta: "Germany-ready evidence" },
      { title: "Expense review ready for finance", meta: "12 items", emphasis: "success" },
    ],
    gridTitle: "Document queue",
    gridRows: [
      { name: "Supplier invoice · DE-2031", owner: "Finance Admin", status: "Mismatch", updated: "2 min ago" },
      { name: "Travel expense bundle", owner: "Member Upload", status: "Ready", updated: "11 min ago" },
      { name: "Tax evidence export", owner: "System", status: "Archived", updated: "21 min ago" },
    ],
  },
  attendance: {
    eyebrow: "attendance",
    title: "Attendance control board",
    description: "Designed for quick review, exceptions, and approvals without drowning the user in ERP density.",
    kpis: [
      { label: "Checked in", value: "148", delta: "12 online" },
      { label: "Late arrivals", value: "5", delta: "Needs action" },
      { label: "Leave requests", value: "9", delta: "3 urgent" },
      { label: "Sync health", value: "100%", delta: "Stable" },
    ],
    highlights: [
      { title: "3 employees missing check-in", meta: "Operations queue", emphasis: "warning" },
      { title: "Leave calendar updated", meta: "HR team", emphasis: "success" },
      { title: "Attendance rule set active", meta: "Germany-ready time policy" },
    ],
    gridTitle: "Team watchlist",
    gridRows: [
      { name: "Operations Team A", owner: "Team leader", status: "Stable", updated: "Now" },
      { name: "Finance Team", owner: "Finance admin", status: "1 missing", updated: "7 min ago" },
      { name: "Field Sales", owner: "HR admin", status: "Late check-in", updated: "15 min ago" },
    ],
  },
  MNU_CUSTOMER_ATTENDANCE: {
    eyebrow: "attendance",
    title: "Attendance control board",
    description: "Designed for quick review, exceptions, and approvals without drowning the user in ERP density.",
    kpis: [
      { label: "Checked in", value: "148", delta: "12 online" },
      { label: "Late arrivals", value: "5", delta: "Needs action" },
      { label: "Leave requests", value: "9", delta: "3 urgent" },
      { label: "Sync health", value: "100%", delta: "Stable" },
    ],
    highlights: [
      { title: "3 employees missing check-in", meta: "Operations queue", emphasis: "warning" },
      { title: "Leave calendar updated", meta: "HR team", emphasis: "success" },
      { title: "Attendance rule set active", meta: "Germany-ready time policy" },
    ],
    gridTitle: "Team watchlist",
    gridRows: [
      { name: "Operations Team A", owner: "Team leader", status: "Stable", updated: "Now" },
      { name: "Finance Team", owner: "Finance admin", status: "1 missing", updated: "7 min ago" },
      { name: "Field Sales", owner: "HR admin", status: "Late check-in", updated: "15 min ago" },
    ],
  },
  MNU_CUSTOMER_MY_TIME: {
    eyebrow: "attendance",
    title: "My Work Time",
    description: "Review your own work time entries, personal calendar, and recent attendance activity.",
    kpis: [
      { label: "This month", value: "168h", delta: "Recorded" },
      { label: "Today", value: "08:42", delta: "Worked" },
      { label: "Leave", value: "2", delta: "Approved" },
      { label: "Status", value: "Normal", delta: "On track" },
    ],
    highlights: [
      { title: "Monthly personal attendance view", meta: "Calendar and saved entries" },
      { title: "Holiday and leave markers stay visible", meta: "Personal schedule", emphasis: "success" },
      { title: "Manual entries remain available where allowed", meta: "Attendance policy" },
    ],
    gridTitle: "Recent entries",
    gridRows: [
      { name: "2026-04-18", owner: "My calendar", status: "Recorded", updated: "Today" },
      { name: "2026-04-17", owner: "My calendar", status: "Recorded", updated: "Yesterday" },
      { name: "2026-04-16", owner: "My calendar", status: "Holiday", updated: "2 days ago" },
    ],
  },
  MNU_CUSTOMER_TEAM_TIME: {
    eyebrow: "attendance",
    title: "Team Work Time",
    description: "Review team-level work time status and attendance exceptions from an operational table view.",
    kpis: [
      { label: "Team members", value: "24", delta: "Visible" },
      { label: "Missing", value: "3", delta: "Need review" },
      { label: "Late", value: "2", delta: "Today" },
      { label: "Leave", value: "4", delta: "Planned" },
    ],
    highlights: [
      { title: "Team work time is limited to customer admin roles", meta: "Role-scoped view", emphasis: "warning" },
      { title: "Missing check-in records surface first", meta: "Operational watchlist" },
      { title: "Leave and holiday context stay visible", meta: "Team schedule", emphasis: "success" },
    ],
    gridTitle: "Team watchlist",
    gridRows: [
      { name: "Anna Schmidt", owner: "Sales", status: "Checked in", updated: "08:57" },
      { name: "Minho Park", owner: "Finance", status: "Missing", updated: "09:12" },
      { name: "Lena Bauer", owner: "Operations", status: "Leave", updated: "Today" },
    ],
  },
  MNU_CUSTOMER_WORK_TIME: {
    eyebrow: "reports",
    title: "Work Time",
    description: "Filter monthly work time records and export report results.",
    kpis: [
      { label: "Month", value: "Current", delta: "Default" },
      { label: "Scope", value: "User", delta: "Role aware" },
      { label: "Export", value: "PDF/XLSX", delta: "Available" },
      { label: "Records", value: "Live", delta: "Attendance" },
    ],
    highlights: [
      { title: "Report filters by employee and department", meta: "Operational reporting" },
      { title: "Exports follow the filtered result set", meta: "PDF and Excel", emphasis: "success" },
      { title: "Own records remain default for non-admin users", meta: "Role-aware scope" },
    ],
    gridTitle: "Work time report",
    gridRows: [
      { name: "Target month", owner: "Default", status: "Current month", updated: "Now" },
      { name: "Employee scope", owner: "Role policy", status: "Resolved", updated: "Now" },
      { name: "Department scope", owner: "Role policy", status: "Resolved", updated: "Now" },
    ],
  },
  MNU_CUSTOMER_ADMIN_HOME: {
    eyebrow: "admin",
    title: "Admin Area",
    description: "Placeholder area for future customer admin functions.",
    kpis: [
      { label: "Access", value: "Admin", delta: "Role required" },
      { label: "Menus", value: "Planned", delta: "Future" },
      { label: "Scope", value: "Customer", delta: "Client mode" },
      { label: "State", value: "Ready", delta: "Placeholder" },
    ],
    highlights: [
      { title: "Customer admin menus will expand here", meta: "Planned runtime group" },
      { title: "Current page is a placeholder scaffold", meta: "No business logic change" },
      { title: "Visibility stays limited to customer admin and above", meta: "Role-based access", emphasis: "success" },
    ],
    gridTitle: "Admin placeholder",
    gridRows: [
      { name: "Admin area", owner: "Client workspace", status: "Placeholder", updated: "Now" },
      { name: "Future menus", owner: "Customer admin", status: "Planned", updated: "Next step" },
      { name: "Structure", owner: "Workspace shell", status: "Ready", updated: "Now" },
    ],
  },
  MNU_CUSTOMER_WORK_LOCATIONS: {
    eyebrow: "work locations",
    title: "Dispatch and work region control",
    description: "Manage headquarters defaults, client sites, and employee work-location assignments so local holiday rules follow the real workplace.",
    kpis: [
      { label: "HQ rule", value: "Default", delta: "Company fallback" },
      { label: "Client sites", value: "6", delta: "Regional coverage" },
      { label: "Assigned staff", value: "128", delta: "Workplace override" },
      { label: "Holiday sync", value: "Live", delta: "Open API" },
    ],
    highlights: [
      { title: "Frankfurt remains the headquarters default", meta: "Company baseline" },
      { title: "Berlin and Munich dispatch sites override HQ holidays", meta: "Work location priority", emphasis: "warning" },
      { title: "Customer admins can maintain work locations directly", meta: "Delegated control", emphasis: "success" },
    ],
    gridTitle: "Work location roster",
    gridRows: [
      { name: "Frankfurt HQ", owner: "Headquarters", status: "Default", updated: "Today" },
      { name: "QCells Berlin", owner: "Client site", status: "Active", updated: "12 min ago" },
      { name: "Samsung Semiconductor Munich", owner: "Client site", status: "Active", updated: "28 min ago" },
    ],
    /*
    de: {
      eyebrow: "einsatzorte",
      title: "Einsatz- und Regionssteuerung",
      description: "Verwalten Sie Hauptsitz-Standards, Kundeneinsatzorte und Mitarbeiterzuordnungen, damit Feiertage nach dem realen Arbeitsort angewendet werden.",
    },
    ko: {
      eyebrow: "근무지",
      title: "파견지 및 지역 관리",
      description: "본사 기준값, 고객사 파견지, 직원 근무지 배정을 함께 관리하여 실제 근무 지역의 휴일 규칙이 적용되도록 합니다.",
    },
    */
  },
  "billing-center": {
    eyebrow: "billing",
    title: "Billing and upgrade gateway",
    description: "This area sits between customer member access and customer admin capability. Paid access changes what users can see.",
    kpis: [
      { label: "Current plan", value: "Growth", delta: "Monthly" },
      { label: "Upgrade requests", value: "2", delta: "Pending" },
      { label: "Invoices", value: "11", delta: "Current cycle" },
      { label: "Payment health", value: "OK", delta: "No failures" },
    ],
    highlights: [
      { title: "Admin menu gate is active", meta: "Payment entitlement required" },
      { title: "One upgrade needs approval", meta: "Billing Ops", emphasis: "warning" },
      { title: "Renewal completed successfully", meta: "Stripe / PG", emphasis: "success" },
    ],
    gridTitle: "Billing events",
    gridRows: [
      { name: "Growth plan renewal", owner: "System", status: "Completed", updated: "Today" },
      { name: "Enterprise quote request", owner: "Customer Admin", status: "Waiting", updated: "29 min ago" },
      { name: "Payment method review", owner: "Billing team", status: "Open", updated: "1 hr ago" },
    ],
  },
  MNU_CUSTOMER_WORK_TIME_HISTORY: {
    eyebrow: "reports",
    title: "Work time history",
    description: "Monthly work time reporting for employees, departments, and exports.",
    kpis: [
      { label: "Current month", value: "Now", delta: "Default scope" },
      { label: "Scope", value: "Team", delta: "By role" },
      { label: "Export", value: "2", delta: "Excel / PDF" },
      { label: "Records", value: "Live", delta: "Attendance table" },
    ],
    highlights: [
      { title: "Monthly work history filters by employee and department", meta: "Reporting query" },
      { title: "Normal users stay scoped to their own records", meta: "Role-based access", emphasis: "success" },
      { title: "Admins can export the filtered result set", meta: "Excel and PDF", emphasis: "warning" },
    ],
    gridTitle: "Work time report",
    gridRows: [
      { name: "Target month", owner: "Default", status: "Current month", updated: "Now" },
      { name: "Employee scope", owner: "Role policy", status: "Resolved", updated: "Now" },
      { name: "Department scope", owner: "Role policy", status: "Resolved", updated: "Now" },
    ],
  },
  MNU_CUSTOMER_REPORTS: {
    eyebrow: "reports",
    title: "Report center",
    description: "Review monthly work records, filter by employee or department, and export the result for reporting usage.",
    kpis: [
      { label: "Current month", value: "Now", delta: "Default scope" },
      { label: "Scope", value: "Team", delta: "By role" },
      { label: "Export", value: "2", delta: "Excel / PDF" },
      { label: "Records", value: "Live", delta: "Attendance table" },
    ],
    highlights: [
      { title: "Monthly work history filters by employee and department", meta: "Reporting query" },
      { title: "Normal users stay scoped to their own records", meta: "Role-based access", emphasis: "success" },
      { title: "Admins can export the filtered result set", meta: "Excel and PDF", emphasis: "warning" },
    ],
    gridTitle: "Work time report",
    gridRows: [
      { name: "Target month", owner: "Default", status: "Current month", updated: "Now" },
      { name: "Employee scope", owner: "Role policy", status: "Resolved", updated: "Now" },
      { name: "Department scope", owner: "Role policy", status: "Resolved", updated: "Now" },
    ],
  },
  "admin-overview": {
    eyebrow: "platform admin",
    title: "Control tower",
    description: "A command-center dashboard for super admins who need tenants, billing, authorization, and audit signals together.",
    kpis: [
      { label: "Tenants", value: "24", delta: "3 new" },
      { label: "Paid admins", value: "17", delta: "Gate active" },
      { label: "Open incidents", value: "5", delta: "2 urgent" },
      { label: "Audit alerts", value: "9", delta: "Needs review" },
    ],
    highlights: [
      { title: "Menu depth editor changed 4 items", meta: "Role policy", emphasis: "warning" },
      { title: "Billing gate blocked 2 customer admins", meta: "Expected behavior" },
      { title: "Daily compliance export archived", meta: "GoBD / Audit", emphasis: "success" },
    ],
    gridTitle: "Platform events",
    gridRows: [
      { name: "Tenant Sinwoo ITC GmbH", owner: "Platform Admin", status: "Healthy", updated: "Now" },
      { name: "Role policy rollout", owner: "Super Admin", status: "Propagating", updated: "6 min ago" },
      { name: "Billing gate mismatch", owner: "Billing Ops", status: "Needs review", updated: "18 min ago" },
    ],
  },
  MNU_ADMIN_WORK_TIME_HISTORY: {
    eyebrow: "reports",
    title: "Work time history",
    description: "Monthly work time reporting for employees, departments, and exports.",
    kpis: [
      { label: "Current month", value: "Now", delta: "Default scope" },
      { label: "Scope", value: "Tenant", delta: "Admin view" },
      { label: "Export", value: "2", delta: "Excel / PDF" },
      { label: "Records", value: "Live", delta: "Attendance table" },
    ],
    highlights: [
      { title: "Monthly work history filters by employee and department", meta: "Reporting query" },
      { title: "Normal users stay scoped to their own records", meta: "Role-based access", emphasis: "success" },
      { title: "Admins can export the filtered result set", meta: "Excel and PDF", emphasis: "warning" },
    ],
    gridTitle: "Work time report",
    gridRows: [
      { name: "Target month", owner: "Default", status: "Current month", updated: "Now" },
      { name: "Employee scope", owner: "Role policy", status: "Resolved", updated: "Now" },
      { name: "Department scope", owner: "Role policy", status: "Resolved", updated: "Now" },
    ],
  },
  "tenant-control": {
    eyebrow: "tenant control",
    title: "Tenant administration",
    description: "Manage tenant profile, workspace policy, and menu visibility from one administrable tree.",
    kpis: [
      { label: "Active tenants", value: "24", delta: "Internal + customer" },
      { label: "Trials", value: "4", delta: "Conversion watch" },
      { label: "Frozen", value: "1", delta: "Billing issue" },
      { label: "Workspace policies", value: "29", delta: "Configured" },
    ],
    highlights: [
      { title: "2 tenants need menu policy updates", meta: "Depth mismatch", emphasis: "warning" },
      { title: "New tenant onboarding complete", meta: "HR + Finance stack", emphasis: "success" },
      { title: "Internal tenant remains billing-free", meta: "Expected" },
    ],
    gridTitle: "Tenant list",
    gridRows: [
      { name: "Sinwoo ITC GmbH", owner: "Internal", status: "Free", updated: "Today" },
      { name: "Mitte Logistics GmbH", owner: "Customer", status: "Growth", updated: "9 min ago" },
      { name: "Hanse Trade EU", owner: "Customer", status: "Trial", updated: "26 min ago" },
    ],
  },
  "billing-ops": {
    eyebrow: "billing ops",
    title: "Billing gate operations",
    description: "The boundary between customer members and paid customer admins should be obvious, configurable, and auditable.",
    kpis: [
      { label: "Gate rules", value: "12", delta: "Applied" },
      { label: "Upgrade queue", value: "7", delta: "2 urgent" },
      { label: "Payment fails", value: "1", delta: "Today" },
      { label: "Free tenants", value: "5", delta: "Including internal" },
    ],
    highlights: [
      { title: "Admin access blocked until payment", meta: "Customer workspace", emphasis: "warning" },
      { title: "Plan catalog synchronized", meta: "Platform config", emphasis: "success" },
      { title: "1 payment dispute needs review", meta: "Billing team" },
    ],
    gridTitle: "Billing operation log",
    gridRows: [
      { name: "Growth → Enterprise", owner: "Billing Ops", status: "Pending", updated: "Now" },
      { name: "Customer admin gate", owner: "Policy Engine", status: "Applied", updated: "5 min ago" },
      { name: "Invoice retry", owner: "PG Worker", status: "Completed", updated: "22 min ago" },
    ],
  },
};

const modeTranslations: Record<
  WorkspaceMode,
  Record<LoginLocale, Pick<WorkspaceModeConfig, "label" | "shellTitle" | "shellSubtitle">>
> = {
  client: {
    en: {
      label: "Client workspace",
      shellTitle: "Customer operations portal",
      shellSubtitle: "Work on approvals, people, documents, and billing from one product shell.",
    },
    de: {
      label: "Kundenarbeitsbereich",
      shellTitle: "Kundenportal",
      shellSubtitle: "Freigaben, Mitarbeitende, Dokumente und Abrechnung in einer Arbeitsumgebung verwalten.",
    },
    ko: {
      label: "고객 워크스페이스",
      shellTitle: "고객 운영 포털",
      shellSubtitle: "승인, 인사, 문서, 결제를 하나의 워크스페이스에서 관리합니다.",
    },
  },
  admin: {
    en: {
      label: "Admin console",
      shellTitle: "Platform control tower",
      shellSubtitle: "Supervise tenants, approvals, billing gates, and audit evidence across the platform.",
    },
    de: {
      label: "Admin-Konsole",
      shellTitle: "Plattform-Leitstand",
      shellSubtitle: "Mandanten, Freigaben, Billing-Gates und Audit-Nachweise zentral steuern.",
    },
    ko: {
      label: "관리자 콘솔",
      shellTitle: "플랫폼 관제 센터",
      shellSubtitle: "테넌트, 승인, 결제 게이트, 감사 근거를 플랫폼 전반에서 통제합니다.",
    },
  },
};

const viewTranslations: Partial<Record<string, Partial<Record<LoginLocale, Partial<ViewModel>>>>> = {
  "my-profile": {
    de: {
      eyebrow: "persönliche einstellungen",
      title: "Persönliche Daten",
      description: "Persönliche Daten, Kontoeinstellungen, Sprache und Sicherheit in einem eigenen Tab verwalten.",
      kpis: [
        { label: "Kontostatus", value: "Aktiv", delta: "Verifiziert" },
        { label: "Sprache", value: "3", delta: "DE / EN / KO" },
        { label: "Sicherheit", value: "Gut", delta: "Passwort aktiv" },
        { label: "Letzte Änderung", value: "Heute", delta: "Profil synchronisiert" },
      ],
      highlights: [
        { title: "Anzeigename, Kontakt und Profildaten bearbeiten", meta: "Persönliche Informationen" },
        { title: "Passwort und Sicherheitsoptionen ändern", meta: "Kontosicherheit", emphasis: "warning" },
        { title: "Benachrichtigungen und Spracheinstellungen verwalten", meta: "Workspace-Einstellungen", emphasis: "success" },
      ],
      gridTitle: "Profileinstellungen",
      gridRows: [
        { name: "Anzeigename", owner: "Persönliche Infos", status: "Bearbeitbar", updated: "Jetzt" },
        { name: "Passwort", owner: "Sicherheit", status: "Geschützt", updated: "Heute" },
        { name: "Spracheinstellungen", owner: "Einstellungen", status: "Verfügbar", updated: "Heute" },
      ],
    },
    ko: {
      eyebrow: "개인 설정",
      title: "개인정보변경",
      description: "개인정보, 계정 기본설정, 언어, 보안 정보를 하나의 탭에서 관리합니다.",
      kpis: [
        { label: "계정 상태", value: "Active", delta: "Verified" },
        { label: "언어", value: "3", delta: "DE / EN / KO" },
        { label: "보안", value: "Good", delta: "Password active" },
        { label: "최근 변경", value: "Today", delta: "Profile synced" },
      ],
      highlights: [
        { title: "표시 이름, 연락처, 프로필 정보를 수정합니다", meta: "개인정보" },
        { title: "비밀번호와 보안 옵션을 변경합니다", meta: "보안", emphasis: "warning" },
        { title: "알림과 언어 설정을 관리합니다", meta: "환경설정", emphasis: "success" },
      ],
      gridTitle: "프로필 설정",
      gridRows: [
        { name: "표시 이름", owner: "개인정보", status: "Editable", updated: "지금" },
        { name: "비밀번호", owner: "보안", status: "Protected", updated: "오늘" },
        { name: "언어 설정", owner: "환경설정", status: "Available", updated: "오늘" },
      ],
    },
  },
  "client-dashboard": {
    de: {
      eyebrow: "kundenübersicht",
      title: "Workspace-Status",
      description: "Startseite für Mitglieder und Kundenadministratoren mit klarer Informationsdichte und produktnaher Struktur.",
    },
    ko: {
      eyebrow: "고객 개요",
      title: "워크스페이스 현황",
      description: "고객 멤버와 고객 관리자 모두를 위한 첫 화면으로, 제품성은 유지하고 작업 밀도는 과하지 않게 잡았습니다.",
    },
  },
  MNU_CUSTOMER_DASH: {
    de: {
      eyebrow: "kundenübersicht",
      title: "Workspace-Status",
      description: "Startseite für Mitglieder und Kundenadministratoren mit klarer Informationsdichte und produktnaher Struktur.",
    },
    ko: {
      eyebrow: "고객 개요",
      title: "워크스페이스 현황",
      description: "고객 멤버와 고객 관리자 모두를 위한 첫 화면으로, 제품성은 유지하고 작업 밀도는 과하지 않게 잡았습니다.",
    },
  },
  documents: {
    de: {
      eyebrow: "dokumente",
      title: "Dokumenten-Workspace",
      description: "OCR-Inbox, Prüfwarteschlange und Archiv in einer kompakten Arbeitsfläche.",
    },
    ko: {
      eyebrow: "문서",
      title: "문서 워크스페이스",
      description: "OCR 받은함, 검토 큐, 보관 작업을 하나의 화면에서 다룹니다.",
    },
  },
  attendance: {
    de: {
      eyebrow: "zeiterfassung",
      title: "Arbeitszeit-Board",
      description: "Ausnahmen, Freigaben und 누락 없이 빠르게 확인할 수 있는 근태 중심 화면입니다.",
    },
    ko: {
      eyebrow: "근태",
      title: "근태 관제판",
      description: "예외, 승인, 누락을 빠르게 확인할 수 있는 근태 중심 화면입니다.",
    },
  },
  MNU_CUSTOMER_ATTENDANCE: {
    de: {
      eyebrow: "zeiterfassung",
      title: "Arbeitszeit-Board",
      description: "Ausnahmen, Freigaben und 누락 없이 빠르게 확인할 수 있는 근태 중심 화면입니다.",
    },
    ko: {
      eyebrow: "근태",
      title: "근태 관제판",
      description: "예외, 승인, 누락을 빠르게 확인할 수 있는 근태 중심 화면입니다.",
    },
  },
  MNU_CUSTOMER_REPORTS: {
    de: {
      eyebrow: "berichte",
      title: "Report Center",
      description: "Monatliche Arbeitszeitdaten prüfen, nach Mitarbeiter oder Abteilung filtern und Ergebnisse exportieren.",
    },
    ko: {
      eyebrow: "리포트",
      title: "리포트 센터",
      description: "월별 근무기록을 검토하고 직원 또는 부서 기준으로 필터링한 뒤 결과를 내보냅니다.",
    },
  },
  "billing-center": {
    de: {
      eyebrow: "abrechnung",
      title: "Abrechnung und Upgrade",
      description: "Der Bereich zwischen Kundenmitgliedern und zahlenden Kundenadministratoren wird hier gesteuert.",
    },
    ko: {
      eyebrow: "결제",
      title: "결제 및 업그레이드",
      description: "고객 멤버 권한과 고객 관리자 권한 사이의 결제 게이트를 이 영역에서 관리합니다.",
    },
  },
  "admin-overview": {
    de: {
      eyebrow: "plattform admin",
      title: "Kontrollzentrum",
      description: "Zentrale Übersicht für Super-Admins über Mandanten, 권한, 결제, 감사 지표를 한 번에 보여줍니다.",
    },
    ko: {
      eyebrow: "플랫폼 관리자",
      title: "통합 관제",
      description: "슈퍼관리자가 테넌트, 권한, 결제, 감사 지표를 한 번에 보는 메인 화면입니다.",
    },
  },
  "tenant-control": {
    de: {
      eyebrow: "mandantensteuerung",
      title: "Mandantenverwaltung",
      description: "Mandantenprofil, Workspace-정책과 메뉴 노출 규칙을 한 곳에서 관리합니다.",
    },
    ko: {
      eyebrow: "테넌트 관리",
      title: "테넌트 관리",
      description: "테넌트 프로필, 워크스페이스 정책, 메뉴 노출 규칙을 관리합니다.",
    },
  },
  "billing-ops": {
    de: {
      eyebrow: "billing ops",
      title: "Billing-Betrieb",
      description: "결제 게이트, 업그레이드, 무료 정책을 플랫폼 차원에서 운영합니다.",
    },
    ko: {
      eyebrow: "결제 운영",
      title: "결제 운영",
      description: "결제 게이트, 업그레이드, 무료 정책을 플랫폼 차원에서 운영합니다.",
    },
  },
};

function getFallbackMenuTitle(id: string, locale: LoginLocale): string {
  return (
    fallbackMenuTitleTranslations[id]?.[locale] ??
    fallbackMenuTitleTranslations[id]?.en ??
    id
  );
}

function buildFallbackMenuNode(seed: FallbackMenuSeed, locale: LoginLocale): MenuNode {
  const presentation = fallbackMenuPresentationMetadata[seed.id];
  return {
    id: seed.id,
    title: getFallbackMenuTitle(seed.id, locale),
    icon: presentation?.icon,
    closable: presentation?.closable,
    children: seed.children?.map((child) => buildFallbackMenuNode(child, locale)),
  };
}

function getBaseWorkspaceModeSeed(mode: WorkspaceMode): WorkspaceModeSeed {
  return fallbackWorkspaceModeSeeds.find((item) => item.mode === mode) ?? fallbackWorkspaceModeSeeds[0];
}

export const workspaceModes = fallbackWorkspaceModeSeeds.map((modeSeed) => ({
  ...modeSeed,
  label: modeTranslations[modeSeed.mode].en.label,
  shellTitle: modeTranslations[modeSeed.mode].en.shellTitle,
  shellSubtitle: modeTranslations[modeSeed.mode].en.shellSubtitle,
  menus: modeSeed.menus.map((menu) => buildFallbackMenuNode(menu, "en")),
}));
export function isWorkspaceRuntimePage(id: string): id is WorkspaceRuntimePageId {
  return (WORKSPACE_RUNTIME_PAGE_IDS as readonly string[]).includes(id);
}

export function getWorkspaceModeConfig(mode: WorkspaceMode, locale: LoginLocale): WorkspaceModeConfig {
  const base = getBaseWorkspaceModeSeed(mode);
  const translated = modeTranslations[mode][locale];
  return {
    ...base,
    label: translated.label,
    shellTitle: translated.shellTitle,
    shellSubtitle: translated.shellSubtitle,
    menus: base.menus.map((menu) => buildFallbackMenuNode(menu, locale)),
  };
}

export function getLocalizedWorkspaceFallbackViewModel(id: string, locale: LoginLocale): ViewModel {
  const base = fallbackWorkspaceViewModels[id] ?? fallbackWorkspaceViewModels["MNU_CUSTOMER_MY_TIME"];
  const translated = viewTranslations[id]?.[locale];
  if (!translated) {
    return base;
  }

  return {
    eyebrow: translated.eyebrow ?? base.eyebrow,
    title: translated.title ?? base.title,
    description: translated.description ?? base.description,
    gridTitle: translated.gridTitle ?? base.gridTitle,
    kpis: translated.kpis ?? base.kpis,
    highlights: translated.highlights ?? base.highlights,
    gridRows: translated.gridRows ?? base.gridRows,
  };
}

export const getLocalizedViewModel = getLocalizedWorkspaceFallbackViewModel;

export function findMenuTitle(mode: WorkspaceMode, id: string, locale: LoginLocale = "en"): string {
  const modeConfig = getWorkspaceModeConfig(mode, locale);
  const search = (menus: MenuNode[]): string | null => {
    for (const menu of menus) {
      if (menu.id === id) return menu.title;
      if (menu.children) {
        const childHit = search(menu.children);
        if (childHit) return childHit;
      }
    }
    return null;
  };
  return search(modeConfig.menus) ?? getFallbackMenuTitle(id, locale);
}

export function getFallbackMenuPresentation(id: string): MenuPresentationMetadata | undefined {
  return fallbackMenuPresentationMetadata[id];
}

export function localizeMenuNodesWithFallbackTitles(
  menus: MenuNode[],
  locale: LoginLocale
): MenuNode[] {
  return menus.map((menu) => ({
    ...menu,
    title: fallbackMenuTitleTranslations[menu.id]
      ? getFallbackMenuTitle(menu.id, locale)
      : menu.title,
    children: menu.children?.length
      ? localizeMenuNodesWithFallbackTitles(menu.children, locale)
      : undefined,
  }));
}

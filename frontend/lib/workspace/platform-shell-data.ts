import type { LoginLocale } from "@/lib/i18n/login-content";

export type WorkspaceMode = "client" | "admin";
export const WORKSPACE_RUNTIME_PAGE_IDS = [
  "MNU_CUSTOMER_MY_TIME",
  "MNU_CUSTOMER_TEAM_TIME",
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
    defaultTabId: "MNU_CUSTOMER_MY_TIME",
    menus: [
      {
        id: "MNU_CUSTOMER_ATTENDANCE",
        children: [{ id: "MNU_CUSTOMER_MY_TIME" }, { id: "MNU_CUSTOMER_TEAM_TIME" }],
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
  "client-dashboard": { en: "Dashboard", de: "Dashboard", ko: "ëŒ€ì‹œë³´ë“œ" },
  MNU_CUSTOMER_DASH: { en: "Dashboard", de: "Dashboard", ko: "ëŒ€ì‹œë³´ë“œ" },
  MNU_CUSTOMER_ATTENDANCE: { en: "Attendance", de: "Zeiterfassung", ko: "ê·¼íƒœ" },
  MNU_CUSTOMER_REPORTS: { en: "Reports", de: "Berichte", ko: "ë¦¬í¬íŠ¸" },
  MNU_CUSTOMER_ADMIN: { en: "Admin", de: "Admin", ko: "관리" },
  MNU_CUSTOMER_MY_TIME: { en: "My Work Time", de: "Meine Arbeitszeit", ko: "내 근무시간" },
  MNU_CUSTOMER_TEAM_TIME: { en: "Team Work Time", de: "Team Arbeitszeit", ko: "팀 근무시간" },
  MNU_CUSTOMER_WORK_TIME: { en: "Work Time", de: "Arbeitszeit", ko: "근무시간" },
  MNU_CUSTOMER_ADMIN_HOME: { en: "Admin Area", de: "Admin-Bereich", ko: "관리 영역" },
  workspace: { en: "Workspace", de: "Workspace", ko: "ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤" },
  documents: { en: "Documents", de: "Dokumente", ko: "ë¬¸ì„œ" },
  "ocr-inbox": { en: "OCR Inbox", de: "OCR Inbox", ko: "OCR ë°›ì€í•¨" },
  "expense-review": { en: "Expense Review", de: "KostenprÃ¼fung", ko: "ë¹„ìš© ê²€í† " },
  archive: { en: "Archive", de: "Archiv", ko: "ë³´ê´€í•¨" },
  attendance: { en: "Attendance", de: "Zeiterfassung", ko: "ê·¼íƒœ" },
  "my-time": { en: "My Time", de: "Meine Zeiten", ko: "ë‚´ ê·¼ë¬´ì‹œê°„" },
  "team-time": { en: "Team Time", de: "Team-Zeiten", ko: "íŒ€ ê·¼ë¬´í˜„í™©" },
  leave: { en: "Leave Requests", de: "UrlaubsantrÃ¤ge", ko: "íœ´ê°€ ìš”ì²­" },
  people: { en: "People", de: "Mitarbeitende", ko: "ì¸ì›" },
  employees: { en: "Employees", de: "Mitarbeiter", ko: "ì§ì›" },
  organization: { en: "Organization", de: "Organisation", ko: "ì¡°ì§" },
  departments: { en: "Departments", de: "Abteilungen", ko: "ë¶€ì„œ" },
  roles: { en: "Roles", de: "Rollen", ko: "ê¶Œí•œ" },
  "billing-center": { en: "Billing Center", de: "Billing Center", ko: "ê²°ì œ ì„¼í„°" },
  subscription: { en: "Subscription", de: "Abonnement", ko: "êµ¬ë…" },
  payments: { en: "Payments", de: "Zahlungen", ko: "ê²°ì œ" },
  MNU_CUSTOMER_WORK_TIME_HISTORY: { en: "History", de: "Verlauf", ko: "ì´ë ¥" },
  "admin-overview": { en: "Overview", de: "Ãœberblick", ko: "ê°œìš”" },
  "tenant-control": { en: "Tenant Control", de: "Mandantenverwaltung", ko: "í…Œë„ŒíŠ¸ ê´€ë¦¬" },
  "tenant-list": { en: "Tenant List", de: "Mandanten ëª©ë¡", ko: "í…Œë„ŒíŠ¸ ëª©ë¡" },
  "tenant-settings": { en: "Tenant Settings", de: "Mandanten ì„¤ì •", ko: "í…Œë„ŒíŠ¸ ì„¤ì •" },
  "company-profile": { en: "Company Profile", de: "Unternehmensprofil", ko: "íšŒì‚¬ í”„ë¡œí•„" },
  "workspace-policy": { en: "Workspace Policy", de: "Workspace ì •ì±…", ko: "ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤ ì •ì±…" },
  "menu-policy": { en: "Menu Policy", de: "MenÃ¼ ì •ì±…", ko: "ë©”ë‰´ ì •ì±…" },
  authorization: { en: "Authorization", de: "ê¶Œí•œ ê´€ë¦¬", ko: "ê¶Œí•œ ê´€ë¦¬" },
  "menu-management": { en: "Menu Management", de: "MenÃ¼ ê´€ë¦¬", ko: "ë©”ë‰´ ê´€ë¦¬" },
  "menu-tree": { en: "Menu Tree", de: "MenÃ¼ íŠ¸ë¦¬", ko: "ë©”ë‰´ íŠ¸ë¦¬" },
  "tab-policy": { en: "Tab Policy", de: "Tab ì •ì±…", ko: "íƒ­ ì •ì±…" },
  "depth-policy": { en: "Depth Policy", de: "Depth ì •ì±…", ko: "ëŽìŠ¤ ì •ì±…" },
  "depth-editor": { en: "Depth 1-4 Editor", de: "Depth 1-4 Editor", ko: "1-4ëŽìŠ¤ íŽ¸ì§‘ê¸°" },
  "role-policy": { en: "Role Policy", de: "Rollen ì •ì±…", ko: "ê¶Œí•œ ì •ì±…" },
  "billing-ops": { en: "Billing Ops", de: "ê²°ì œ ìš´ì˜", ko: "ê²°ì œ ìš´ì˜" },
  "plan-catalog": { en: "Plan Catalog", de: "Plan-Katalog", ko: "í”Œëžœ ì¹´íƒˆë¡œê·¸" },
  "payment-gates": { en: "Payment Gates", de: "Payment Gates", ko: "ê²°ì œ ê²Œì´íŠ¸" },
  "upgrade-queue": { en: "Upgrade Queue", de: "Upgrade Queue", ko: "ì—…ê·¸ë ˆì´ë“œ í" },
  "audit-center": { en: "Audit Center", de: "ê°ì‚¬ ì„¼í„°", ko: "ê°ì‚¬ ì„¼í„°" },
  "change-history": { en: "Change History", de: "Ã„nderungsverlauf", ko: "ë³€ê²½ ì´ë ¥" },
  "access-logs": { en: "Access Logs", de: "Zugriffsprotokolle", ko: "ì ‘ì† ë¡œê·¸" },
  compliance: { en: "Compliance Desk", de: "Compliance Desk", ko: "ì»´í”Œë¼ì´ì–¸ìŠ¤ ë°ìŠ¤í¬" },
  MNU_ADMIN_REPORTS: { en: "Reports", de: "Berichte", ko: "ë¦¬í¬íŠ¸" },
  MNU_ADMIN_WORK_TIME: { en: "Work Time", de: "Arbeitszeit", ko: "ê·¼íƒœ" },
  MNU_ADMIN_WORK_TIME_HISTORY: { en: "History", de: "Verlauf", ko: "ì´ë ¥" },
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
      { title: "4 invoices require approval", meta: "Finance admin Â· OCR review", emphasis: "warning" },
      { title: "2 leave requests need attention", meta: "HR admin Â· Team review" },
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
      { title: "4 invoices require approval", meta: "Finance admin Â· OCR review", emphasis: "warning" },
      { title: "2 leave requests need attention", meta: "HR admin Â· Team review" },
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
      { name: "Supplier invoice Â· DE-2031", owner: "Finance Admin", status: "Mismatch", updated: "2 min ago" },
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
      eyebrow: "ê·¼ë¬´ì§€",
      title: "íŒŒê²¬ì§€ ë° ì§€ì—­ ê´€ë¦¬",
      description: "ë³¸ì‚¬ ê¸°ì¤€ê°’, ê³ ê°ì‚¬ íŒŒê²¬ì§€, ì§ì› ê·¼ë¬´ì§€ ë°°ì •ì„ í•¨ê»˜ ê´€ë¦¬í•˜ì—¬ ì‹¤ì œ ê·¼ë¬´ ì§€ì—­ì˜ íœ´ì¼ ê·œì¹™ì´ ì ìš©ë˜ë„ë¡ í•©ë‹ˆë‹¤.",
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
      { name: "Growth â†’ Enterprise", owner: "Billing Ops", status: "Pending", updated: "Now" },
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
      label: "ê³ ê° ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤",
      shellTitle: "ê³ ê° ìš´ì˜ í¬í„¸",
      shellSubtitle: "ìŠ¹ì¸, ì¸ì‚¬, ë¬¸ì„œ, ê²°ì œë¥¼ í•˜ë‚˜ì˜ ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤ì—ì„œ ê´€ë¦¬í•©ë‹ˆë‹¤.",
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
      label: "ê´€ë¦¬ìž ì½˜ì†”",
      shellTitle: "í”Œëž«í¼ ê´€ì œ ì„¼í„°",
      shellSubtitle: "í…Œë„ŒíŠ¸, ìŠ¹ì¸, ê²°ì œ ê²Œì´íŠ¸, ê°ì‚¬ ê·¼ê±°ë¥¼ í”Œëž«í¼ ì „ë°˜ì—ì„œ í†µì œí•©ë‹ˆë‹¤.",
    },
  },
};

const viewTranslations: Partial<Record<string, Partial<Record<LoginLocale, Partial<ViewModel>>>>> = {
  "my-profile": {
    de: {
      eyebrow: "persÃ¶nliche einstellungen",
      title: "PersÃ¶nliche Daten",
      description: "PersÃ¶nliche Daten, Kontoeinstellungen, Sprache und Sicherheit in einem eigenen Tab verwalten.",
      kpis: [
        { label: "Kontostatus", value: "Aktiv", delta: "Verifiziert" },
        { label: "Sprache", value: "3", delta: "DE / EN / KO" },
        { label: "Sicherheit", value: "Gut", delta: "Passwort aktiv" },
        { label: "Letzte Ã„nderung", value: "Heute", delta: "Profil synchronisiert" },
      ],
      highlights: [
        { title: "Anzeigename, Kontakt und Profildaten bearbeiten", meta: "PersÃ¶nliche Informationen" },
        { title: "Passwort und Sicherheitsoptionen Ã¤ndern", meta: "Kontosicherheit", emphasis: "warning" },
        { title: "Benachrichtigungen und Spracheinstellungen verwalten", meta: "Workspace-Einstellungen", emphasis: "success" },
      ],
      gridTitle: "Profileinstellungen",
      gridRows: [
        { name: "Anzeigename", owner: "PersÃ¶nliche Infos", status: "Bearbeitbar", updated: "Jetzt" },
        { name: "Passwort", owner: "Sicherheit", status: "GeschÃ¼tzt", updated: "Heute" },
        { name: "Spracheinstellungen", owner: "Einstellungen", status: "VerfÃ¼gbar", updated: "Heute" },
      ],
    },
    ko: {
      eyebrow: "ê°œì¸ ì„¤ì •",
      title: "ê°œì¸ì •ë³´ë³€ê²½",
      description: "ê°œì¸ì •ë³´, ê³„ì • ê¸°ë³¸ì„¤ì •, ì–¸ì–´, ë³´ì•ˆ ì •ë³´ë¥¼ í•˜ë‚˜ì˜ íƒ­ì—ì„œ ê´€ë¦¬í•©ë‹ˆë‹¤.",
      kpis: [
        { label: "ê³„ì • ìƒíƒœ", value: "Active", delta: "Verified" },
        { label: "ì–¸ì–´", value: "3", delta: "DE / EN / KO" },
        { label: "ë³´ì•ˆ", value: "Good", delta: "Password active" },
        { label: "ìµœê·¼ ë³€ê²½", value: "Today", delta: "Profile synced" },
      ],
      highlights: [
        { title: "í‘œì‹œ ì´ë¦„, ì—°ë½ì²˜, í”„ë¡œí•„ ì •ë³´ë¥¼ ìˆ˜ì •í•©ë‹ˆë‹¤", meta: "ê°œì¸ì •ë³´" },
        { title: "ë¹„ë°€ë²ˆí˜¸ì™€ ë³´ì•ˆ ì˜µì…˜ì„ ë³€ê²½í•©ë‹ˆë‹¤", meta: "ë³´ì•ˆ", emphasis: "warning" },
        { title: "ì•Œë¦¼ê³¼ ì–¸ì–´ ì„¤ì •ì„ ê´€ë¦¬í•©ë‹ˆë‹¤", meta: "í™˜ê²½ì„¤ì •", emphasis: "success" },
      ],
      gridTitle: "í”„ë¡œí•„ ì„¤ì •",
      gridRows: [
        { name: "í‘œì‹œ ì´ë¦„", owner: "ê°œì¸ì •ë³´", status: "Editable", updated: "ì§€ê¸ˆ" },
        { name: "ë¹„ë°€ë²ˆí˜¸", owner: "ë³´ì•ˆ", status: "Protected", updated: "ì˜¤ëŠ˜" },
        { name: "ì–¸ì–´ ì„¤ì •", owner: "í™˜ê²½ì„¤ì •", status: "Available", updated: "ì˜¤ëŠ˜" },
      ],
    },
  },
  "client-dashboard": {
    de: {
      eyebrow: "kundenÃ¼bersicht",
      title: "Workspace-Status",
      description: "Startseite fÃ¼r Mitglieder und Kundenadministratoren mit klarer Informationsdichte und produktnaher Struktur.",
    },
    ko: {
      eyebrow: "ê³ ê° ê°œìš”",
      title: "ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤ í˜„í™©",
      description: "ê³ ê° ë©¤ë²„ì™€ ê³ ê° ê´€ë¦¬ìž ëª¨ë‘ë¥¼ ìœ„í•œ ì²« í™”ë©´ìœ¼ë¡œ, ì œí’ˆì„±ì€ ìœ ì§€í•˜ê³  ìž‘ì—… ë°€ë„ëŠ” ê³¼í•˜ì§€ ì•Šê²Œ ìž¡ì•˜ìŠµë‹ˆë‹¤.",
    },
  },
  MNU_CUSTOMER_DASH: {
    de: {
      eyebrow: "kundenÃ¼bersicht",
      title: "Workspace-Status",
      description: "Startseite fÃ¼r Mitglieder und Kundenadministratoren mit klarer Informationsdichte und produktnaher Struktur.",
    },
    ko: {
      eyebrow: "ê³ ê° ê°œìš”",
      title: "ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤ í˜„í™©",
      description: "ê³ ê° ë©¤ë²„ì™€ ê³ ê° ê´€ë¦¬ìž ëª¨ë‘ë¥¼ ìœ„í•œ ì²« í™”ë©´ìœ¼ë¡œ, ì œí’ˆì„±ì€ ìœ ì§€í•˜ê³  ìž‘ì—… ë°€ë„ëŠ” ê³¼í•˜ì§€ ì•Šê²Œ ìž¡ì•˜ìŠµë‹ˆë‹¤.",
    },
  },
  documents: {
    de: {
      eyebrow: "dokumente",
      title: "Dokumenten-Workspace",
      description: "OCR-Inbox, PrÃ¼fwarteschlange und Archiv in einer kompakten ArbeitsflÃ¤che.",
    },
    ko: {
      eyebrow: "ë¬¸ì„œ",
      title: "ë¬¸ì„œ ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤",
      description: "OCR ë°›ì€í•¨, ê²€í†  í, ë³´ê´€ ìž‘ì—…ì„ í•˜ë‚˜ì˜ í™”ë©´ì—ì„œ ë‹¤ë£¹ë‹ˆë‹¤.",
    },
  },
  attendance: {
    de: {
      eyebrow: "zeiterfassung",
      title: "Arbeitszeit-Board",
      description: "Ausnahmen, Freigaben und ëˆ„ë½ ì—†ì´ ë¹ ë¥´ê²Œ í™•ì¸í•  ìˆ˜ ìžˆëŠ” ê·¼íƒœ ì¤‘ì‹¬ í™”ë©´ìž…ë‹ˆë‹¤.",
    },
    ko: {
      eyebrow: "ê·¼íƒœ",
      title: "ê·¼íƒœ ê´€ì œíŒ",
      description: "ì˜ˆì™¸, ìŠ¹ì¸, ëˆ„ë½ì„ ë¹ ë¥´ê²Œ í™•ì¸í•  ìˆ˜ ìžˆëŠ” ê·¼íƒœ ì¤‘ì‹¬ í™”ë©´ìž…ë‹ˆë‹¤.",
    },
  },
  MNU_CUSTOMER_ATTENDANCE: {
    de: {
      eyebrow: "zeiterfassung",
      title: "Arbeitszeit-Board",
      description: "Ausnahmen, Freigaben und ëˆ„ë½ ì—†ì´ ë¹ ë¥´ê²Œ í™•ì¸í•  ìˆ˜ ìžˆëŠ” ê·¼íƒœ ì¤‘ì‹¬ í™”ë©´ìž…ë‹ˆë‹¤.",
    },
    ko: {
      eyebrow: "ê·¼íƒœ",
      title: "ê·¼íƒœ ê´€ì œíŒ",
      description: "ì˜ˆì™¸, ìŠ¹ì¸, ëˆ„ë½ì„ ë¹ ë¥´ê²Œ í™•ì¸í•  ìˆ˜ ìžˆëŠ” ê·¼íƒœ ì¤‘ì‹¬ í™”ë©´ìž…ë‹ˆë‹¤.",
    },
  },
  MNU_CUSTOMER_REPORTS: {
    de: {
      eyebrow: "berichte",
      title: "Report Center",
      description: "Monatliche Arbeitszeitdaten prÃ¼fen, nach Mitarbeiter oder Abteilung filtern und Ergebnisse exportieren.",
    },
    ko: {
      eyebrow: "ë¦¬í¬íŠ¸",
      title: "ë¦¬í¬íŠ¸ ì„¼í„°",
      description: "ì›”ë³„ ê·¼ë¬´ê¸°ë¡ì„ ê²€í† í•˜ê³  ì§ì› ë˜ëŠ” ë¶€ì„œ ê¸°ì¤€ìœ¼ë¡œ í•„í„°ë§í•œ ë’¤ ê²°ê³¼ë¥¼ ë‚´ë³´ëƒ…ë‹ˆë‹¤.",
    },
  },
  "billing-center": {
    de: {
      eyebrow: "abrechnung",
      title: "Abrechnung und Upgrade",
      description: "Der Bereich zwischen Kundenmitgliedern und zahlenden Kundenadministratoren wird hier gesteuert.",
    },
    ko: {
      eyebrow: "ê²°ì œ",
      title: "ê²°ì œ ë° ì—…ê·¸ë ˆì´ë“œ",
      description: "ê³ ê° ë©¤ë²„ ê¶Œí•œê³¼ ê³ ê° ê´€ë¦¬ìž ê¶Œí•œ ì‚¬ì´ì˜ ê²°ì œ ê²Œì´íŠ¸ë¥¼ ì´ ì˜ì—­ì—ì„œ ê´€ë¦¬í•©ë‹ˆë‹¤.",
    },
  },
  "admin-overview": {
    de: {
      eyebrow: "plattform admin",
      title: "Kontrollzentrum",
      description: "Zentrale Ãœbersicht fÃ¼r Super-Admins Ã¼ber Mandanten, ê¶Œí•œ, ê²°ì œ, ê°ì‚¬ ì§€í‘œë¥¼ í•œ ë²ˆì— ë³´ì—¬ì¤ë‹ˆë‹¤.",
    },
    ko: {
      eyebrow: "í”Œëž«í¼ ê´€ë¦¬ìž",
      title: "í†µí•© ê´€ì œ",
      description: "ìŠˆí¼ê´€ë¦¬ìžê°€ í…Œë„ŒíŠ¸, ê¶Œí•œ, ê²°ì œ, ê°ì‚¬ ì§€í‘œë¥¼ í•œ ë²ˆì— ë³´ëŠ” ë©”ì¸ í™”ë©´ìž…ë‹ˆë‹¤.",
    },
  },
  "tenant-control": {
    de: {
      eyebrow: "mandantensteuerung",
      title: "Mandantenverwaltung",
      description: "Mandantenprofil, Workspace-ì •ì±…ê³¼ ë©”ë‰´ ë…¸ì¶œ ê·œì¹™ì„ í•œ ê³³ì—ì„œ ê´€ë¦¬í•©ë‹ˆë‹¤.",
    },
    ko: {
      eyebrow: "í…Œë„ŒíŠ¸ ê´€ë¦¬",
      title: "í…Œë„ŒíŠ¸ ê´€ë¦¬",
      description: "í…Œë„ŒíŠ¸ í”„ë¡œí•„, ì›Œí¬ìŠ¤íŽ˜ì´ìŠ¤ ì •ì±…, ë©”ë‰´ ë…¸ì¶œ ê·œì¹™ì„ ê´€ë¦¬í•©ë‹ˆë‹¤.",
    },
  },
  "billing-ops": {
    de: {
      eyebrow: "billing ops",
      title: "Billing-Betrieb",
      description: "ê²°ì œ ê²Œì´íŠ¸, ì—…ê·¸ë ˆì´ë“œ, ë¬´ë£Œ ì •ì±…ì„ í”Œëž«í¼ ì°¨ì›ì—ì„œ ìš´ì˜í•©ë‹ˆë‹¤.",
    },
    ko: {
      eyebrow: "ê²°ì œ ìš´ì˜",
      title: "ê²°ì œ ìš´ì˜",
      description: "ê²°ì œ ê²Œì´íŠ¸, ì—…ê·¸ë ˆì´ë“œ, ë¬´ë£Œ ì •ì±…ì„ í”Œëž«í¼ ì°¨ì›ì—ì„œ ìš´ì˜í•©ë‹ˆë‹¤.",
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

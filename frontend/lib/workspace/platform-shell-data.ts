import type { LoginLocale } from "@/lib/i18n/login-content";

export type WorkspaceMode = "client" | "admin";

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
  mobileQuickMenus: string[];
};

const baseWorkspaceModes: WorkspaceModeConfig[] = [
  {
    mode: "client",
    label: "Client workspace",
    shellTitle: "Customer operations portal",
    shellSubtitle: "Work on approvals, people, documents, and billing from one product shell.",
    defaultTabId: "client-dashboard",
    mobileQuickMenus: ["client-dashboard", "documents", "attendance", "billing-center"],
    menus: [
      { id: "client-dashboard", title: "Dashboard", icon: "grid" },
      {
        id: "workspace",
        title: "Workspace",
        icon: "briefcase",
        children: [
          {
            id: "documents",
            title: "Documents",
            children: [
              { id: "ocr-inbox", title: "OCR Inbox" },
              { id: "expense-review", title: "Expense Review" },
              { id: "archive", title: "Archive" },
            ],
          },
          {
            id: "attendance",
            title: "Attendance",
            children: [
              { id: "my-time", title: "My Time" },
              { id: "team-time", title: "Team Time" },
              { id: "leave", title: "Leave Requests" },
            ],
          },
        ],
      },
      {
        id: "people",
        title: "People",
        icon: "users",
        children: [
          { id: "employees", title: "Employees" },
          {
            id: "organization",
            title: "Organization",
            children: [
              { id: "departments", title: "Departments" },
              { id: "roles", title: "Roles" },
            ],
          },
        ],
      },
      {
        id: "billing-center",
        title: "Billing Center",
        icon: "credit-card",
        children: [
          { id: "subscription", title: "Subscription" },
          { id: "payments", title: "Payments" },
        ],
      },
    ],
  },
  {
    mode: "admin",
    label: "Admin console",
    shellTitle: "Platform control tower",
    shellSubtitle: "Supervise tenants, approvals, billing gates, and audit evidence across the platform.",
    defaultTabId: "admin-overview",
    mobileQuickMenus: ["admin-overview", "tenant-control", "billing-ops", "audit-center"],
    menus: [
      { id: "admin-overview", title: "Overview", icon: "shield" },
      {
        id: "tenant-control",
        title: "Tenant Control",
        icon: "building",
        children: [
          { id: "tenant-list", title: "Tenant List" },
          {
            id: "tenant-settings",
            title: "Tenant Settings",
            children: [
              { id: "company-profile", title: "Company Profile" },
              { id: "workspace-policy", title: "Workspace Policy" },
              { id: "menu-policy", title: "Menu Policy" },
            ],
          },
        ],
      },
      {
        id: "authorization",
        title: "Authorization",
        icon: "key",
        children: [
          {
            id: "menu-management",
            title: "Menu Management",
            children: [
              { id: "menu-tree", title: "Menu Tree" },
              { id: "tab-policy", title: "Tab Policy" },
              {
                id: "depth-policy",
                title: "Depth Policy",
                children: [{ id: "depth-editor", title: "Depth 1-4 Editor" }],
              },
            ],
          },
          { id: "role-policy", title: "Role Policy" },
        ],
      },
      {
        id: "billing-ops",
        title: "Billing Ops",
        icon: "wallet",
        children: [
          { id: "plan-catalog", title: "Plan Catalog" },
          { id: "payment-gates", title: "Payment Gates" },
          { id: "upgrade-queue", title: "Upgrade Queue" },
        ],
      },
      {
        id: "audit-center",
        title: "Audit Center",
        icon: "activity",
        children: [
          { id: "change-history", title: "Change History" },
          { id: "access-logs", title: "Access Logs" },
          { id: "compliance", title: "Compliance Desk" },
        ],
      },
    ],
  },
];

const baseViewModels: Record<string, ViewModel> = {
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

function cloneMenuNode(menu: MenuNode): MenuNode {
  return {
    ...menu,
    children: menu.children?.map((child) => cloneMenuNode(child)),
  };
}

function getBaseWorkspaceMode(mode: WorkspaceMode): WorkspaceModeConfig {
  return baseWorkspaceModes.find((item) => item.mode === mode) ?? baseWorkspaceModes[0];
}

export const workspaceModes = baseWorkspaceModes;
export const viewModels = baseViewModels;

export function getWorkspaceModeConfig(mode: WorkspaceMode, locale: LoginLocale): WorkspaceModeConfig {
  const base = getBaseWorkspaceMode(mode);
  const translated = modeTranslations[mode][locale];
  return {
    ...base,
    label: translated.label,
    shellTitle: translated.shellTitle,
    shellSubtitle: translated.shellSubtitle,
    menus: base.menus.map((menu) => cloneMenuNode(menu)),
  };
}

export function getLocalizedViewModel(id: string, locale: LoginLocale): ViewModel {
  const base = baseViewModels[id] ?? baseViewModels["client-dashboard"];
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

export function findMenuTitle(mode: WorkspaceMode, id: string, locale: LoginLocale = "en"): string {
  const config = getWorkspaceModeConfig(mode, locale);
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
  return search(config.menus) ?? id;
}

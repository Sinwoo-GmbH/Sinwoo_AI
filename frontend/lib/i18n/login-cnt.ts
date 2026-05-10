export const LOGIN_LOCALES = ["de", "en", "ko"] as const;
export const LOGIN_LOCALE_STORAGE_KEY = "sinwoo.wsp.locale.v1";

export const LOGIN_LOCALE_FLAGS: Record<LoginLocale, string> = {
  de: "🇩🇪",
  en: "🇬🇧",
  ko: "🇰🇷",
};

export type LoginLocale = (typeof LOGIN_LOCALES)[number];

export type LoginMsgs = {
  localeLabel: string;
  localeNames: Record<LoginLocale, string>;
  platformEyebrow: string;
  platformTitle: string;
  platformDesc: string;
  tagline: string;
  desktopLabel: string;
  desktopHighlights: string[];
  cardEyebrow: string;
  signInTitle: string;
  formDesc: string;
  emailLabel: string;
  emailPh: string;
  passwordLabel: string;
  passwordPh: string;
  rememberEmail: string;
  signInButton: string;
  ssoLabel: string;
  continueWith: string;
  footerCo: string;
  footerDesc: string;
  footerCopyright: string;
  errorMsgs: Record<string, string>;
};

const loginMsgs: Record<LoginLocale, LoginMsgs> = {
  ko: {
    localeLabel: "언어",
    localeNames: {
      ko: "한국어",
      en: "English",
      de: "Deutsch",
    },
    platformEyebrow: "Enterprise B2B Workspace",
    platformTitle: "SINWOO Platform",
    platformDesc: "독일 법인 운영, 고객사 관리, 감사 추적을 하나의 입구에서 처리하는 기업형 워크스페이스입니다.",
    tagline: "Access Sinwoo internal administration and customer workspace operations.",
    desktopLabel: "Enterprise access",
    desktopHighlights: [
      "내부 운영과 고객사 계정을 하나의 진입점에서 관리",
      "권한별 메뉴와 감사 추적을 동일한 기준으로 통제",
      "독일 법인 운영 기준에 맞춘 보안형 워크스페이스 로그인",
    ],
    cardEyebrow: "Workspace Access",
    signInTitle: "로그인",
    formDesc: "회사 이메일과 비밀번호로 안전하게 접속하세요.",
    emailLabel: "이메일 주소",
    emailPh: "name@company.com",
    passwordLabel: "비밀번호",
    passwordPh: "비밀번호를 입력하세요",
    rememberEmail: "로그인 이메일 저장",
    signInButton: "로그인",
    ssoLabel: "SSO",
    continueWith: "다음 계정으로 계속",
    footerCo: "SINWOO INTERNATIONAL",
    footerDesc: "독일 및 글로벌 팀을 위한 엔터프라이즈 B2B 운영 플랫폼",
    footerCopyright: "Copyright 2026 SINWOO International. All rights reserved.",
    errorMsgs: {
      AUTH_INVALID_CREDENTIALS: "이메일 주소 또는 비밀번호가 올바르지 않습니다.",
      AUTH_USER_INACTIVE: "현재 이 계정은 로그인할 수 없습니다.",
      AUTH_EMAIL_REQUIRED: "이메일 주소를 입력해 주세요.",
      AUTH_TENANT_UNRESOLVED: "이메일 주소로 워크스페이스를 확인할 수 없습니다.",
      AUTH_TENANT_AMBIGUOUS: "이 이메일 주소에 여러 워크스페이스가 연결되어 있습니다. 관리자에게 문의해 주세요.",
      AUTH_AUTHENTICATION_REQUIRED: "로그인이 필요합니다.",
      AUTH_OAUTH_SUBJECT_MISSING: "외부 로그인 응답이 완전하지 않습니다. 다시 시도해 주세요.",
      AUTH_OAUTH_EMAIL_REQUIRED: "외부 계정에서 이메일 주소를 제공하지 않았습니다.",
      REQUEST_VALIDATION_ERROR: "입력값을 다시 확인해 주세요.",
      REQUEST_BODY_INVALID: "요청 데이터를 다시 확인해 주세요.",
      INTERNAL_SERVER_ERROR: "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
      DEFAULT_400: "이메일 주소와 비밀번호를 확인해 주세요.",
      DEFAULT_401: "이메일 주소 또는 비밀번호가 올바르지 않습니다.",
      DEFAULT_403: "현재 이 계정은 로그인할 수 없습니다.",
      DEFAULT_FALLBACK: "로그인할 수 없습니다. 잠시 후 다시 시도해 주세요.",
    },
  },
  en: {
    localeLabel: "Language",
    localeNames: {
      ko: "한국어",
      en: "English",
      de: "Deutsch",
    },
    platformEyebrow: "Enterprise B2B Workspace",
    platformTitle: "SINWOO Platform",
    platformDesc: "A structured enterprise workspace for internal operations, customer administration, and audit-ready workflows.",
    tagline: "Access Sinwoo internal administration and customer workspace operations.",
    desktopLabel: "Enterprise access",
    desktopHighlights: [
      "Manage internal operations and customer accounts from a single entry point.",
      "Control role-based mnus and audit visibility with one shared policy.",
      "Use a secure workspace sign-in aligned to Germany-facing operations.",
    ],
    cardEyebrow: "Workspace Access",
    signInTitle: "Sign in",
    formDesc: "Use your company email and password to access the platform securely.",
    emailLabel: "Email address",
    emailPh: "name@company.com",
    passwordLabel: "Password",
    passwordPh: "Enter your password",
    rememberEmail: "Remember email",
    signInButton: "Sign in",
    ssoLabel: "SSO",
    continueWith: "Continue with",
    footerCo: "SINWOO INTERNATIONAL",
    footerDesc: "Enterprise B2B Operations Platform for Germany and Global Teams",
    footerCopyright: "Copyright 2026 SINWOO International. All rights reserved.",
    errorMsgs: {
      AUTH_INVALID_CREDENTIALS: "Email address or password is incorrect.",
      AUTH_USER_INACTIVE: "Your account is not allowed to sign in right now.",
      AUTH_EMAIL_REQUIRED: "Email address is required.",
      AUTH_TENANT_UNRESOLVED: "Unable to identify your workspace from the email address.",
      AUTH_TENANT_AMBIGUOUS: "This email address matches more than one workspace. Please contact an administrator.",
      AUTH_AUTHENTICATION_REQUIRED: "Authentication is required.",
      AUTH_OAUTH_SUBJECT_MISSING: "The external sign-in response is incomplete. Please try again.",
      AUTH_OAUTH_EMAIL_REQUIRED: "The external account does not provide an email address.",
      REQUEST_VALIDATION_ERROR: "Please check the required fields and try again.",
      REQUEST_BODY_INVALID: "Please check the request data and try again.",
      INTERNAL_SERVER_ERROR: "An unexpected error occurred. Please try again later.",
      DEFAULT_400: "Please check your email address and password.",
      DEFAULT_401: "Email address or password is incorrect.",
      DEFAULT_403: "Your account is not allowed to sign in right now.",
      DEFAULT_FALLBACK: "Unable to sign in. Please try again.",
    },
  },
  de: {
    localeLabel: "Sprache",
    localeNames: {
      ko: "한국어",
      en: "English",
      de: "Deutsch",
    },
    platformEyebrow: "Enterprise B2B Workspace",
    platformTitle: "SINWOO Platform",
    platformDesc: "Ein strukturierter Enterprise-Workspace für interne Abläufe, Kundenverwaltung und auditfeste Betriebsprozesse.",
    tagline: "Access Sinwoo internal administration and customer workspace operations.",
    desktopLabel: "Enterprise access",
    desktopHighlights: [
      "Interne Abläufe und Kundenkonten über einen einzigen Zugang verwalten.",
      "Rollenbasierte Menüs und Audit-Sichtbarkeit mit einer Richtlinie steuern.",
      "Sicherer Workspace-Login für Deutschland-orientierte Betriebsprozesse.",
    ],
    cardEyebrow: "Workspace Access",
    signInTitle: "Anmelden",
    formDesc: "Melden Sie sich mit Ihrer Firmen-E-Mail und Ihrem Passwort sicher an.",
    emailLabel: "E-Mail-Adresse",
    emailPh: "name@company.com",
    passwordLabel: "Passwort",
    passwordPh: "Passwort eingeben",
    rememberEmail: "E-Mail speichern",
    signInButton: "Anmelden",
    ssoLabel: "SSO",
    continueWith: "Weiter mit",
    footerCo: "SINWOO INTERNATIONAL",
    footerDesc: "Enterprise-B2B-Betriebsplattform für Deutschland und globale Teams",
    footerCopyright: "Copyright 2026 SINWOO International. All rights reserved.",
    errorMsgs: {
      AUTH_INVALID_CREDENTIALS: "E-Mail-Adresse oder Passwort ist nicht korrekt.",
      AUTH_USER_INACTIVE: "Dieses Konto darf sich derzeit nicht anmelden.",
      AUTH_EMAIL_REQUIRED: "Eine E-Mail-Adresse ist erforderlich.",
      AUTH_TENANT_UNRESOLVED: "Der Workspace konnte anhand der E-Mail-Adresse nicht ermittelt werden.",
      AUTH_TENANT_AMBIGUOUS: "Diese E-Mail-Adresse ist mehreren Workspaces zugeordnet. Bitte wenden Sie sich an den Administrator.",
      AUTH_AUTHENTICATION_REQUIRED: "Eine Anmeldung ist erforderlich.",
      AUTH_OAUTH_SUBJECT_MISSING: "Die Antwort des externen Anbieters ist unvollständig. Bitte versuchen Sie es erneut.",
      AUTH_OAUTH_EMAIL_REQUIRED: "Das externe Konto liefert keine E-Mail-Adresse.",
      REQUEST_VALIDATION_ERROR: "Bitte prüfen Sie die Eingaben und versuchen Sie es erneut.",
      REQUEST_BODY_INVALID: "Bitte prüfen Sie die Anfragedaten und versuchen Sie es erneut.",
      INTERNAL_SERVER_ERROR: "Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.",
      DEFAULT_400: "Bitte prüfen Sie E-Mail-Adresse und Passwort.",
      DEFAULT_401: "E-Mail-Adresse oder Passwort ist nicht korrekt.",
      DEFAULT_403: "Dieses Konto darf sich derzeit nicht anmelden.",
      DEFAULT_FALLBACK: "Anmeldung nicht möglich. Bitte versuchen Sie es erneut.",
    },
  },
};

export function isSupportedLoginLocale(value: string | null | undefined): value is LoginLocale {
  return !!value && LOGIN_LOCALES.includes(value as LoginLocale);
}

export function getLoginMsgs(locale: LoginLocale): LoginMsgs {
  return loginMsgs[locale];
}

export function detectBrowserLoginLocale(): LoginLocale {
  if (typeof window === "undefined") {
    return "en";
  }

  const candidates = [
    window.navigator.language,
    ...(window.navigator.languages ?? []),
  ].filter(Boolean) as string[];

  for (const candidate of candidates) {
    const normalized = candidate.toLowerCase();
    if (normalized.startsWith("de")) {
      return "de";
    }
    if (normalized.startsWith("en")) {
      return "en";
    }
    if (normalized.startsWith("ko")) {
      return "ko";
    }
  }

  return "en";
}

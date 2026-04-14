import type { LoginLocale } from "@/lib/i18n/login-content";

export type AuthCallbackMessages = {
  successTitle: string;
  failureTitle: string;
  successDescription: string;
  failureDescription: string;
  providerLabel: string;
  userIdLabel: string;
  tenantIdLabel: string;
  unknownError: string;
  goToDashboard: string;
  backToLogin: string;
};

const authCallbackMessages: Record<LoginLocale, AuthCallbackMessages> = {
  en: {
    successTitle: "OAuth login completed",
    failureTitle: "OAuth login failed",
    successDescription:
      "SINWOO application tokens have been issued and stored in the browser.",
    failureDescription: "The provider callback did not complete successfully.",
    providerLabel: "Provider",
    userIdLabel: "User ID",
    tenantIdLabel: "Tenant ID",
    unknownError: "Unknown OAuth error",
    goToDashboard: "Go to dashboard",
    backToLogin: "Back to login",
  },
  de: {
    successTitle: "OAuth-Anmeldung abgeschlossen",
    failureTitle: "OAuth-Anmeldung fehlgeschlagen",
    successDescription:
      "SINWOO-Anwendungstoken wurden ausgestellt und im Browser gespeichert.",
    failureDescription: "Der Callback des Providers wurde nicht erfolgreich abgeschlossen.",
    providerLabel: "Provider",
    userIdLabel: "Benutzer-ID",
    tenantIdLabel: "Tenant-ID",
    unknownError: "Unbekannter OAuth-Fehler",
    goToDashboard: "Zum Dashboard",
    backToLogin: "Zurück zur Anmeldung",
  },
  ko: {
    successTitle: "OAuth 로그인 완료",
    failureTitle: "OAuth 로그인 실패",
    successDescription: "SINWOO 애플리케이션 토큰이 발급되어 브라우저에 저장되었습니다.",
    failureDescription: "외부 제공자 콜백이 정상적으로 완료되지 않았습니다.",
    providerLabel: "제공자",
    userIdLabel: "사용자 ID",
    tenantIdLabel: "테넌트 ID",
    unknownError: "알 수 없는 OAuth 오류",
    goToDashboard: "대시보드로 이동",
    backToLogin: "로그인으로 돌아가기",
  },
};

export function getAuthCallbackMessages(locale: LoginLocale): AuthCallbackMessages {
  return authCallbackMessages[locale];
}

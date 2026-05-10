import type { LoginLocale } from "@/lib/i18n/login-cnt";

export type AuthCallbackMsgs = {
  successTitle: string;
  failureTitle: string;
  successDesc: string;
  failureDesc: string;
  providerLabel: string;
  usrCdLabel: string;
  tenantCdLabel: string;
  unknownError: string;
  goToDashboard: string;
  backToLogin: string;
};

const authCallbackMsgs: Record<LoginLocale, AuthCallbackMsgs> = {
  en: {
    successTitle: "OAuth login completed",
    failureTitle: "OAuth login failed",
    successDesc:
      "SINWOO application tokens have been issued and stored in the browser.",
    failureDesc: "The provider callback did not complete successfully.",
    providerLabel: "Provider",
    usrCdLabel: "Login ID",
    tenantCdLabel: "Tenant Code",
    unknownError: "Unknown OAuth error",
    goToDashboard: "Go to dashboard",
    backToLogin: "Back to login",
  },
  de: {
    successTitle: "OAuth-Anmeldung abgeschlossen",
    failureTitle: "OAuth-Anmeldung fehlgeschlagen",
    successDesc:
      "SINWOO-Anwendungstoken wurden ausgestellt und im Browser gespeichert.",
    failureDesc: "Der Callback des Providers wurde nicht erfolgreich abgeschlossen.",
    providerLabel: "Provider",
    usrCdLabel: "Login-ID",
    tenantCdLabel: "Tenant-Code",
    unknownError: "Unbekannter OAuth-Fehler",
    goToDashboard: "Zum Dashboard",
    backToLogin: "Zurück zur Anmeldung",
  },
  ko: {
    successTitle: "OAuth 로그인 완료",
    failureTitle: "OAuth 로그인 실패",
    successDesc: "SINWOO 애플리케이션 토큰이 발급되어 브라우저에 저장되었습니다.",
    failureDesc: "외부 제공자 콜백이 정상적으로 완료되지 않았습니다.",
    providerLabel: "제공자",
    usrCdLabel: "Login ID",
    tenantCdLabel: "테넌트 코드",
    unknownError: "알 수 없는 OAuth 오류",
    goToDashboard: "대시보드로 이동",
    backToLogin: "로그인으로 돌아가기",
  },
};

export function getAuthCallbackMsgs(locale: LoginLocale): AuthCallbackMsgs {
  return authCallbackMsgs[locale];
}

export type AuthProviderItem = {
  registrationId: string;
  providerNm: string;
  authorizeUri: string;
};

export type AuthProviderListResponse = {
  totCnt: number;
  itemList: AuthProviderItem[];
};

export type CurrentUsr = {
  usrId: number;
  tenantId: number;
  tenantCd: string;
  coId: number | null;
  tenantTpCd: string;
  lgnId: string;
  eml: string;
  dspNm: string;
  authGrpCd: string | null;
  authLvlCd: string | null;
  roleCds: string[];
  billEntitledYn: "Y" | "N";
};

export type AuthTokenResponse = {
  accessToken: string;
  accessTokenExpiresIn: number;
  refreshToken: string;
  refreshTokenExpiresIn: number;
  tokenType: string;
  providerCd: string;
  user: CurrentUsr;
};

export type CredLoginRequest = {
  eml: string;
  pwdEnc: string;
};

export type CredKeyResponse = {
  alg: string;
  keyFormat: string;
  publicKey: string;
};

export type ApiErrorResponse = {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
};

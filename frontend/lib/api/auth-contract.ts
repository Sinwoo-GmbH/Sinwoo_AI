export type AuthProviderItem = {
  registrationId: string;
  providerNm: string;
  authorizeUri: string;
};

export type AuthProviderListResponse = {
  totCnt: number;
  itemList: AuthProviderItem[];
};

export type CurrentUser = {
  usrId: number;
  tenantId: number;
  coId: number | null;
  lgnId: string;
  eml: string;
  dspNm: string;
  authGrpCd: string | null;
  authLvlCd: string | null;
  roleCds: string[];
};

export type AuthTokenResponse = {
  accessToken: string;
  accessTokenExpiresIn: number;
  refreshToken: string;
  refreshTokenExpiresIn: number;
  tokenType: string;
  providerCd: string;
  user: CurrentUser;
};

export type CredentialLoginRequest = {
  tenantCd: string;
  lgnId: string;
  pwd: string;
};

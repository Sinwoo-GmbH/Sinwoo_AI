export type AuthProviderItem = {
  registrationId: string;
  providerNm: string;
  authorizeUri: string;
};

export type AuthProviderListResponse = {
  totCnt: number;
  itemList: AuthProviderItem[];
};

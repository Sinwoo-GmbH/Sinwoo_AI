export interface CommonCodeRes {
  cdId: number;
  grpId: number;
  grpCd: string;
  cd: string;
  cdNmKo?: string | null;
  cdNmEn?: string | null;
  cdNmDe?: string | null;
  dspCdNm?: string | null;
  cdDescKo?: string | null;
  cdDescEn?: string | null;
  cdDescDe?: string | null;
  useYn: string;
  dspOrd?: number | null;
  crtDtm?: string | null;
  updDtm?: string | null;
}

export interface CommonCodeListRes {
  totCnt: number;
  itemList: CommonCodeRes[];
}

export const COMMON_CODE_API_PATH = "/api/v1/codes";

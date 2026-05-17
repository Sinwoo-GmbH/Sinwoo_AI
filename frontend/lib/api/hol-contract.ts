/* ── Holiday API contracts ──────────────────────────────── */

export const RGN_HOL_API = "/api/v1/region-holidays";
export const CO_HOL_API = "/api/v1/company-holidays";

/* ── Region Holiday ── */

export interface RgnHolResponse {
  id: number;
  yr: number;
  regionCd: string;
  regionNm: string;
  holidayDt: string;
  holidayNm: string;
  wkndYn: string;
}

export interface RgnHolListResponse {
  totCnt: number;
  itemList: RgnHolResponse[];
}

/* ── Company Holiday ── */

export interface CoHolResponse {
  id: number;
  holidayNm: string;
  strDt: string;
  endDt: string;
  annualYn: string;
  applyYr: number;
  crtDtm: string;
  updDtm: string;
}

export interface CoHolListResponse {
  totCnt: number;
  itemList: CoHolResponse[];
}

export interface CreateCoHolRequest {
  holidayNm: string;
  strDt: string;
  endDt: string;
  annualYn: string;
  applyYr: number;
}

export interface UpdateCoHolRequest {
  holidayNm: string;
  strDt: string;
  endDt: string;
  annualYn: string;
  applyYr: number;
}

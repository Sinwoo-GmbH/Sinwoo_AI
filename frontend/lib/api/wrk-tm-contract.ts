/* ── Work Time API contract ─────────────────────────────── */

export const WRK_TM_API = "/api/v1/work-times";

/* ── Request ── */

export interface ClockInRequest {
  workDt: string;   // "2026-05-16"
  strTm: string;    // "09:00:00"
}

export interface ClockOutRequest {
  workDt: string;
  endTm: string;
}

export interface SaveWrkTmRequest {
  workDt: string;
  strTm?: string | null;
  endTm?: string | null;
  rmk?: string | null;
}

/* ── Response ── */

export interface WrkTmResponse {
  id: number;
  empId: number;
  workDt: string;
  strTm: string | null;
  endTm: string | null;
  workMin: number | null;
  rmk: string | null;
  crtDtm: string;
  updDtm: string;
}

export interface WrkTmListResponse {
  totCnt: number;
  itemList: WrkTmResponse[];
}

export interface CreateSubscrPlanRequest {
  planCd: string;
  planNm: string;
  tenantTpCd: string;
  billCyclCd: string;
  currCd: string;
  baseAmt: number;
  usrLmtCnt?: number | null;
  useYn?: "Y" | "N";
}

export interface SubscrPlanResponse {
  planId: number;
  planCd: string;
  planNm: string;
  tenantTpCd: string;
  billCyclCd: string;
  currCd: string;
  baseAmt: number;
  usrLmtCnt?: number | null;
  useYn: "Y" | "N";
  crtDtm: string;
  updDtm: string;
}

export interface SubscrPlanListResponse {
  totCnt: number;
  itemList: SubscrPlanResponse[];
}

export interface CreateSubscrRequest {
  tenantId: number;
  planCd: string;
  subsStsCd: string;
  billFreeYn?: "Y" | "N";
  autoPayYn?: "Y" | "N";
  strDt: string;
  endDt?: string | null;
  nextBillDt?: string | null;
}

export interface SubscrResponse {
  subsId: number;
  tenantId: number;
  planId: number;
  planCd: string;
  planNm: string;
  subsStsCd: string;
  billFreeYn: "Y" | "N";
  autoPayYn: "Y" | "N";
  strDt: string;
  endDt?: string | null;
  nextBillDt?: string | null;
  crtDtm: string;
  updDtm: string;
}

export interface SubscrListResponse {
  totCnt: number;
  itemList: SubscrResponse[];
}

export interface CreatePayTxnRequest {
  tenantId: number;
  subsId: number;
  payTpCd: string;
  payStsCd: string;
  payAmt: number;
  currCd: string;
  pgCd?: string | null;
  pgTxnNo?: string | null;
  aprvDtm?: string | null;
  failMsg?: string | null;
}

export interface PayTxnResponse {
  payTxnId: number;
  tenantId: number;
  subsId: number;
  payTpCd: string;
  payStsCd: string;
  payAmt: number;
  currCd: string;
  pgCd?: string | null;
  pgTxnNo?: string | null;
  aprvDtm?: string | null;
  failMsg?: string | null;
  crtDtm: string;
  updDtm: string;
}

export interface PayTxnListResponse {
  totCnt: number;
  itemList: PayTxnResponse[];
}

export const SUBSCR_PLAN_API_PATH = "/api/v1/subscription-plans";
export const SUBSCR_API_PATH = "/api/v1/subscriptions";
export const PAY_TXN_API_PATH = "/api/v1/payment-transactions";

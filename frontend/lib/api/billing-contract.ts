export interface CreateSubscriptionPlanReq {
  planCd: string;
  planNm: string;
  tenantTpCd: string;
  billCyclCd: string;
  currCd: string;
  baseAmt: number;
  usrLmtCnt?: number | null;
  useYn?: "Y" | "N";
}

export interface SubscriptionPlanRes {
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

export interface SubscriptionPlanListRes {
  totCnt: number;
  itemList: SubscriptionPlanRes[];
}

export interface CreateSubscriptionReq {
  tenantId: number;
  planCd: string;
  subsStsCd: string;
  billFreeYn?: "Y" | "N";
  autoPayYn?: "Y" | "N";
  strDt: string;
  endDt?: string | null;
  nextBillDt?: string | null;
}

export interface SubscriptionRes {
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

export interface SubscriptionListRes {
  totCnt: number;
  itemList: SubscriptionRes[];
}

export interface CreatePaymentTransactionReq {
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

export interface PaymentTransactionRes {
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

export interface PaymentTransactionListRes {
  totCnt: number;
  itemList: PaymentTransactionRes[];
}

export const SUBSCRIPTION_PLAN_API_PATH = "/api/v1/subscription-plans";
export const SUBSCRIPTION_API_PATH = "/api/v1/subscriptions";
export const PAYMENT_TRANSACTION_API_PATH = "/api/v1/payment-transactions";

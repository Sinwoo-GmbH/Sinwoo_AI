export type BizModMetricResponse = {
  metricCd: string;
  metricNm: string;
  metricVal: string;
};

export type BizModResponse = {
  modCd: string;
  mnuCd: string;
  groupCd: string;
  modNm: string;
  desc: string;
  legacyMnuCd: string;
  legacyUri: string;
  primaryTblNm: string;
  tblNms: string[];
  itemCnt: number;
  metricList: BizModMetricResponse[];
};

export type BizModListResponse = {
  itemList: BizModResponse[];
};

export type BizRecColResponse = {
  colNm: string;
  label: string;
  dataTpCd: "code" | "date" | "datetime" | "decimal" | "number" | "text" | "time" | "yn" | string;
  keyYn: boolean;
  visibleYn: boolean;
  writableYn: boolean;
  requiredYn: boolean;
  dspOrd: number;
};

export type BizRecRow = Record<string, string | number | boolean | null>;

export type BizRecListResponse = {
  modCd: string;
  tblNm: string;
  creatableYn: boolean;
  editableYn: boolean;
  deletableYn: boolean;
  colList: BizRecColResponse[];
  itemList: BizRecRow[];
  totCnt: number;
  page: number;
  size: number;
};

export type BizRecResponse = {
  modCd: string;
  tblNm: string;
  values: BizRecRow;
};

export type BizRecSaveRequest = {
  values: BizRecRow;
};

export type BizRelTableResponse = {
  tblNm: string;
  label: string;
  colList: string[];
  itemList: BizRecRow[];
  totCnt: number;
};

export type BizRelListResponse = {
  modCd: string;
  recId: number;
  tableList: BizRelTableResponse[];
};

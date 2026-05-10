export type BusinessModuleMetricRes = {
  metricCd: string;
  metricNm: string;
  metricVal: string;
};

export type BusinessModuleRes = {
  moduleCd: string;
  menuCd: string;
  groupCd: string;
  moduleNm: string;
  description: string;
  legacyMenuCd: string;
  legacyUri: string;
  primaryTableNm: string;
  tableNms: string[];
  itemCnt: number;
  metricList: BusinessModuleMetricRes[];
};

export type BusinessModuleListRes = {
  itemList: BusinessModuleRes[];
};

export type BusinessRecordColumnRes = {
  columnNm: string;
  label: string;
  dataTpCd: "code" | "date" | "datetime" | "decimal" | "number" | "text" | "time" | "yn" | string;
  keyYn: boolean;
  visibleYn: boolean;
  writableYn: boolean;
  requiredYn: boolean;
  dspOrd: number;
};

export type BusinessRecordRow = Record<string, string | number | boolean | null>;

export type BusinessRecordListRes = {
  moduleCd: string;
  tableNm: string;
  creatableYn: boolean;
  editableYn: boolean;
  deletableYn: boolean;
  columnList: BusinessRecordColumnRes[];
  itemList: BusinessRecordRow[];
  totCnt: number;
  page: number;
  size: number;
};

export type BusinessRecordRes = {
  moduleCd: string;
  tableNm: string;
  values: BusinessRecordRow;
};

export type BusinessRecordSaveReq = {
  values: BusinessRecordRow;
};

export type BusinessRelatedTableRes = {
  tableNm: string;
  label: string;
  columnList: string[];
  itemList: BusinessRecordRow[];
  totCnt: number;
};

export type BusinessRelatedListRes = {
  moduleCd: string;
  recordId: number;
  tableList: BusinessRelatedTableRes[];
};

export const LEAVE_API_PATH = "/api/v1/leaves";

export interface LeaveApplResponse {
  name: string;
  dept: string;
  position: string;
}

export interface LeavePartResponse {
  id: string;
  name: string;
  dept: string;
  position: string;
  orgId: string;
}

export interface LeaveOrgResponse {
  id: string;
  label: string;
  children?: LeaveOrgResponse[];
}

export interface LeaveBalResponse {
  availableDays: number;
  afterRequestDays: number;
  previousYearDays: number;
  currentYearDays: number;
}

export interface LeaveAprvStepResponse {
  id: string;
  order: number;
  usrs: LeavePartResponse[];
}

export interface LeaveReqResponse {
  id: string;
  no: number;
  leaveType: string;
  deductionType: string;
  leaveUnit: string;
  startDate: string;
  endDate: string;
  days: number;
  approverStatus: string;
  status: string;
  createdAt: string;
  attachmentName?: string | null;
  reason?: string | null;
  approvalSteps: LeaveAprvStepResponse[];
  ccs: LeavePartResponse[];
  canEdit?: boolean | null;
  canCancel?: boolean | null;
  canApprove?: boolean | null;
  canReject?: boolean | null;
  myRoleCd?: string | null;
}

export interface LeaveListResponse {
  balance: LeaveBalResponse;
  itemList: LeaveReqResponse[];
}

export interface LeaveCtxResponse {
  applicant: LeaveApplResponse;
  balance: LeaveBalResponse;
  leaveTypeOpts: string[];
  deductionTypeOpts: string[];
  leaveUnitOpts: string[];
  statusOpts: string[];
  organizations: LeaveOrgResponse[];
  emps: LeavePartResponse[];
}

export interface LeaveAprvStepRequest {
  order: number;
  usrIds: string[];
}

export interface LeaveSaveRequest {
  leaveType: string;
  deductionType: string;
  leaveUnit: string;
  startDate: string;
  endDate: string;
  attachmentName?: string | null;
  reason?: string | null;
  approvalSteps: LeaveAprvStepRequest[];
  ccIds: string[];
  nextStatus: "Draft" | "Requested";
}

export interface LeaveCalcRequest {
  leaveId?: string | null;
  leaveType: string;
  deductionType: string;
  leaveUnit: string;
  startDate: string;
  endDate: string;
}

export interface LeaveDupResponse {
  type: string;
  id: string;
  startDate: string;
  endDate: string;
}

export interface LeaveCalcResponse {
  resultCd: string;
  resultMsg: string;
  previousYearDays: number;
  currentYearDays: number;
  days: number;
  afterRequestDays: number;
  duplicates: LeaveDupResponse[];
}

export const LEAVE_API_PATH = "/api/v1/leaves";

export interface LeaveApplicantRes {
  name: string;
  department: string;
  position: string;
}

export interface LeaveParticipantRes {
  id: string;
  name: string;
  department: string;
  position: string;
  orgId: string;
}

export interface LeaveOrganizationRes {
  id: string;
  label: string;
  children?: LeaveOrganizationRes[];
}

export interface LeaveBalanceRes {
  availableDays: number;
  afterRequestDays: number;
  previousYearDays: number;
  currentYearDays: number;
}

export interface LeaveApprovalStepRes {
  id: string;
  order: number;
  users: LeaveParticipantRes[];
}

export interface LeaveRequestRes {
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
  approvalSteps: LeaveApprovalStepRes[];
  ccs: LeaveParticipantRes[];
  canEdit?: boolean | null;
  canCancel?: boolean | null;
  canApprove?: boolean | null;
  canReject?: boolean | null;
  myRoleCd?: string | null;
}

export interface LeaveListRes {
  balance: LeaveBalanceRes;
  itemList: LeaveRequestRes[];
}

export interface LeaveContextRes {
  applicant: LeaveApplicantRes;
  balance: LeaveBalanceRes;
  leaveTypeOptions: string[];
  deductionTypeOptions: string[];
  leaveUnitOptions: string[];
  statusOptions: string[];
  organizations: LeaveOrganizationRes[];
  employees: LeaveParticipantRes[];
}

export interface LeaveApprovalStepReq {
  order: number;
  userIds: string[];
}

export interface LeaveSaveReq {
  leaveType: string;
  deductionType: string;
  leaveUnit: string;
  startDate: string;
  endDate: string;
  attachmentName?: string | null;
  reason?: string | null;
  approvalSteps: LeaveApprovalStepReq[];
  ccIds: string[];
  nextStatus: "Draft" | "Requested";
}

export interface LeaveCalculateReq {
  leaveId?: string | null;
  leaveType: string;
  deductionType: string;
  leaveUnit: string;
  startDate: string;
  endDate: string;
}

export interface LeaveDuplicateRes {
  type: string;
  id: string;
  startDate: string;
  endDate: string;
}

export interface LeaveCalculateRes {
  resultCd: string;
  resultMessage: string;
  previousYearDays: number;
  currentYearDays: number;
  days: number;
  afterRequestDays: number;
  duplicates: LeaveDuplicateRes[];
}

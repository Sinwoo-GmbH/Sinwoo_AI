export type LeaveStatus =
  | "Draft"
  | "Requested"
  | "Approved"
  | "Rejected"
  | "Cancelled"
  | "Admin Cancelled";

export type ApproverStatus =
  | "No Approver"
  | "Waiting"
  | `Approved by ${number}`
  | "Rejected";

export type LeaveType =
  | "Annual Leave"
  | "Sick Leave"
  | "Marriage Leave"
  | "Bereavement Leave"
  | "Unpaid Leave"
  | "Special Leave";

export type DeductionType = "Deducted Leave" | "Non-deducted Leave";

export type LeaveUnit = "Full Day" | "Half Day AM" | "Half Day PM";

export type LeaveFiltStatus = "All" | LeaveStatus;

export type LeavePart = {
  id: string;
  name: string;
  dept: string;
  position: string;
  orgId: string;
};

export type AprvStep = {
  id: string;
  order: number;
  usrs: LeavePart[];
};

export type LeaveOrgNode = {
  id: string;
  label: string;
  children?: LeaveOrgNode[];
};

export type LeaveReqRec = {
  id: string;
  no: number;
  leaveType: LeaveType;
  deductionType: DeductionType;
  leaveUnit: LeaveUnit;
  startDate: string;
  endDate: string;
  days: number;
  approverStatus: ApproverStatus;
  status: LeaveStatus;
  createdAt: string;
  attachmentName: string | null;
  reason?: string | null;
  approvalSteps: AprvStep[];
  ccs: LeavePart[];
  canEdit?: boolean | null;
  canCancel?: boolean | null;
  canApprove?: boolean | null;
  canReject?: boolean | null;
  myRoleCd?: string | null;
};

export type LeaveReqFormValue = {
  id: string | null;
  leaveType: LeaveType;
  deductionType: DeductionType;
  leaveUnit: LeaveUnit;
  startDate: string;
  endDate: string;
  attachmentName: string | null;
  reason: string;
  approvalSteps: AprvStep[];
  ccs: LeavePart[];
};

export type LeaveFiltValue = {
  startDateFrom: string;
  startDateTo: string;
  status: LeaveFiltStatus;
};

export type LeaveApplProfile = {
  name: string;
  dept: string;
  position: string;
};

export const LEAVE_AVAILABLE_DAYS = 14.5;
export const LEAVE_AFTER_REQUEST_PREVIEW = 13.5;

export const LEAVE_STATUS_OPTS: readonly LeaveFiltStatus[] = [
  "All",
  "Draft",
  "Requested",
  "Approved",
  "Rejected",
  "Cancelled",
  "Admin Cancelled",
] as const;

export const LEAVE_TYPE_OPTS: readonly LeaveType[] = [
  "Annual Leave",
  "Sick Leave",
  "Marriage Leave",
  "Bereavement Leave",
  "Unpaid Leave",
  "Special Leave",
] as const;

export const DEDUCTION_TYPE_OPTS: readonly DeductionType[] = [
  "Deducted Leave",
  "Non-deducted Leave",
] as const;

export const LEAVE_UNIT_OPTS: readonly LeaveUnit[] = [
  "Full Day",
  "Half Day AM",
  "Half Day PM",
] as const;

export const MOCK_LEAVE_APPL: LeaveApplProfile = {
  name: "Juyong Lee",
  dept: "Development",
  position: "Manager",
};

export const MOCK_LEAVE_ORGS: LeaveOrgNode[] = [
  {
    id: "org-germany",
    label: "Germany",
    children: [
      {
        id: "org-berlin",
        label: "Berlin Office",
        children: [
          { id: "org-berlin-head", label: "Head Office" },
          { id: "org-berlin-finance", label: "Finance Team" },
        ],
      },
      {
        id: "org-frankfurt",
        label: "Frankfurt Office",
        children: [{ id: "org-frankfurt-hr", label: "HR Team" }],
      },
    ],
  },
  {
    id: "org-korea",
    label: "Korea",
    children: [
      {
        id: "org-seoul",
        label: "Seoul Office",
        children: [{ id: "org-seoul-dev", label: "Development Team" }],
      },
    ],
  },
];

export const MOCK_LEAVE_EMPS: LeavePart[] = [
  {
    id: "usr-max-muller",
    name: "Max Muller",
    dept: "Head Office",
    position: "Director",
    orgId: "org-berlin-head",
  },
  {
    id: "usr-anna-schmidt",
    name: "Anna Schmidt",
    dept: "HR",
    position: "HR Manager",
    orgId: "org-frankfurt-hr",
  },
  {
    id: "usr-daniel-weber",
    name: "Daniel Weber",
    dept: "Finance",
    position: "Finance Lead",
    orgId: "org-berlin-finance",
  },
  {
    id: "usr-jisoo-kim",
    name: "Jisoo Kim",
    dept: "Development",
    position: "Engineer",
    orgId: "org-seoul-dev",
  },
  {
    id: "usr-sofia-becker",
    name: "Sofia Becker",
    dept: "Head Office",
    position: "Executive Assistant",
    orgId: "org-berlin-head",
  },
];

function cloneParts(usrs: LeavePart[]) {
  return usrs.map((user) => ({ ...user }));
}

function buildApprovalStep(
  id: string,
  order: number,
  usrs: LeavePart[]
): AprvStep {
  return {
    id,
    order,
    usrs: cloneParts(usrs),
  };
}

export function normalizeAprvSteps(steps: AprvStep[]) {
  const normalized = steps
    .filter(Boolean)
    .map((step, index) => ({
      id: step.id || `approval-step-${index + 1}`,
      order: index + 1,
      usrs: cloneParts(step.usrs ?? []),
    }));

  return normalized.length
    ? normalized
    : [buildApprovalStep("approval-step-1", 1, [])];
}

export function cloneAprvSteps(steps: AprvStep[]) {
  return normalizeAprvSteps(steps);
}

export function createEmptyAprvStep(order: number): AprvStep {
  return buildApprovalStep(
    `approval-step-${Date.now()}-${order}`,
    order,
    []
  );
}

export function formatAprvStepLabel(order: number) {
  return `Step ${order}`;
}

function createAprvSteps(
  ...usrsPerStep: LeavePart[][]
): AprvStep[] {
  return normalizeAprvSteps(
    usrsPerStep.map((usrs, index) =>
      buildApprovalStep(`approval-step-${index + 1}`, index + 1, usrs)
    )
  );
}

const DEFAULT_APRV_STEPS = createAprvSteps(
  [MOCK_LEAVE_EMPS[0], MOCK_LEAVE_EMPS[4]],
  [MOCK_LEAVE_EMPS[1]]
);

const DEFAULT_SINGLE_APRV_STEP = createAprvSteps([
  MOCK_LEAVE_EMPS[0],
]);

const DEFAULT_THREE_STEP_APRV = createAprvSteps(
  [MOCK_LEAVE_EMPS[0]],
  [MOCK_LEAVE_EMPS[1], MOCK_LEAVE_EMPS[4]],
  [MOCK_LEAVE_EMPS[2]]
);

const DEFAULT_CCS = cloneParts([MOCK_LEAVE_EMPS[2]]);

export const MOCK_LEAVE_REQUESTS: LeaveReqRec[] = [
  {
    id: "leave-1001",
    no: 1,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-21",
    endDate: "2026-04-21",
    days: 1,
    approverStatus: "Waiting",
    status: "Requested",
    createdAt: "2026-04-18 09:15",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_APRV_STEPS),
    ccs: cloneParts(DEFAULT_CCS),
  },
  {
    id: "leave-1002",
    no: 2,
    leaveType: "Sick Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-10",
    endDate: "2026-04-10",
    days: 1,
    approverStatus: "Approved by 1",
    status: "Approved",
    createdAt: "2026-04-09 15:20",
    attachmentName: "medical-note.pdf",
    approvalSteps: cloneAprvSteps(
      createAprvSteps(
        [MOCK_LEAVE_EMPS[0], MOCK_LEAVE_EMPS[4]],
        [MOCK_LEAVE_EMPS[1]]
      )
    ),
    ccs: [],
  },
  {
    id: "leave-1003",
    no: 3,
    leaveType: "Marriage Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-05-02",
    endDate: "2026-05-03",
    days: 2,
    approverStatus: "No Approver",
    status: "Draft",
    createdAt: "2026-04-17 11:05",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_SINGLE_APRV_STEP),
    ccs: cloneParts([MOCK_LEAVE_EMPS[4]]),
  },
  {
    id: "leave-1004",
    no: 4,
    leaveType: "Bereavement Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-03-28",
    endDate: "2026-03-30",
    days: 3,
    approverStatus: "Rejected",
    status: "Rejected",
    createdAt: "2026-03-24 10:45",
    attachmentName: "supporting-document.pdf",
    approvalSteps: cloneAprvSteps(DEFAULT_THREE_STEP_APRV),
    ccs: cloneParts(DEFAULT_CCS),
  },
  {
    id: "leave-1005",
    no: 5,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Half Day PM",
    startDate: "2026-02-14",
    endDate: "2026-02-14",
    days: 0.5,
    approverStatus: "No Approver",
    status: "Cancelled",
    createdAt: "2026-02-10 08:30",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_SINGLE_APRV_STEP),
    ccs: [],
  },
  {
    id: "leave-1006",
    no: 6,
    leaveType: "Special Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-01-07",
    endDate: "2026-01-07",
    days: 1,
    approverStatus: "No Approver",
    status: "Admin Cancelled",
    createdAt: "2026-01-05 17:10",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[1]], [MOCK_LEAVE_EMPS[2]])
    ),
    ccs: cloneParts([MOCK_LEAVE_EMPS[4]]),
  },
  {
    id: "leave-1007",
    no: 7,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-24",
    endDate: "2026-04-25",
    days: 2,
    approverStatus: "Waiting",
    status: "Requested",
    createdAt: "2026-04-19 08:20",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_THREE_STEP_APRV),
    ccs: [],
  },
  {
    id: "leave-1008",
    no: 8,
    leaveType: "Unpaid Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-04",
    endDate: "2026-04-04",
    days: 1,
    approverStatus: "Approved by 1",
    status: "Approved",
    createdAt: "2026-04-02 14:00",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[1], MOCK_LEAVE_EMPS[4]])
    ),
    ccs: [],
  },
  {
    id: "leave-1009",
    no: 9,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Half Day AM",
    startDate: "2026-03-19",
    endDate: "2026-03-19",
    days: 0.5,
    approverStatus: "No Approver",
    status: "Draft",
    createdAt: "2026-03-18 09:40",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_SINGLE_APRV_STEP),
    ccs: [],
  },
  {
    id: "leave-1010",
    no: 10,
    leaveType: "Sick Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-03-05",
    endDate: "2026-03-05",
    days: 1,
    approverStatus: "Rejected",
    status: "Rejected",
    createdAt: "2026-03-03 16:25",
    attachmentName: "clinic-note.pdf",
    approvalSteps: cloneAprvSteps(DEFAULT_APRV_STEPS),
    ccs: cloneParts(DEFAULT_CCS),
  },
  {
    id: "leave-1011",
    no: 11,
    leaveType: "Special Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-02-03",
    endDate: "2026-02-04",
    days: 2,
    approverStatus: "No Approver",
    status: "Cancelled",
    createdAt: "2026-01-31 12:10",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[1]], [MOCK_LEAVE_EMPS[2]])
    ),
    ccs: [],
  },
  {
    id: "leave-1012",
    no: 12,
    leaveType: "Bereavement Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-01-15",
    endDate: "2026-01-17",
    days: 3,
    approverStatus: "No Approver",
    status: "Admin Cancelled",
    createdAt: "2026-01-10 09:05",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps(
        [MOCK_LEAVE_EMPS[2]],
        [MOCK_LEAVE_EMPS[1], MOCK_LEAVE_EMPS[4]]
      )
    ),
    ccs: cloneParts([MOCK_LEAVE_EMPS[0]]),
  },
  {
    id: "leave-1013",
    no: 13,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-05-08",
    endDate: "2026-05-09",
    days: 2,
    approverStatus: "Waiting",
    status: "Requested",
    createdAt: "2026-04-19 10:15",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_APRV_STEPS),
    ccs: [],
  },
  {
    id: "leave-1014",
    no: 14,
    leaveType: "Sick Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-13",
    endDate: "2026-04-13",
    days: 1,
    approverStatus: "Approved by 1",
    status: "Approved",
    createdAt: "2026-04-12 09:10",
    attachmentName: "hospital-note.pdf",
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[1]], [MOCK_LEAVE_EMPS[2]])
    ),
    ccs: [],
  },
  {
    id: "leave-1015",
    no: 15,
    leaveType: "Unpaid Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-05-14",
    endDate: "2026-05-16",
    days: 3,
    approverStatus: "No Approver",
    status: "Draft",
    createdAt: "2026-04-18 13:05",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(DEFAULT_SINGLE_APRV_STEP),
    ccs: cloneParts([MOCK_LEAVE_EMPS[4]]),
  },
  {
    id: "leave-1016",
    no: 16,
    leaveType: "Marriage Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-06-02",
    endDate: "2026-06-04",
    days: 3,
    approverStatus: "Rejected",
    status: "Rejected",
    createdAt: "2026-04-08 15:35",
    attachmentName: "family-certificate.pdf",
    approvalSteps: cloneAprvSteps(DEFAULT_THREE_STEP_APRV),
    ccs: cloneParts(DEFAULT_CCS),
  },
  {
    id: "leave-1017",
    no: 17,
    leaveType: "Special Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Half Day PM",
    startDate: "2026-03-27",
    endDate: "2026-03-27",
    days: 0.5,
    approverStatus: "No Approver",
    status: "Cancelled",
    createdAt: "2026-03-20 11:20",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[0]], [MOCK_LEAVE_EMPS[1]])
    ),
    ccs: [],
  },
  {
    id: "leave-1018",
    no: 18,
    leaveType: "Bereavement Leave",
    deductionType: "Non-deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-02-21",
    endDate: "2026-02-23",
    days: 3,
    approverStatus: "No Approver",
    status: "Admin Cancelled",
    createdAt: "2026-02-16 08:55",
    attachmentName: null,
    approvalSteps: cloneAprvSteps(
      createAprvSteps([MOCK_LEAVE_EMPS[2]], [MOCK_LEAVE_EMPS[1]])
    ),
    ccs: cloneParts([MOCK_LEAVE_EMPS[0]]),
  },
];

export const DEFAULT_LEAVE_FILT_VALUE: LeaveFiltValue = {
  startDateFrom: "",
  startDateTo: "",
  status: "All",
};

export function calculateLeaveDays(
  startDate: string,
  endDate: string,
  leaveUnit: LeaveUnit
) {
  if (!startDate || !endDate) {
    return 0;
  }

  if (leaveUnit !== "Full Day") {
    return 0.5;
  }

  const start = new Date(`${startDate}T00:00:00`);
  const end = new Date(`${endDate}T00:00:00`);
  const diffMs = Math.max(end.getTime() - start.getTime(), 0);
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24)) + 1;
  return Number(diffDays.toFixed(1));
}

export function calculateAfterRequest(
  availableDays: number,
  deductionType: DeductionType,
  days: number
) {
  if (deductionType !== "Deducted Leave") {
    return availableDays;
  }

  return Number(Math.max(availableDays - days, 0).toFixed(1));
}

export function formatLeaveDays(days: number) {
  return days.toFixed(1);
}

function todayIsoDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function toDeductionTableLabel(deductionType: DeductionType) {
  return deductionType === "Deducted Leave" ? "Deducted" : "Non-deducted";
}

export function createDefaultLeaveReqFormValue(): LeaveReqFormValue {
  return {
    id: null,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: todayIsoDate(),
    endDate: todayIsoDate(),
    attachmentName: null,
    reason: "",
    approvalSteps: cloneAprvSteps(DEFAULT_APRV_STEPS),
    ccs: cloneParts(DEFAULT_CCS),
  };
}

export function toLeaveReqFormValue(
  rec: LeaveReqRec
): LeaveReqFormValue {
  return {
    id: rec.id,
    leaveType: rec.leaveType,
    deductionType: rec.deductionType,
    leaveUnit: rec.leaveUnit,
    startDate: rec.startDate,
    endDate: rec.endDate,
    attachmentName: rec.attachmentName,
    reason: rec.reason ?? "",
    approvalSteps: cloneAprvSteps(rec.approvalSteps),
    ccs: cloneParts(rec.ccs),
  };
}

export function toLeaveReqRec(
  value: LeaveReqFormValue,
  currentNo: number,
  nextStatus: "Draft" | "Requested",
  createdAt: string
): LeaveReqRec {
  const days = calculateLeaveDays(value.startDate, value.endDate, value.leaveUnit);
  const approvalSteps = cloneAprvSteps(value.approvalSteps);
  const hasApprovers = approvalSteps.some((step) => step.usrs.length > 0);

  return {
    id: value.id ?? `leave-${Date.now()}`,
    no: currentNo,
    leaveType: value.leaveType,
    deductionType: value.deductionType,
    leaveUnit: value.leaveUnit,
    startDate: value.startDate,
    endDate: value.endDate,
    days,
    approverStatus: hasApprovers && nextStatus === "Requested" ? "Waiting" : "No Approver",
    status: nextStatus,
    createdAt,
    attachmentName: value.attachmentName,
    reason: value.reason,
    approvalSteps,
    ccs: cloneParts(value.ccs),
  };
}

export function renumberLeaveReqs(recs: LeaveReqRec[]) {
  return recs.map((rec, index) => ({
    ...rec,
    no: index + 1,
  }));
}

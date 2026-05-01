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
  | "Approved by 1"
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

export type LeaveFilterStatus = "All" | LeaveStatus;

export type LeaveParticipant = {
  id: string;
  name: string;
  department: string;
  position: string;
  orgId: string;
};

export type ApprovalStep = {
  id: string;
  order: number;
  users: LeaveParticipant[];
};

export type LeaveOrganizationNode = {
  id: string;
  label: string;
  children?: LeaveOrganizationNode[];
};

export type LeaveRequestRecord = {
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
  approvalSteps: ApprovalStep[];
  ccs: LeaveParticipant[];
};

export type LeaveRequestFormValue = {
  id: string | null;
  leaveType: LeaveType;
  deductionType: DeductionType;
  leaveUnit: LeaveUnit;
  startDate: string;
  endDate: string;
  attachmentName: string | null;
  approvalSteps: ApprovalStep[];
  ccs: LeaveParticipant[];
};

export type LeaveFilterValue = {
  startDateFrom: string;
  startDateTo: string;
  status: LeaveFilterStatus;
};

export type LeaveApplicantProfile = {
  name: string;
  department: string;
  position: string;
};

export const LEAVE_AVAILABLE_DAYS = 14.5;
export const LEAVE_AFTER_REQUEST_PREVIEW = 13.5;

export const LEAVE_STATUS_OPTIONS: readonly LeaveFilterStatus[] = [
  "All",
  "Draft",
  "Requested",
  "Approved",
  "Rejected",
  "Cancelled",
  "Admin Cancelled",
] as const;

export const LEAVE_TYPE_OPTIONS: readonly LeaveType[] = [
  "Annual Leave",
  "Sick Leave",
  "Marriage Leave",
  "Bereavement Leave",
  "Unpaid Leave",
  "Special Leave",
] as const;

export const DEDUCTION_TYPE_OPTIONS: readonly DeductionType[] = [
  "Deducted Leave",
  "Non-deducted Leave",
] as const;

export const LEAVE_UNIT_OPTIONS: readonly LeaveUnit[] = [
  "Full Day",
  "Half Day AM",
  "Half Day PM",
] as const;

export const MOCK_LEAVE_APPLICANT: LeaveApplicantProfile = {
  name: "Juyong Lee",
  department: "Development",
  position: "Manager",
};

export const MOCK_LEAVE_ORGANIZATIONS: LeaveOrganizationNode[] = [
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

export const MOCK_LEAVE_EMPLOYEES: LeaveParticipant[] = [
  {
    id: "usr-max-muller",
    name: "Max Muller",
    department: "Head Office",
    position: "Director",
    orgId: "org-berlin-head",
  },
  {
    id: "usr-anna-schmidt",
    name: "Anna Schmidt",
    department: "HR",
    position: "HR Manager",
    orgId: "org-frankfurt-hr",
  },
  {
    id: "usr-daniel-weber",
    name: "Daniel Weber",
    department: "Finance",
    position: "Finance Lead",
    orgId: "org-berlin-finance",
  },
  {
    id: "usr-jisoo-kim",
    name: "Jisoo Kim",
    department: "Development",
    position: "Engineer",
    orgId: "org-seoul-dev",
  },
  {
    id: "usr-sofia-becker",
    name: "Sofia Becker",
    department: "Head Office",
    position: "Executive Assistant",
    orgId: "org-berlin-head",
  },
];

function cloneParticipants(users: LeaveParticipant[]) {
  return users.map((user) => ({ ...user }));
}

function buildApprovalStep(
  id: string,
  order: number,
  users: LeaveParticipant[]
): ApprovalStep {
  return {
    id,
    order,
    users: cloneParticipants(users),
  };
}

export function normalizeApprovalSteps(steps: ApprovalStep[]) {
  const normalized = steps
    .filter(Boolean)
    .map((step, index) => ({
      id: step.id || `approval-step-${index + 1}`,
      order: index + 1,
      users: cloneParticipants(step.users ?? []),
    }));

  return normalized.length
    ? normalized
    : [buildApprovalStep("approval-step-1", 1, [])];
}

export function cloneApprovalSteps(steps: ApprovalStep[]) {
  return normalizeApprovalSteps(steps);
}

export function createEmptyApprovalStep(order: number): ApprovalStep {
  return buildApprovalStep(
    `approval-step-${Date.now()}-${order}`,
    order,
    []
  );
}

export function formatApprovalStepLabel(order: number) {
  return `Step ${order}`;
}

function createApprovalSteps(
  ...usersPerStep: LeaveParticipant[][]
): ApprovalStep[] {
  return normalizeApprovalSteps(
    usersPerStep.map((users, index) =>
      buildApprovalStep(`approval-step-${index + 1}`, index + 1, users)
    )
  );
}

const DEFAULT_APPROVAL_STEPS = createApprovalSteps(
  [MOCK_LEAVE_EMPLOYEES[0], MOCK_LEAVE_EMPLOYEES[4]],
  [MOCK_LEAVE_EMPLOYEES[1]]
);

const DEFAULT_SINGLE_APPROVAL_STEP = createApprovalSteps([
  MOCK_LEAVE_EMPLOYEES[0],
]);

const DEFAULT_THREE_STEP_APPROVAL = createApprovalSteps(
  [MOCK_LEAVE_EMPLOYEES[0]],
  [MOCK_LEAVE_EMPLOYEES[1], MOCK_LEAVE_EMPLOYEES[4]],
  [MOCK_LEAVE_EMPLOYEES[2]]
);

const DEFAULT_CCS = cloneParticipants([MOCK_LEAVE_EMPLOYEES[2]]);

export const MOCK_LEAVE_REQUESTS: LeaveRequestRecord[] = [
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
    approvalSteps: cloneApprovalSteps(DEFAULT_APPROVAL_STEPS),
    ccs: cloneParticipants(DEFAULT_CCS),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps(
        [MOCK_LEAVE_EMPLOYEES[0], MOCK_LEAVE_EMPLOYEES[4]],
        [MOCK_LEAVE_EMPLOYEES[1]]
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
    approvalSteps: cloneApprovalSteps(DEFAULT_SINGLE_APPROVAL_STEP),
    ccs: cloneParticipants([MOCK_LEAVE_EMPLOYEES[4]]),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_THREE_STEP_APPROVAL),
    ccs: cloneParticipants(DEFAULT_CCS),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_SINGLE_APPROVAL_STEP),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[1]], [MOCK_LEAVE_EMPLOYEES[2]])
    ),
    ccs: cloneParticipants([MOCK_LEAVE_EMPLOYEES[4]]),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_THREE_STEP_APPROVAL),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[1], MOCK_LEAVE_EMPLOYEES[4]])
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
    approvalSteps: cloneApprovalSteps(DEFAULT_SINGLE_APPROVAL_STEP),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_APPROVAL_STEPS),
    ccs: cloneParticipants(DEFAULT_CCS),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[1]], [MOCK_LEAVE_EMPLOYEES[2]])
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps(
        [MOCK_LEAVE_EMPLOYEES[2]],
        [MOCK_LEAVE_EMPLOYEES[1], MOCK_LEAVE_EMPLOYEES[4]]
      )
    ),
    ccs: cloneParticipants([MOCK_LEAVE_EMPLOYEES[0]]),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_APPROVAL_STEPS),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[1]], [MOCK_LEAVE_EMPLOYEES[2]])
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
    approvalSteps: cloneApprovalSteps(DEFAULT_SINGLE_APPROVAL_STEP),
    ccs: cloneParticipants([MOCK_LEAVE_EMPLOYEES[4]]),
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
    approvalSteps: cloneApprovalSteps(DEFAULT_THREE_STEP_APPROVAL),
    ccs: cloneParticipants(DEFAULT_CCS),
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[0]], [MOCK_LEAVE_EMPLOYEES[1]])
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
    approvalSteps: cloneApprovalSteps(
      createApprovalSteps([MOCK_LEAVE_EMPLOYEES[2]], [MOCK_LEAVE_EMPLOYEES[1]])
    ),
    ccs: cloneParticipants([MOCK_LEAVE_EMPLOYEES[0]]),
  },
];

export const DEFAULT_LEAVE_FILTER_VALUE: LeaveFilterValue = {
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

export function toDeductionTableLabel(deductionType: DeductionType) {
  return deductionType === "Deducted Leave" ? "Deducted" : "Non-deducted";
}

export function createDefaultLeaveRequestFormValue(): LeaveRequestFormValue {
  return {
    id: null,
    leaveType: "Annual Leave",
    deductionType: "Deducted Leave",
    leaveUnit: "Full Day",
    startDate: "2026-04-21",
    endDate: "2026-04-21",
    attachmentName: null,
    approvalSteps: cloneApprovalSteps(DEFAULT_APPROVAL_STEPS),
    ccs: cloneParticipants(DEFAULT_CCS),
  };
}

export function toLeaveRequestFormValue(
  record: LeaveRequestRecord
): LeaveRequestFormValue {
  return {
    id: record.id,
    leaveType: record.leaveType,
    deductionType: record.deductionType,
    leaveUnit: record.leaveUnit,
    startDate: record.startDate,
    endDate: record.endDate,
    attachmentName: record.attachmentName,
    approvalSteps: cloneApprovalSteps(record.approvalSteps),
    ccs: cloneParticipants(record.ccs),
  };
}

export function toLeaveRequestRecord(
  value: LeaveRequestFormValue,
  currentNo: number,
  nextStatus: "Draft" | "Requested",
  createdAt: string
): LeaveRequestRecord {
  const days = calculateLeaveDays(value.startDate, value.endDate, value.leaveUnit);
  const approvalSteps = cloneApprovalSteps(value.approvalSteps);
  const hasApprovers = approvalSteps.some((step) => step.users.length > 0);

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
    approvalSteps,
    ccs: cloneParticipants(value.ccs),
  };
}

export function renumberLeaveRequests(records: LeaveRequestRecord[]) {
  return records.map((record, index) => ({
    ...record,
    no: index + 1,
  }));
}

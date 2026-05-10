# Sinwoo Naming Convention

## Scope

This convention applies to backend Java files/classes and frontend TypeScript/TSX files/components.
Use AS-IS-style business abbreviations for domain terms, but keep common suffixes readable.

## Directory And Route Policy

These root directories are never abbreviated:

- `common`
- `platform`
- `business`
- `frontend`
- `components`
- `features`
- `lib`

Backend package and frontend route names should remain readable. Use route paths such as:

- `/requests/leave`
- `/requests/biz-trips`
- `/claims/expense-reports`
- `/finance/bank-transactions`
- `/finance/financial-statements`
- `/reports/work-time`

File names and exported names may use domain abbreviations, for example:

- `work-time-rpt-page.tsx`
- `leave-bal-sum.tsx`
- `emp-picker-modal.tsx`
- `common-cd-contract.ts`
- `wsp-mnu-utils.ts`
- `attnd-contract.ts`
- `biz-mod-page.tsx`

## Suffixes

Never abbreviate these suffixes:

| Suffix | Do Not Use |
| --- | --- |
| Controller | Ctrl |
| Service | Svc |
| ServiceImpl | SvcImpl |
| Repository | Repo |
| Domain | Dom |
| Entity | Ent |
| Dto | DTO shorthand variants |
| Request | Req |
| Response | Res |
| Mapper | Map |
| Config | Cfg |
| Provider | Prvdr |
| Component | Comp |
| Page | Pg |
| Layout | Lyt |
| Table | Tbl |
| Form | Frm |
| Modal | Mdl |
| Hook | Hk |
| Store | Str |
| Client | Clnt |
| Util | Utl |

## Approved Domain Abbreviations

| Domain Term | Abbreviation |
| --- | --- |
| Expense | Exp |
| Report | Rpt |
| Approval | Aprv |
| BusinessTrip | BizTrip |
| Vacation | Leave |
| Leave | Leave |
| Account | Acct |
| Accounting | Acctg |
| Transaction | Txn |
| Attachment | Attch |
| Department | Dept |
| Employee | Emp |
| Company | Co |
| User | Usr |
| Menu | Mnu |
| Code | Cd |
| Recurring | Recur |
| Asset | Asset |
| Payroll | Payroll |
| Statement | Stmt |
| Ledger | Ldgr |
| Balance | Bal |
| Detail | Dtl |
| History | Hist |
| Location | Loc |
| Holiday | Holi |
| PeriodClose | PeriodClose |
| FinancialStatement | FiStmt |
| BankAccount | BankAcct |
| BankTransaction | BankTxn |
| ExpenseMatching | ExpMatching |
| WorkLocation | WorkLoc |
| Attendance | Attnd |
| Business | Biz |
| Module | Mod |
| Record | Rec |
| Column | Col |
| Filter | Filt |
| Option | Opt |
| Calendar | Cal |
| Calculate | Calc |
| Duplicate | Dup |
| Context | Ctx |
| Organization | Org |
| Billing | Bill |
| Subscription | Subscr |
| SubscriptionPlan | SubscrPlan |
| Payment | Pay |
| Authentication | Auth |
| Credential | Cred |
| Encryption | Encrypt |
| TenantContext | TenantCtx |
| Workspace | Wsp |
| Content | Cnt |
| Container | Cntr |
| Messages | Msgs |
| Description | Desc |
| Placeholder | Ph |
| Section | Sect |
| Header | Hdr |
| Summary | Sum |
| Participant | Part |
| Applicant | Appl |
| Action | Act |
| Allocation | Alloc |

## Java Examples

| Before | After |
| --- | --- |
| ExpenseReportController | ExpRptController |
| ExpenseReportService | ExpRptService |
| ExpenseReportServiceImpl | ExpRptServiceImpl |
| ExpenseReportRepository | ExpRptRepository |
| ExpenseReportDto | ExpRptDto |
| BusinessTripController | BizTripController |
| BusinessTripApprovalService | BizTripAprvService |
| LedgerEntryRepository | LdgrEntryRepository |
| CompanyController | CoController |
| DepartmentRepository | DeptRepository |
| CommonCodeResponse | CommonCdResponse |
| AttendanceWorkTimeFilterOptionResponse | AttndWorkTimeFiltOptResponse |
| AttendanceWorkTimeFilterOptionsResponse | AttndWorkTimeFiltOptsResponse |
| LeaveRequestController | LeaveReqController |
| LeaveRequestServiceImpl | LeaveReqServiceImpl |
| LeaveRequestResponse | LeaveReqResponse |
| BizModController | BizModController |
| BusinessRecordColumnResponse | BizRecColResponse |
| BusinessRecordServiceImpl | BizRecServiceImpl |
| SubscriptionPlanListResponse | SubscrPlanListResponse |
| SubscriptionPlanController | SubscrPlanController |
| SubscriptionServiceImpl | SubscrServiceImpl |
| PaymentTxnController | PayTxnController |
| CredentialLoginRequest | CredLoginRequest |
| CredentialKeyResponse | CredKeyResponse |
| JwtAuthenticationFilter | JwtAuthFilt |
| AuthenticatedUser | AuthenticatedUsr |
| AccessLogFilter | AccessLogFilt |
| AuthErrorCode | AuthErrorCd |
| LeaveParticipantResponse | LeavePartResponse |
| LeaveApplicantResponse | LeaveApplResponse |
| LeaveActionRequest | LeaveActRequest |

## TypeScript And TSX Examples

| Before | After |
| --- | --- |
| ExpenseReportPage.tsx | ExpRptPage.tsx |
| BusinessTripApprovalModal.tsx | BizTripAprvModal.tsx |
| BankTransactionTable.tsx | BankTxnTable.tsx |
| WorkTimeReportPage | WorkTimeRptPage |
| EmployeePickerModal | EmpPickerModal |
| LeaveBalanceSummary | LeaveBalSum |
| SelectedUserChip | SelectedUsrChip |
| CommonCodeListResponse | CommonCdListResponse |
| AttendanceContract | AttndContract |
| BizModPage.tsx | BizModPage.tsx |
| RequestLeavePage.tsx | LeaveReqPage.tsx |
| BillingContract | BillContract |
| SubscriptionPlanListResponse | SubscrPlanListResponse |
| CredentialLoginPanel.tsx | CredLoginPanel.tsx |
| AttendanceCalendarCard.tsx | AttndCalCard.tsx |
| LeaveFilterBar.tsx | LeaveFiltBar.tsx |
| LeaveOrganizationNode | LeaveOrgNode |
| WorkspaceContentContainer | WspCntCntr |
| WorkspaceSectionPanel | WspSectPanel |
| WorkspacePageHeader | WspPageHdr |
| WorkspaceShellContent | WspShellCnt |
| WorkspaceTabContextMenuContent | WspTabCtxMnuCnt |
| CardContent | CardCnt |
| CardDescription | CardDesc |

## Identifier Examples

| Before | After |
| --- | --- |
| getWorkspaceAdminAreaMessages | getWspAdminAreaMsgs |
| getVisibleMnusForCurrentUser | getVisibleMnusForCurrentUsr |
| createBusinessRecord | createBizRec |
| getBusinessRecordRelatedTables | getBizRecRelTbls |
| createPaymentTransaction | createPayTxn |
| getPaymentTransactions | getPayTxns |
| buildReportContext | buildRptCtx |
| resolveValidationMessage | resolveValidationMsg |
| descriptionId | descId |
| placeholderDescription | phDesc |
| placeholderTitle | phTitle |
| moduleCd | modCd |
| moduleNm | modNm |
| tableNm | tblNm |
| tableNms | tblNms |
| columnList | colList |
| description | desc |

## ID And URL Examples

| Before | After |
| --- | --- |
| workspace-client-admin-placeholder-description | wsp-clnt-admin-ph-desc |
| workspace-content-container | wsp-cnt-cntr |
| workspace-rule-active-workspace | wsp-rule-active-wsp |
| sinwoo.workspace.shell.v1 | sinwoo.wsp.shell.v1 |

## Current Project Choices

- `Vacation` is represented as `Leave` in TO-BE because the active menu is Request Leave.
- `Asset`, `Payroll`, and `PeriodClose` remain unshortened to avoid ambiguous names.
- `Request` and `Response` are suffixes, so they stay full in new names. When `Request` is a middle business qualifier, abbreviate it as `Req`; for example, `LeaveRequestResponse` becomes `LeaveReqResponse`, while `LeaveSaveRequest` keeps the `Request` suffix.
- Framework/API attribute names are not business domain terms and must not be abbreviated; for example, JPA `columnDefinition` remains `columnDefinition`.
- API URL paths and user-visible labels remain stable even when file/type names are abbreviated.
- The same domain abbreviations apply to classes, files, exports, interfaces, methods, props, local variables, ids, and internal storage keys.
- External framework names and browser/React/Spring API names stay original; for example, `onContextMenu`, `getContentType`, `getStatusCode`, and `UsernamePasswordAuthenticationFilter` are not renamed.

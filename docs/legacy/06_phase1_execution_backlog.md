# SINWOO Next-Gen Upgrade Phase 1 Backlog

## 1. Goal

The goal of Phase 1 is to establish the next-generation common axis without breaking valuable legacy business assets.

This phase is primarily about:

- standards
- bridge architecture
- common masters
- compliance-safe migration groundwork

It is not primarily about feature quantity.

## 2. Top Priority Work

### P0-0. Fix the German Compliance Baseline

Goal:

- lock the platform baseline to German legal requirements for German entities
- connect GDPR/BDSG, GoBD, AO/HGB retention, e-invoicing, working-time law, and HR/AI controls into engineering work

Done means:

- an official compliance baseline document exists
- backlog priorities explicitly depend on that baseline
- finance, HR, payroll, attendance, OCR, and document modules inherit legal control requirements

### P0-1. Finalize the User, Company, and Role Common Axis

Goal:

- stabilize `TB_TENANT`
- stabilize `TB_CO`
- stabilize `TB_USR`
- stabilize `TB_ROLE`
- stabilize `TB_USR_ROLE`

Done means:

- legacy `tb_user` and next-gen `TB_USR` mapping is accepted
- company, department, employee, and role references are stable

### P0-2. Separate Access Log and Data History Correctly

Goal:

- keep operational request logging separate from business-row history
- preserve `_HIST + Trigger` as the row-history model

Done means:

- `TB_ACCESS_LOG` stays the operational log
- business-row history remains table-specific and database-driven

### P0-3. Standardize Runtime Environments

Goal:

- keep local, dev, and prod configuration separable
- keep secrets out of code
- keep run instructions and verification repeatable

Done means:

- runtime paths are documented
- migration and verification steps are executable

## 3. First Porting Targets

### P1-1. Authentication and Authorization Bridge

Reason:

- it is the entry point for all screens and modules
- it controls both B2B tenant isolation and internal administration

Target:

- final auth bridge for next-gen login
- role depth interpretation
- user-to-menu visibility
- tenant-aware auth foundation

### P1-2. User, Company, Department, and Employee

Reason:

- attendance, payroll, HR, finance, approval, and requests all depend on the same master axis

Target:

- user
- company
- department
- employee
- profile

### P1-3. Common Code, Locale, and Compliance Metadata

Reason:

- multilingual behavior, status codes, legal statuses, and compliance flags need one standard source

Target:

- code master
- locale master
- legal and retention policy support fields

## 4. Second Porting Targets

### P2-1. Attendance and Leave

Reason:

- high business value
- clear domain boundary
- strong fit for German-law validation around traceability and approval

### P2-2. Expense, Travel, and Personal Requests

Reason:

- visible workflow value
- can reuse employee, role, and approval models

### P2-3. Finance and Bookkeeping Bridge

Reason:

- must be upgraded under GoBD and e-invoicing assumptions, not as a generic CRUD port

## 5. Third Porting Targets

### P3-1. Payroll

Reason:

- high value but high rule complexity
- must align with German payroll and HR compliance boundaries

### P3-2. Asset and Financial Closing

Reason:

- high regression risk
- should be moved after master data and finance controls stabilize

## 6. Execution Order

1. fix the German compliance baseline
2. stabilize common-axis schema and fields
3. complete the authentication and authorization bridge
4. complete user, company, department, and employee migration
5. strengthen access logging and table history
6. start attendance domain migration
7. move into finance and document controls

## 7. Immediate Next Work

### Track A. Authentication Bridge

- finalize tenant-aware auth model
- add login bridge and role-context handling
- connect role depth and menu visibility to runtime authentication

### Track B. Attendance Domain

- design attendance records under German-law assumptions
- make correction, approval, and history traceable
- prepare leave and holiday structures

### Track C. Finance and Document Compliance

- define document retention and legal hold structure
- define e-invoice domain objects and document linking
- define GoBD-safe posting mutation rules

The current recommendation is:

- start with Track A and Track B in parallel architecture terms
- keep Track C immediately behind them because it is legally critical

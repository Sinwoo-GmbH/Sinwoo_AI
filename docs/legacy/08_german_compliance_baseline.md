# SINWOO German Compliance Baseline

## 1. Purpose

This document defines the non-optional legal baseline for the next-generation SINWOO platform when the product is used by German entities.

This is not treated as an optional customer feature. It is a platform-level architecture requirement.

The working principle is:

- Germany-first compliance for accounting, invoicing, labor, HR, payroll, and document retention
- legal controls are designed into schema, workflow, audit, and permissions
- customer-specific configuration can extend the product but cannot disable mandatory legal controls

This document is an engineering baseline, not a substitute for German legal, tax, payroll, or labor counsel.

## 2. Mandatory Legal Tracks

### 2.1 Data Protection: GDPR and BDSG

The platform processes employee, payroll, attendance, accounting, and identity data. That means GDPR and the German Federal Data Protection Act are not optional.

Engineering implications:

- privacy by design and by default
- least-privilege authorization by role, depth, and menu
- auditability for access, change history, export, and approval actions
- separation of internal SINWOO data and customer tenant data
- lawful processing basis per business process
- configurable retention and deletion handling
- support for information requests, correction, and deletion workflows
- EU-focused hosting and data flow review before transferring personal data outside the EEA

Implementation rules:

- no personal data export without authorization and audit trail
- no menu access without role-menu authorization mapping
- access logs and row history remain mandatory
- HR and payroll screens must support field-level masking where needed

### 2.2 Bookkeeping and Tax Records: GoBD

If SINWOO stores bookkeeping records, OCR document captures, journal entries, and approval workflows for German entities, the product must support GoBD-compatible operation.

Engineering implications:

- no silent overwrite of accounting-relevant records
- correction by reversal or explicit amendment trace, not hidden mutation
- immutable audit trail for document upload, extraction, review, approval, and posting
- exportability of tax-relevant records
- reproducibility of accounting-relevant processing steps
- retention-aware storage for originals and derived records

Implementation rules:

- OCR source file must remain linked to the final posting record
- booking-relevant documents cannot be hard-deleted in normal business flow
- every accounting status transition must be logged with actor and timestamp

### 2.3 Retention: AO and HGB

The platform must support German retention duties for accounting and commercial records.

Engineering implications:

- retention period must be part of the data model, not a manual convention
- legal hold must block deletion
- document lifecycle needs clear status separation: active, archived, retention-expired, deletion-blocked

Implementation rules:

- add retention policy fields to accounting and HR document domains
- keep source and derived files traceable during the retention window
- do not implement destructive purge flows without retention validation

### 2.4 E-Invoicing in German B2B

Domestic B2B invoicing in Germany moved to mandatory electronic invoicing from January 1, 2025, with transition rules. A simple PDF alone is not the target end state.

Engineering implications:

- inbound invoice handling must support structured e-invoice formats
- invoice storage must preserve the original structured payload
- invoice workflow must distinguish structured e-invoice, other electronic invoice, and paper origin
- OCR stays useful for receipts and non-structured legacy documents, but it is not the only invoice path

Implementation rules:

- introduce e-invoice domain support in the finance/document module
- retain original file plus parsed payload plus review result
- make tax-relevant invoice fields explicit, not embedded only in free-text OCR results

### 2.5 Working Time and Rest Rules

If SINWOO manages attendance and working time for German employees, the platform must be designed around German working-time law, not only convenience UX.

Engineering implications:

- capture start, end, breaks, and corrections with audit trail
- keep manager approval trail for manual corrections
- track overtime, rest periods, and violations or warnings
- keep holiday and public holiday configuration by applicable German state if needed

Implementation rules:

- time entries must be traceable and correction-aware
- attendance changes must create history records
- approval and override actions must be attributable to a named actor

### 2.6 HR and AI Usage

AI use in OCR and document support is acceptable, but AI use in employment and worker-management decisions must be treated carefully under the EU AI Act.

Engineering implications:

- do not allow fully automated HR decisions that materially affect employees
- keep AI as assistive support with human review in HR-sensitive flows
- preserve traceability of AI suggestion, reviewer, final decision, and override

Implementation rules:

- AI recommendation fields must be separate from final approved values
- add reviewer identity and approval timestamps to HR-sensitive decisions
- keep an option to disable AI-driven recommendations by domain

### 2.7 Cross-Border Data Flows

Because the target market is Korean companies with German entities, cross-border data access is part of the real operating model.

Engineering implications:

- default hosting and primary processing should stay EU-oriented
- remote access and support access must be logged
- data export outside the EEA requires transfer-basis validation

Implementation rules:

- treat Korea-related transfers as a controlled compliance topic, not an ad hoc support action
- support legal-basis documentation for cross-border transfer scenarios

## 3. Product Rules Derived from the Legal Baseline

These rules are now part of the target architecture:

1. Accounting-relevant documents and postings must be traceable end-to-end.
2. Attendance and HR changes must be actor-attributable and history-preserving.
3. Role and menu authorization is a compliance control, not only a UI preference.
4. Internal SINWOO tenant and customer tenants share the platform, but legal controls apply to both.
5. Germany-facing finance and HR modules must ship with German-law defaults before optional localization.
6. AI may assist, but final accountable decisions in sensitive areas must stay human-controlled.

## 4. Immediate Engineering Backlog Derived from German Law

### P0

- add legal compliance baseline to official architecture documents
- add retention-policy fields to finance and document domains
- define accounting mutation policy: reverse and correct, not silent overwrite
- define e-invoice domain model and file storage policy
- define attendance correction and approval audit model

### P1

- port attendance domain with German-law validation points
- port finance request and bookkeeping bridge with GoBD-safe status transitions
- add document locking and legal-hold support
- add HR-sensitive action logging and reviewer controls

### P2

- add e-invoice inbound processing support
- add payroll compliance checkpoints and integration boundaries
- add data subject request operations for privacy workflows

## 5. Source Baseline

Primary official sources used for this baseline:

- GDPR official text: https://eur-lex.europa.eu/eli/2016/679/oj
- German Federal Data Protection Act (BDSG): https://www.gesetze-im-internet.de/bdsg_2018/BDSG.pdf
- GoBD official BMF source: https://ao.bundesfinanzministerium.de/ao/2025/Anhaenge/BMF-Schreiben-und-gleichlautende-Laendererlasse/Anhang-33/anhang-33.html
- AO retention baseline: https://www.gesetze-im-internet.de/ao_1977/AO.pdf
- HGB retention baseline: https://www.gesetze-im-internet.de/hgb/__257.html
- BMF FAQ on mandatory e-invoicing from January 1, 2025: https://www.bundesfinanzministerium.de/Content/DE/FAQ/e-rechnung.html
- BMF letter on obligatory e-invoice introduction: https://www.bundesfinanzministerium.de/Content/DE/Downloads/BMF_Schreiben/Steuerarten/Umsatzsteuer/2024-10-15-einfuehrung-e-rechnung.pdf?__blob=publicationFile&v=4
- German Working Time Act (ArbZG): https://www.gesetze-im-internet.de/arbzg/BJNR117100994.html
- EU AI Act implementation overview: https://digital-strategy.ec.europa.eu/en/policies/regulatory-framework-ai
- Korea adequacy decision under GDPR: https://eur-lex.europa.eu/eli/dec/2023/636/oj/eng

## 6. Final Rule

From this point forward, the next-generation SINWOO platform is designed under the assumption that German legal compliance is a first-class product requirement.

That means:

- schema decisions must survive audit and retention requirements
- workflow decisions must survive tax and labor review
- AI decisions must remain explainable and reviewable
- future porting work must be checked against this baseline before release

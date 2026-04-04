# SINWOO Database Naming Standard

## 1. Purpose

This document defines the official abbreviation-based naming standard for SINWOO database objects.

## 2. General Principles

- Use uppercase letters only
- Separate words with underscore `_`
- Prefer approved abbreviations over long full names
- Keep the same abbreviation consistently across DB, backend API contracts, and frontend integration types
- Use singular table names

## 3. Table Naming

Format:

```text
TB_<ENTITY>
```

Examples:

- `TB_TENANT`
- `TB_CO`
- `TB_USR`
- `TB_ROLE`
- `TB_USR_ROLE`
- `TB_CHG_HIST`

## 4. Column Naming

### 4.1 Core Rules

- Primary key: `ID`
- Foreign key: `<ENTITY>_ID`
- Code: `_CD`
- Name: `_NM`
- Status code: `STS_CD`
- Number: `_NO`
- DateTime: `_DTM`
- JSON snapshot: `_JSON`

### 4.2 Audit Columns

Every business table must append the following columns at the end:

- `CRT_BY`
- `CRT_DTM`
- `UPD_BY`
- `UPD_DTM`

## 5. Constraint Naming

### Primary Key

```text
PK_<TABLE>
```

Example:

- `PK_TB_TENANT`

### Foreign Key

```text
FK_<TABLE>_<SEQ>
```

Examples:

- `FK_TB_CO_01`
- `FK_TB_USR_01`

### Normal Index

```text
IX_<TABLE>_<COLUMN>
```

Examples:

- `IX_TB_CO_TENANT_ID`
- `IX_TB_USR_CO_ID`

### Unique Key

```text
UK_<TABLE>_<COLUMN>
```

Examples:

- `UK_TB_TENANT_TENANT_CD`
- `UK_TB_USR_EML`

## 6. Approved Abbreviation Dictionary

| Full Term | Standard |
| --- | --- |
| TENANT | TENANT |
| COMPANY | CO |
| USER | USR |
| ROLE | ROLE |
| HISTORY | HIST |
| CHANGE | CHG |
| CODE | CD |
| NAME | NM |
| STATUS | STS |
| NUMBER | NO |
| REGISTRATION | REG |
| DISPLAY | DSP |
| PASSWORD | PWD |
| HASH | HASH |
| EMAIL | EML |
| LOCALE | LOCL |
| CREATED | CRT |
| UPDATED | UPD |
| DATE TIME | DTM |
| SNAPSHOT | SNAP |
| TABLE | TBL |
| ENTITY | ENT |
| ROW | ROW |
| TYPE | TP |
| JSON | JSON |

## 7. API Contract Naming

Backend response/request fields exposed for frontend integration should align with the same standard where it improves consistency.

Examples:

- `tenantCd`
- `tenantNm`
- `stsCd`
- `crtDtm`
- `updDtm`
- `totCnt`
- `itemList`

## 8. Standard Application

All new migration scripts, entity mappings, request/response DTOs, and frontend API types must follow this document.

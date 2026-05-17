-- ============================================================
-- V17: 휴가 신청 + 통합 결재선 + 공통코드 시드
-- ============================================================

-- ------------------------------------------------------------
-- 1. TB_LEAVE_REQ  (휴가 신청)
-- ------------------------------------------------------------
CREATE TABLE TB_LEAVE_REQ (
    ID              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'Leave request primary key',
    TENANT_ID       VARCHAR(20)     NOT NULL COMMENT 'Tenant code',
    CO_ID           VARCHAR(20)     NOT NULL COMMENT 'Company code',
    EMP_ID          VARCHAR(20)     NOT NULL COMMENT 'Applicant employee code',
    LEAVE_TP_CD     VARCHAR(20)     NOT NULL COMMENT 'Leave type code (VAC_TP: AN/SK/MR/BV/UP/SP)',
    DEDUCT_TP_CD    VARCHAR(20)     NOT NULL COMMENT 'Deduction type code (DEDUCT_TP: DD/ND)',
    LEAVE_UNIT_CD   VARCHAR(20)     NOT NULL COMMENT 'Leave unit code (LEAVE_UNIT: FD/AM/PM)',
    STR_DT          DATE            NOT NULL COMMENT 'Start date',
    END_DT          DATE            NOT NULL COMMENT 'End date',
    USE_DAYS        DECIMAL(4,1)    NULL     COMMENT 'Used days (calculated by server)',
    PRE_YR_USED     DECIMAL(4,1)    NULL     COMMENT 'Previous year carryover days used',
    CURR_YR_USED    DECIMAL(4,1)    NULL     COMMENT 'Current year days used',
    REASON          TEXT            NULL     COMMENT 'Reason (required for Special Leave)',
    ATCH_FILE_NM    VARCHAR(255)    NULL     COMMENT 'Attachment file name',
    STS_CD          VARCHAR(20)     NOT NULL COMMENT 'Status code (APRV_STS: DRF/REQ/APR/REJ/CAN/WAT)',
    REJ_REASON      TEXT            NULL     COMMENT 'Rejection reason',
    DEL_YN          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT 'Delete yes or no',
    CRT_BY          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM' COMMENT 'Created by',
    CRT_DTM         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created datetime',
    UPD_BY          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM' COMMENT 'Updated by',
    UPD_DTM         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_LEAVE_REQ PRIMARY KEY (ID)
) COMMENT='Leave request';

CREATE INDEX IX_TB_LEAVE_REQ_TENANT_CO     ON TB_LEAVE_REQ (TENANT_ID, CO_ID, DEL_YN);
CREATE INDEX IX_TB_LEAVE_REQ_EMP           ON TB_LEAVE_REQ (EMP_ID, DEL_YN);
CREATE INDEX IX_TB_LEAVE_REQ_STS           ON TB_LEAVE_REQ (STS_CD, DEL_YN);
CREATE INDEX IX_TB_LEAVE_REQ_DT            ON TB_LEAVE_REQ (STR_DT, END_DT);

-- ------------------------------------------------------------
-- 2. TB_APRV_LINE  (통합 결재선 — 모든 도메인 공용)
-- ------------------------------------------------------------
CREATE TABLE TB_APRV_LINE (
    ID              BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'Approval line primary key',
    TENANT_ID       VARCHAR(20)     NOT NULL COMMENT 'Tenant code',
    CO_ID           VARCHAR(20)     NOT NULL COMMENT 'Company code',
    REQ_TP_CD       VARCHAR(20)     NOT NULL COMMENT 'Request type code (LEAVE/TRIP/EXPENSE etc.)',
    REQ_ID          BIGINT          NOT NULL COMMENT 'Source request primary key',
    APRV_TP_CD      VARCHAR(10)     NOT NULL COMMENT 'Approval type (APP=approver, REF=cc reference)',
    EMP_ID          VARCHAR(20)     NOT NULL COMMENT 'Approver or cc employee code',
    STEP_ORDER      INT             NOT NULL COMMENT 'Approval step order (1,2,3… / REF=0)',
    STS_CD          VARCHAR(20)     NOT NULL COMMENT 'Status code (APRV_STS: WAT/REQ/APR/REJ/RF)',
    FINAL_YN        CHAR(1)         NOT NULL DEFAULT 'N' COMMENT 'Final approver yes or no',
    DEL_YN          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT 'Delete yes or no',
    CRT_BY          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM' COMMENT 'Created by',
    CRT_DTM         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created datetime',
    UPD_BY          VARCHAR(100)    NOT NULL DEFAULT 'SYSTEM' COMMENT 'Updated by',
    UPD_DTM         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_APRV_LINE PRIMARY KEY (ID)
) COMMENT='Unified approval line for all request types';

CREATE INDEX IX_TB_APRV_LINE_REQ   ON TB_APRV_LINE (REQ_TP_CD, REQ_ID, DEL_YN);
CREATE INDEX IX_TB_APRV_LINE_EMP   ON TB_APRV_LINE (EMP_ID, DEL_YN);

-- ------------------------------------------------------------
-- 3. 공통코드 시드
-- ------------------------------------------------------------

-- 3-1. 코드 그룹
INSERT INTO TB_CD_GRP (GRP_CD, GRP_NM_KO, GRP_NM_EN, GRP_NM_DE, SYS_YN, DSP_ORD)
VALUES
    ('VAC_TP',     '휴가 유형',   'Leave Type',      'Urlaubsart',       'Y', 30),
    ('DEDUCT_TP',  '차감 유형',   'Deduction Type',   'Abzugsart',        'Y', 31),
    ('LEAVE_UNIT', '휴가 단위',   'Leave Unit',       'Urlaubseinheit',   'Y', 32),
    ('APRV_STS',   '결재 상태',   'Approval Status',  'Genehmigungsstatus','Y', 33);

-- 3-2. VAC_TP (휴가 유형)
INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, DSP_ORD)
SELECT g.ID, v.CD, v.CD_NM_KO, v.CD_NM_EN, v.CD_NM_DE, v.DSP_ORD
FROM TB_CD_GRP g
JOIN (  SELECT 'AN' CD, '연차'     CD_NM_KO, 'Annual Leave'     CD_NM_EN, 'Jahresurlaub'       CD_NM_DE, 1 DSP_ORD
  UNION SELECT 'SK',    '병가',               'Sick Leave',                'Krankmeldung',               2
  UNION SELECT 'MR',    '결혼휴가',           'Marriage Leave',            'Hochzeitsurlaub',            3
  UNION SELECT 'BV',    '경조휴가',           'Bereavement Leave',         'Trauerurlaub',               4
  UNION SELECT 'UP',    '무급휴가',           'Unpaid Leave',              'Unbezahlter Urlaub',         5
  UNION SELECT 'SP',    '특별휴가',           'Special Leave',             'Sonderurlaub',               6
) v ON 1=1
WHERE g.GRP_CD = 'VAC_TP';

-- 3-3. DEDUCT_TP (차감 유형)
INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, DSP_ORD)
SELECT g.ID, v.CD, v.CD_NM_KO, v.CD_NM_EN, v.CD_NM_DE, v.DSP_ORD
FROM TB_CD_GRP g
JOIN (  SELECT 'DD' CD, '차감'   CD_NM_KO, 'Deducted'     CD_NM_EN, 'Abgezogen'         CD_NM_DE, 1 DSP_ORD
  UNION SELECT 'ND',    '비차감',           'Non-deducted',            'Nicht abgezogen',           2
) v ON 1=1
WHERE g.GRP_CD = 'DEDUCT_TP';

-- 3-4. LEAVE_UNIT (휴가 단위)
INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, DSP_ORD)
SELECT g.ID, v.CD, v.CD_NM_KO, v.CD_NM_EN, v.CD_NM_DE, v.DSP_ORD
FROM TB_CD_GRP g
JOIN (  SELECT 'FD' CD, '전일'     CD_NM_KO, 'Full Day'     CD_NM_EN, 'Ganzer Tag'   CD_NM_DE, 1 DSP_ORD
  UNION SELECT 'AM',    '오전반차',           'Half Day AM',             'Halber Tag VM',          2
  UNION SELECT 'PM',    '오후반차',           'Half Day PM',             'Halber Tag NM',          3
) v ON 1=1
WHERE g.GRP_CD = 'LEAVE_UNIT';

-- 3-5. APRV_STS (결재 상태)
INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, DSP_ORD)
SELECT g.ID, v.CD, v.CD_NM_KO, v.CD_NM_EN, v.CD_NM_DE, v.DSP_ORD
FROM TB_CD_GRP g
JOIN (  SELECT 'DRF' CD, '임시저장' CD_NM_KO, 'Draft'     CD_NM_EN, 'Entwurf'   CD_NM_DE, 1 DSP_ORD
  UNION SELECT 'REQ',    '요청',              'Requested',            'Beantragt',          2
  UNION SELECT 'APR',    '승인',              'Approved',             'Genehmigt',          3
  UNION SELECT 'REJ',    '반려',              'Rejected',             'Abgelehnt',          4
  UNION SELECT 'CAN',    '취소',              'Cancelled',            'Storniert',          5
  UNION SELECT 'WAT',    '대기',              'Waiting',              'Wartend',             6
) v ON 1=1
WHERE g.GRP_CD = 'APRV_STS';

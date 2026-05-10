ALTER TABLE TB_LEAVE_REQ
    ADD COLUMN IF NOT EXISTS DEDUCT_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Deduct leave balance yes or no' AFTER DAY_TP_CD,
    ADD COLUMN IF NOT EXISTS ATCH_FILE_NM VARCHAR(255) NULL COMMENT 'Attachment file name' AFTER REJ_RSN_CNTS;

CREATE INDEX IF NOT EXISTS IX_TB_LEAVE_REQ_STS_DT ON TB_LEAVE_REQ (TENANT_ID, CO_ID, STS_CD, STR_DT);
CREATE INDEX IF NOT EXISTS IX_TB_LEAVE_APRV_EMP_STS ON TB_LEAVE_APRV (TENANT_ID, CO_ID, APRV_EMP_ID, APRV_STS_CD);

INSERT INTO TB_CD_GRP (GRP_CD, GRP_NM_KO, GRP_NM_EN, GRP_NM_DE, SYS_YN, USE_YN, DSP_ORD, CRT_BY, UPD_BY)
VALUES
    ('VAC_TP', 'Vacation Type', 'Vacation Type', 'Vacation Type', 'N', 'Y', 210, 'SYSTEM', 'SYSTEM'),
    ('DAY_TP', 'Day Type', 'Day Type', 'Day Type', 'N', 'Y', 220, 'SYSTEM', 'SYSTEM'),
    ('DEDUCT_TP', 'Deduction Type', 'Deduction Type', 'Deduction Type', 'N', 'Y', 230, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    GRP_NM_KO = VALUES(GRP_NM_KO),
    GRP_NM_EN = VALUES(GRP_NM_EN),
    GRP_NM_DE = VALUES(GRP_NM_DE),
    USE_YN = VALUES(USE_YN),
    DSP_ORD = VALUES(DSP_ORD),
    UPD_BY = 'SYSTEM';

SET @GRP_VAC_TP = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'VAC_TP');
SET @GRP_DAY_TP = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'DAY_TP');
SET @GRP_DEDUCT_TP = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'DEDUCT_TP');

INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, USE_YN, DSP_ORD, CRT_BY, UPD_BY)
VALUES
    (@GRP_VAC_TP, 'AN', 'Annual Leave', 'Annual Leave', 'Annual Leave', 'Y', 1, 'SYSTEM', 'SYSTEM'),
    (@GRP_VAC_TP, 'SICK', 'Sick Leave', 'Sick Leave', 'Sick Leave', 'Y', 2, 'SYSTEM', 'SYSTEM'),
    (@GRP_VAC_TP, 'MARR', 'Marriage Leave', 'Marriage Leave', 'Marriage Leave', 'Y', 3, 'SYSTEM', 'SYSTEM'),
    (@GRP_VAC_TP, 'BERE', 'Bereavement Leave', 'Bereavement Leave', 'Bereavement Leave', 'Y', 4, 'SYSTEM', 'SYSTEM'),
    (@GRP_VAC_TP, 'UNPD', 'Unpaid Leave', 'Unpaid Leave', 'Unpaid Leave', 'Y', 5, 'SYSTEM', 'SYSTEM'),
    (@GRP_VAC_TP, 'SP', 'Special Leave', 'Special Leave', 'Special Leave', 'Y', 6, 'SYSTEM', 'SYSTEM'),
    (@GRP_DAY_TP, 'AD', 'Full Day', 'Full Day', 'Full Day', 'Y', 1, 'SYSTEM', 'SYSTEM'),
    (@GRP_DAY_TP, 'HAM', 'Half Day AM', 'Half Day AM', 'Half Day AM', 'Y', 2, 'SYSTEM', 'SYSTEM'),
    (@GRP_DAY_TP, 'HPM', 'Half Day PM', 'Half Day PM', 'Half Day PM', 'Y', 3, 'SYSTEM', 'SYSTEM'),
    (@GRP_DAY_TP, 'HD', 'Half Day', 'Half Day', 'Half Day', 'Y', 4, 'SYSTEM', 'SYSTEM'),
    (@GRP_DEDUCT_TP, 'DED', 'Deducted Leave', 'Deducted Leave', 'Deducted Leave', 'Y', 1, 'SYSTEM', 'SYSTEM'),
    (@GRP_DEDUCT_TP, 'NDED', 'Non-deducted Leave', 'Non-deducted Leave', 'Non-deducted Leave', 'Y', 2, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    CD_NM_KO = VALUES(CD_NM_KO),
    CD_NM_EN = VALUES(CD_NM_EN),
    CD_NM_DE = VALUES(CD_NM_DE),
    USE_YN = VALUES(USE_YN),
    DSP_ORD = VALUES(DSP_ORD),
    UPD_BY = 'SYSTEM';

INSERT INTO TB_EMP_HR_PROFILE (
    TENANT_ID, CO_ID, EMP_ID, LEGACY_EMP_ID, WORK_STR_TM, WORK_END_TM,
    STD_VAC_CNT, INC_VAC_CNT, CURR_VAC_CNT, PRE_VAC_CNT,
    LEGACY_RESIGN_YN, STS_CD, CRT_BY, UPD_BY
)
SELECT
    e.TENANT_ID,
    e.CO_ID,
    e.ID,
    e.EMP_NO,
    '08:00:00',
    '17:00:00',
    24.0,
    0.0,
    24.0,
    0.0,
    'N',
    'ACTIVE',
    'SYSTEM',
    'SYSTEM'
FROM TB_EMP e
WHERE e.STS_CD = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM TB_EMP_HR_PROFILE hp
      WHERE hp.EMP_ID = e.ID
  );

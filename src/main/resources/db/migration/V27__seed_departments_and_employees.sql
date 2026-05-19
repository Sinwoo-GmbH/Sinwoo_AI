-- =============================================================
-- V27: 부서(7건) + 직원(20건) 시드 마이그레이션
-- as-is: tb_dept_master / tb_employee_master
-- to-be: TB_DEPT / TB_EMP
-- =============================================================
-- 매핑 요약:
--   DEPT_ID       → DEPT_CD (VARCHAR)
--   PARENT='SW'   → UP_DEPT_ID=NULL (회사 루트)
--   RANK_CD       → JOB_TTL_CD (CE/MG/DR/IN)
--   RESIGN_YN='Y' → STS_CD='RESG', RETR_DT=NULL
--   RESIGN_YN='N' → STS_CD='ACTIVE'
--   SEX_CD=''     → NULL
--   BIRTH_DATE='0000-00-00' → NULL
--   '111' 테스트 부서 제외
-- =============================================================

-- -----------------------------------------------
-- Phase A: 부서 시드 (7건, SWH 기존 업데이트 포함)
-- -----------------------------------------------
INSERT INTO TB_DEPT (TENANT_ID, CO_ID, DEPT_CD, DEPT_NM, UP_DEPT_ID, DEPT_LVL_NO,
                     STS_CD, REGION_CD, VAC_CNT, VAC_INC, DSP_ORD, CRT_BY, UPD_BY)
VALUES
    (1, 1, 'SWH',  'Head Office',           NULL, 1, 'ACTIVE', 'HE', 24.0, 0.5, 1, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'IT',   'IT Department',          NULL, 1, 'ACTIVE', 'HE', 24.0, 0.5, 2, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'SEG',  'Samsung Electronics',    NULL, 1, 'ACTIVE', 'HE', 30.0, 0.0, 3, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'SSEG', 'Samsung Semiconductor',  NULL, 1, 'ACTIVE', 'BY', 30.0, 0.0, 4, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'QEG',  'Qenergy',               NULL, 1, 'ACTIVE', 'BE', 24.0, 0.0, 5, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'QCL',  'QCell',                  NULL, 1, 'ACTIVE', 'BE', 30.0, 0.0, 6, 'SYSTEM', 'SYSTEM'),
    (1, 1, 'HMSG', 'Hyundai Motorsport',     NULL, 1, 'ACTIVE', 'BY', 30.0, 0.0, 7, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    DEPT_NM     = VALUES(DEPT_NM),
    DEPT_LVL_NO = VALUES(DEPT_LVL_NO),
    STS_CD      = VALUES(STS_CD),
    REGION_CD   = VALUES(REGION_CD),
    VAC_CNT     = VALUES(VAC_CNT),
    VAC_INC     = VALUES(VAC_INC),
    DSP_ORD     = VALUES(DSP_ORD),
    UPD_BY      = VALUES(UPD_BY);

-- -----------------------------------------------
-- Phase B: 직원 시드 (20건)
-- -----------------------------------------------

-- 부서 PK 조회 (AUTO_INCREMENT ID는 예측 불가하므로 변수로 조회)
SET @dept_swh  = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'SWH');
SET @dept_it   = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'IT');
SET @dept_seg  = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'SEG');
SET @dept_sseg = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'SSEG');
SET @dept_qeg  = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'QEG');
SET @dept_qcl  = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'QCL');
SET @dept_hmsg = (SELECT ID FROM TB_DEPT WHERE TENANT_ID = 1 AND CO_ID = 1 AND DEPT_CD = 'HMSG');

-- USR_ID: SW0004(ggamgang)만 TB_USR id=1 연결. 나머지 NULL.
INSERT INTO TB_EMP (TENANT_ID, CO_ID, USR_ID, DEPT_ID, MGR_EMP_ID, EMP_NO, EMP_NM,
                    TEAM_ROLE_CD, JOB_TTL_CD, EML, EXP_ACC_CD, SEX_CD, TEL_NO, BIRTH_DT,
                    HIRE_DT, RETR_DT, STS_CD, CRT_BY, UPD_BY)
VALUES
    -- SWH (Head Office)
    (1, 1, NULL, @dept_swh,  NULL, 'SW0001', 'Seung Ho Choi',   'TEAM_MEMBER', 'CE', 'shchoi@sinwoo-itc.com',        80001, 'M',  NULL,             NULL,         '2012-08-24', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0002', 'Jieun Lee',       'TEAM_MEMBER', 'MG', 'jieun@sinwoo-itc.com',         80003, 'F',  NULL,             NULL,         '2015-03-09', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0003', 'Junserk Park',    'TEAM_MEMBER', 'DR', 'jamespark@sinwoo-itc.com',     80007, 'M',  NULL,             NULL,         '2019-05-01', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, 1,    @dept_swh,  NULL, 'SW0004', 'Juyong Lee',      'TEAM_MEMBER', 'DR', 'ggamgang@sinwoo-itc.com',      80008, 'M',  '017647991276',   '1977-05-22', '2020-02-01', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0005', 'Sang Kwang Nam',  'TEAM_MEMBER', 'MG', 'namsk@sinwoo-itc.com',         80002, 'M',  NULL,             NULL,         '2021-05-01', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0006', 'Changyong Jung',  'TEAM_MEMBER', 'MG', 'cyjung@sinwoo-itc.com',        80013, 'M',  NULL,             NULL,         '2021-06-15', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0021', 's.son',           'TEAM_MEMBER', 'MG', 's.son@sinwoo-itc.com',         80018, NULL, NULL,             NULL,         '2025-06-30', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_swh,  NULL, 'SW0022', 'FI_Admin',        'TEAM_MEMBER', 'IN', 'user_FI@sinwoo-itc.com',       80019, NULL, NULL,             NULL,         '2025-06-02', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    -- SEG (Samsung Electronics)
    (1, 1, NULL, @dept_seg,  NULL, 'SW0007', 'Sijin Lee',       'TEAM_MEMBER', 'MG', 'ryanjin@sinwoo-itc.com',       81007, 'M',  NULL,             NULL,         '2021-07-16', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_seg,  NULL, 'SW0008', 'Jung Woo Choi',   'TEAM_MEMBER', 'MG', 'jw.choi@sinwoo-itc.com',      80011, 'M',  NULL,             NULL,         '2021-09-13', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_seg,  NULL, 'SW0011', 'Chiho Yun',       'TEAM_MEMBER', 'MG', 'chelix@sinwoo-itc.com',        80010, 'M',  NULL,             NULL,         '2022-02-01', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_seg,  NULL, 'SW0017', 'Il Hyun Lee',     'TEAM_MEMBER', 'DR', 'ihlee1@sinwoo-itc.com',        81017, NULL, NULL,             NULL,         '2024-01-08', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    -- SSEG (Samsung Semiconductor)
    (1, 1, NULL, @dept_sseg, NULL, 'SW0010', 'Wonjun Choi',     'TEAM_MEMBER', 'MG', 'wjchoi@sinwoo-itc.com',       80014, 'M',  NULL,             NULL,         '2021-10-25', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_sseg, NULL, 'SW0012', 'Serae Kim',       'TEAM_MEMBER', 'MG', 'seraekim@sinwoo-itc.com',      80015, 'M',  NULL,             NULL,         '2022-08-25', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_sseg, NULL, 'SW0016', 'Sungwon Bang',    'TEAM_MEMBER', 'MG', 'sungwon.bang@sinwoo-itc.com',  80016, 'F',  NULL,             NULL,         '2023-11-29', NULL, 'ACTIVE', 'SYSTEM', 'SYSTEM'),
    -- QEG (Qenergy)
    (1, 1, NULL, @dept_qeg,  NULL, 'SW0013', 'Sang Soo Kim',   'TEAM_MEMBER', 'MG', 'deje123@sinwoo-itc.com',       81013, 'M',  NULL,             NULL,         '2023-02-14', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_qeg,  NULL, 'SW0014', 'Sungmin Park',   'TEAM_MEMBER', 'MG', 'smpark@sinwoo-itc.com',        81014, 'M',  NULL,             NULL,         '2023-04-17', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_qeg,  NULL, 'SW0018', 'Byungjin Choi',  'TEAM_MEMBER', 'MG', 'chrisholic@sinwoo-itc.com',    80017, NULL, NULL,             NULL,         '2023-12-21', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    (1, 1, NULL, @dept_qeg,  NULL, 'SW0019', 'So Young Jeon',  'TEAM_MEMBER', 'MG', 'sophia.jeon@sinwoo-itc.com',   81019, NULL, NULL,             NULL,         '2022-02-01', NULL, 'RESG',   'SYSTEM', 'SYSTEM'),
    -- QCL (QCell)
    (1, 1, NULL, @dept_qcl,  NULL, 'SW0020', 'Sung Young Oh',  'TEAM_MEMBER', 'MG', 'syoh@sinwoo-itc.com',          81020, NULL, NULL,             NULL,         '2024-04-01', NULL, 'RESG',   'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    EMP_NM       = VALUES(EMP_NM),
    DEPT_ID      = VALUES(DEPT_ID),
    USR_ID       = COALESCE(VALUES(USR_ID), USR_ID),
    JOB_TTL_CD   = VALUES(JOB_TTL_CD),
    EML          = VALUES(EML),
    EXP_ACC_CD   = VALUES(EXP_ACC_CD),
    SEX_CD       = VALUES(SEX_CD),
    TEL_NO       = VALUES(TEL_NO),
    BIRTH_DT     = VALUES(BIRTH_DT),
    HIRE_DT      = VALUES(HIRE_DT),
    RETR_DT      = VALUES(RETR_DT),
    STS_CD       = VALUES(STS_CD),
    UPD_BY       = VALUES(UPD_BY);

-- ============================================================
-- V16: 출퇴근 도메인 (Work Time + Holiday)
-- ============================================================

-- ------------------------------------------------------------
-- 1. TB_WORK_TIME  (출퇴근 기록 — 1인 1일 1건)
-- ------------------------------------------------------------
CREATE TABLE TB_WORK_TIME (
    ID          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'Work time primary key',
    TENANT_ID   BIGINT      NOT NULL COMMENT 'Tenant primary key reference',
    CO_ID       BIGINT      NOT NULL COMMENT 'Company primary key reference',
    EMP_ID      BIGINT      NOT NULL COMMENT 'Employee primary key reference',
    WORK_DT     DATE        NOT NULL COMMENT 'Work date',
    STR_TM      TIME        NULL     COMMENT 'Start time (clock-in)',
    END_TM      TIME        NULL     COMMENT 'End time (clock-out)',
    WORK_MIN    SMALLINT    NULL     COMMENT 'Net work minutes (calculated by service)',
    RMK         VARCHAR(500) NULL    COMMENT 'Remark',
    DEL_YN      CHAR(1)     NOT NULL DEFAULT 'N' COMMENT 'Delete yes or no',
    CRT_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Created by',
    CRT_DTM     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created datetime',
    UPD_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Updated by',
    UPD_DTM     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_WORK_TIME PRIMARY KEY (ID),
    CONSTRAINT UK_TB_WORK_TIME_TENANT_CO_EMP_DT UNIQUE (TENANT_ID, CO_ID, EMP_ID, WORK_DT),
    CONSTRAINT FK_TB_WORK_TIME_01 FOREIGN KEY (TENANT_ID) REFERENCES TB_TENANT (ID),
    CONSTRAINT FK_TB_WORK_TIME_02 FOREIGN KEY (CO_ID)     REFERENCES TB_CO (ID),
    CONSTRAINT FK_TB_WORK_TIME_03 FOREIGN KEY (EMP_ID)    REFERENCES TB_EMP (ID)
) COMMENT='Daily work time record';

CREATE INDEX IX_TB_WORK_TIME_TENANT_ID ON TB_WORK_TIME (TENANT_ID);
CREATE INDEX IX_TB_WORK_TIME_CO_ID     ON TB_WORK_TIME (CO_ID);
CREATE INDEX IX_TB_WORK_TIME_EMP_ID    ON TB_WORK_TIME (EMP_ID);
CREATE INDEX IX_TB_WORK_TIME_WORK_DT   ON TB_WORK_TIME (WORK_DT);

CREATE TABLE TB_WORK_TIME_HIST (
    HIST_ID     BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'History primary key',
    HIST_TP     CHAR(1)     NOT NULL COMMENT 'I,U,D',
    HIST_DTM    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'History datetime',
    HIST_BY     VARCHAR(100) NOT NULL COMMENT 'History actor',
    ID          BIGINT      NOT NULL COMMENT 'Work time primary key',
    TENANT_ID   BIGINT      NOT NULL COMMENT 'Tenant primary key reference',
    CO_ID       BIGINT      NOT NULL COMMENT 'Company primary key reference',
    EMP_ID      BIGINT      NOT NULL COMMENT 'Employee primary key reference',
    WORK_DT     DATE        NOT NULL COMMENT 'Work date',
    STR_TM      TIME        NULL     COMMENT 'Start time',
    END_TM      TIME        NULL     COMMENT 'End time',
    WORK_MIN    SMALLINT    NULL     COMMENT 'Net work minutes',
    RMK         VARCHAR(500) NULL    COMMENT 'Remark',
    DEL_YN      CHAR(1)     NOT NULL COMMENT 'Delete yes or no',
    CRT_BY      VARCHAR(100) NOT NULL COMMENT 'Created by',
    CRT_DTM     TIMESTAMP   NOT NULL COMMENT 'Created datetime',
    UPD_BY      VARCHAR(100) NOT NULL COMMENT 'Updated by',
    UPD_DTM     TIMESTAMP   NOT NULL COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_WORK_TIME_HIST PRIMARY KEY (HIST_ID)
) COMMENT='Daily work time history';

CREATE INDEX IX_TB_WORK_TIME_HIST_ID       ON TB_WORK_TIME_HIST (ID);
CREATE INDEX IX_TB_WORK_TIME_HIST_HIST_DTM ON TB_WORK_TIME_HIST (HIST_DTM);

CREATE TRIGGER TR_TB_WORK_TIME_AI
AFTER INSERT ON TB_WORK_TIME
FOR EACH ROW
INSERT INTO TB_WORK_TIME_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, EMP_ID, WORK_DT, STR_TM, END_TM, WORK_MIN, RMK, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'I', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.EMP_ID, NEW.WORK_DT, NEW.STR_TM, NEW.END_TM, NEW.WORK_MIN, NEW.RMK, NEW.DEL_YN, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_WORK_TIME_AU
AFTER UPDATE ON TB_WORK_TIME
FOR EACH ROW
INSERT INTO TB_WORK_TIME_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, EMP_ID, WORK_DT, STR_TM, END_TM, WORK_MIN, RMK, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'U', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.EMP_ID, NEW.WORK_DT, NEW.STR_TM, NEW.END_TM, NEW.WORK_MIN, NEW.RMK, NEW.DEL_YN, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_WORK_TIME_BD
BEFORE DELETE ON TB_WORK_TIME
FOR EACH ROW
INSERT INTO TB_WORK_TIME_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, EMP_ID, WORK_DT, STR_TM, END_TM, WORK_MIN, RMK, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'D', COALESCE(OLD.UPD_BY, OLD.CRT_BY, 'SYSTEM'), OLD.ID, OLD.TENANT_ID, OLD.CO_ID, OLD.EMP_ID, OLD.WORK_DT, OLD.STR_TM, OLD.END_TM, OLD.WORK_MIN, OLD.RMK, OLD.DEL_YN, OLD.CRT_BY, OLD.CRT_DTM, OLD.UPD_BY, OLD.UPD_DTM
);

-- ------------------------------------------------------------
-- 2. TB_REGION_HOLIDAY  (지역 공휴일 — 오픈 API 수집, 테넌트 독립)
-- ------------------------------------------------------------
CREATE TABLE TB_REGION_HOLIDAY (
    ID          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Region holiday primary key',
    YR          SMALLINT     NOT NULL COMMENT 'Year',
    REGION_CD   VARCHAR(20)  NOT NULL COMMENT 'Region code (ALL, NW, BY, HE...)',
    REGION_NM   VARCHAR(100) NULL     COMMENT 'Region name',
    HOLIDAY_DT  DATE         NOT NULL COMMENT 'Holiday date',
    HOLIDAY_NM  VARCHAR(255) NOT NULL COMMENT 'Holiday name',
    WKND_YN     CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'Falls on weekend yes or no',
    CRT_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Created by',
    CRT_DTM     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created datetime',
    UPD_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Updated by',
    UPD_DTM     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_REGION_HOLIDAY PRIMARY KEY (ID),
    CONSTRAINT UK_TB_REGION_HOLIDAY_YR_RGN_DT UNIQUE (YR, REGION_CD, HOLIDAY_DT)
) COMMENT='Regional public holiday (auto-collected from open API)';

CREATE INDEX IX_TB_REGION_HOLIDAY_YR        ON TB_REGION_HOLIDAY (YR);
CREATE INDEX IX_TB_REGION_HOLIDAY_REGION_CD ON TB_REGION_HOLIDAY (REGION_CD);
CREATE INDEX IX_TB_REGION_HOLIDAY_DT        ON TB_REGION_HOLIDAY (HOLIDAY_DT);

-- ------------------------------------------------------------
-- 3. TB_CO_HOLIDAY  (회사 자체 휴일 — customer admin 관리)
-- ------------------------------------------------------------
CREATE TABLE TB_CO_HOLIDAY (
    ID          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Company holiday primary key',
    TENANT_ID   BIGINT       NOT NULL COMMENT 'Tenant primary key reference',
    CO_ID       BIGINT       NOT NULL COMMENT 'Company primary key reference',
    HOLIDAY_NM  VARCHAR(255) NOT NULL COMMENT 'Holiday name',
    STR_DT      DATE         NOT NULL COMMENT 'Start date',
    END_DT      DATE         NOT NULL COMMENT 'End date',
    ANNUAL_YN   CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'Annual recurring yes or no',
    APPLY_YR    SMALLINT     NOT NULL DEFAULT 0 COMMENT 'Apply year (0 if annual)',
    DEL_YN      CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'Delete yes or no',
    CRT_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Created by',
    CRT_DTM     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created datetime',
    UPD_BY      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT 'Updated by',
    UPD_DTM     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_CO_HOLIDAY PRIMARY KEY (ID),
    CONSTRAINT FK_TB_CO_HOLIDAY_01 FOREIGN KEY (TENANT_ID) REFERENCES TB_TENANT (ID),
    CONSTRAINT FK_TB_CO_HOLIDAY_02 FOREIGN KEY (CO_ID)     REFERENCES TB_CO (ID)
) COMMENT='Company holiday (managed by customer admin)';

CREATE INDEX IX_TB_CO_HOLIDAY_TENANT_ID ON TB_CO_HOLIDAY (TENANT_ID);
CREATE INDEX IX_TB_CO_HOLIDAY_CO_ID     ON TB_CO_HOLIDAY (CO_ID);
CREATE INDEX IX_TB_CO_HOLIDAY_STR_DT    ON TB_CO_HOLIDAY (STR_DT);
CREATE INDEX IX_TB_CO_HOLIDAY_END_DT    ON TB_CO_HOLIDAY (END_DT);

CREATE TABLE TB_CO_HOLIDAY_HIST (
    HIST_ID     BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'History primary key',
    HIST_TP     CHAR(1)      NOT NULL COMMENT 'I,U,D',
    HIST_DTM    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'History datetime',
    HIST_BY     VARCHAR(100) NOT NULL COMMENT 'History actor',
    ID          BIGINT       NOT NULL COMMENT 'Company holiday primary key',
    TENANT_ID   BIGINT       NOT NULL COMMENT 'Tenant primary key reference',
    CO_ID       BIGINT       NOT NULL COMMENT 'Company primary key reference',
    HOLIDAY_NM  VARCHAR(255) NOT NULL COMMENT 'Holiday name',
    STR_DT      DATE         NOT NULL COMMENT 'Start date',
    END_DT      DATE         NOT NULL COMMENT 'End date',
    ANNUAL_YN   CHAR(1)      NOT NULL COMMENT 'Annual recurring yes or no',
    APPLY_YR    SMALLINT     NOT NULL COMMENT 'Apply year',
    DEL_YN      CHAR(1)      NOT NULL COMMENT 'Delete yes or no',
    CRT_BY      VARCHAR(100) NOT NULL COMMENT 'Created by',
    CRT_DTM     TIMESTAMP    NOT NULL COMMENT 'Created datetime',
    UPD_BY      VARCHAR(100) NOT NULL COMMENT 'Updated by',
    UPD_DTM     TIMESTAMP    NOT NULL COMMENT 'Updated datetime',
    CONSTRAINT PK_TB_CO_HOLIDAY_HIST PRIMARY KEY (HIST_ID)
) COMMENT='Company holiday history';

CREATE INDEX IX_TB_CO_HOLIDAY_HIST_ID       ON TB_CO_HOLIDAY_HIST (ID);
CREATE INDEX IX_TB_CO_HOLIDAY_HIST_HIST_DTM ON TB_CO_HOLIDAY_HIST (HIST_DTM);

CREATE TRIGGER TR_TB_CO_HOLIDAY_AI
AFTER INSERT ON TB_CO_HOLIDAY
FOR EACH ROW
INSERT INTO TB_CO_HOLIDAY_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, HOLIDAY_NM, STR_DT, END_DT, ANNUAL_YN, APPLY_YR, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'I', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.HOLIDAY_NM, NEW.STR_DT, NEW.END_DT, NEW.ANNUAL_YN, NEW.APPLY_YR, NEW.DEL_YN, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_CO_HOLIDAY_AU
AFTER UPDATE ON TB_CO_HOLIDAY
FOR EACH ROW
INSERT INTO TB_CO_HOLIDAY_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, HOLIDAY_NM, STR_DT, END_DT, ANNUAL_YN, APPLY_YR, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'U', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.HOLIDAY_NM, NEW.STR_DT, NEW.END_DT, NEW.ANNUAL_YN, NEW.APPLY_YR, NEW.DEL_YN, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_CO_HOLIDAY_BD
BEFORE DELETE ON TB_CO_HOLIDAY
FOR EACH ROW
INSERT INTO TB_CO_HOLIDAY_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, HOLIDAY_NM, STR_DT, END_DT, ANNUAL_YN, APPLY_YR, DEL_YN, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'D', COALESCE(OLD.UPD_BY, OLD.CRT_BY, 'SYSTEM'), OLD.ID, OLD.TENANT_ID, OLD.CO_ID, OLD.HOLIDAY_NM, OLD.STR_DT, OLD.END_DT, OLD.ANNUAL_YN, OLD.APPLY_YR, OLD.DEL_YN, OLD.CRT_BY, OLD.CRT_DTM, OLD.UPD_BY, OLD.UPD_DTM
);

-- ------------------------------------------------------------
-- 4. TB_CO ALTER — 점심시간 설정 추가
-- ------------------------------------------------------------
ALTER TABLE TB_CO
    ADD COLUMN LUNCH_STR_TM TIME NULL COMMENT 'Lunch start time' AFTER STS_CD,
    ADD COLUMN LUNCH_END_TM TIME NULL COMMENT 'Lunch end time' AFTER LUNCH_STR_TM;

ALTER TABLE TB_CO_HIST
    ADD COLUMN LUNCH_STR_TM TIME NULL COMMENT 'Lunch start time' AFTER STS_CD,
    ADD COLUMN LUNCH_END_TM TIME NULL COMMENT 'Lunch end time' AFTER LUNCH_STR_TM;

DROP TRIGGER IF EXISTS TR_TB_CO_AI;
DROP TRIGGER IF EXISTS TR_TB_CO_AU;
DROP TRIGGER IF EXISTS TR_TB_CO_BD;

CREATE TRIGGER TR_TB_CO_AI
AFTER INSERT ON TB_CO
FOR EACH ROW
INSERT INTO TB_CO_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_CD, CO_NM, REG_NO, STS_CD, LUNCH_STR_TM, LUNCH_END_TM, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'I', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_CD, NEW.CO_NM, NEW.REG_NO, NEW.STS_CD, NEW.LUNCH_STR_TM, NEW.LUNCH_END_TM, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_CO_AU
AFTER UPDATE ON TB_CO
FOR EACH ROW
INSERT INTO TB_CO_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_CD, CO_NM, REG_NO, STS_CD, LUNCH_STR_TM, LUNCH_END_TM, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'U', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_CD, NEW.CO_NM, NEW.REG_NO, NEW.STS_CD, NEW.LUNCH_STR_TM, NEW.LUNCH_END_TM, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_CO_BD
BEFORE DELETE ON TB_CO
FOR EACH ROW
INSERT INTO TB_CO_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_CD, CO_NM, REG_NO, STS_CD, LUNCH_STR_TM, LUNCH_END_TM, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'D', COALESCE(OLD.UPD_BY, OLD.CRT_BY, 'SYSTEM'), OLD.ID, OLD.TENANT_ID, OLD.CO_CD, OLD.CO_NM, OLD.REG_NO, OLD.STS_CD, OLD.LUNCH_STR_TM, OLD.LUNCH_END_TM, OLD.CRT_BY, OLD.CRT_DTM, OLD.UPD_BY, OLD.UPD_DTM
);

-- ------------------------------------------------------------
-- 5. TB_DEPT ALTER — REGION_CD 추가 (공휴일 매핑용)
-- ------------------------------------------------------------
ALTER TABLE TB_DEPT
    ADD COLUMN REGION_CD VARCHAR(20) NULL COMMENT 'Work region code for holiday mapping (NW, BY, HE...)' AFTER STS_CD;

ALTER TABLE TB_DEPT_HIST
    ADD COLUMN REGION_CD VARCHAR(20) NULL COMMENT 'Work region code' AFTER STS_CD;

DROP TRIGGER IF EXISTS TR_TB_DEPT_AI;
DROP TRIGGER IF EXISTS TR_TB_DEPT_AU;
DROP TRIGGER IF EXISTS TR_TB_DEPT_BD;

CREATE TRIGGER TR_TB_DEPT_AI
AFTER INSERT ON TB_DEPT
FOR EACH ROW
INSERT INTO TB_DEPT_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, DEPT_CD, DEPT_NM, UP_DEPT_ID, DEPT_LVL_NO, STS_CD, REGION_CD, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'I', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.DEPT_CD, NEW.DEPT_NM, NEW.UP_DEPT_ID, NEW.DEPT_LVL_NO, NEW.STS_CD, NEW.REGION_CD, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_DEPT_AU
AFTER UPDATE ON TB_DEPT
FOR EACH ROW
INSERT INTO TB_DEPT_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, DEPT_CD, DEPT_NM, UP_DEPT_ID, DEPT_LVL_NO, STS_CD, REGION_CD, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'U', COALESCE(NEW.UPD_BY, NEW.CRT_BY, 'SYSTEM'), NEW.ID, NEW.TENANT_ID, NEW.CO_ID, NEW.DEPT_CD, NEW.DEPT_NM, NEW.UP_DEPT_ID, NEW.DEPT_LVL_NO, NEW.STS_CD, NEW.REGION_CD, NEW.CRT_BY, NEW.CRT_DTM, NEW.UPD_BY, NEW.UPD_DTM
);

CREATE TRIGGER TR_TB_DEPT_BD
BEFORE DELETE ON TB_DEPT
FOR EACH ROW
INSERT INTO TB_DEPT_HIST (
    HIST_TP, HIST_BY, ID, TENANT_ID, CO_ID, DEPT_CD, DEPT_NM, UP_DEPT_ID, DEPT_LVL_NO, STS_CD, REGION_CD, CRT_BY, CRT_DTM, UPD_BY, UPD_DTM
) VALUES (
    'D', COALESCE(OLD.UPD_BY, OLD.CRT_BY, 'SYSTEM'), OLD.ID, OLD.TENANT_ID, OLD.CO_ID, OLD.DEPT_CD, OLD.DEPT_NM, OLD.UP_DEPT_ID, OLD.DEPT_LVL_NO, OLD.STS_CD, OLD.REGION_CD, OLD.CRT_BY, OLD.CRT_DTM, OLD.UPD_BY, OLD.UPD_DTM
);

-- ------------------------------------------------------------
-- 6. 시드: 기존 부서에 REGION_CD 세팅 (NW = 노르트라인-베스트팔렌)
-- ------------------------------------------------------------
UPDATE TB_DEPT SET REGION_CD = 'NW' WHERE ID = 1;

-- ------------------------------------------------------------
-- 7. 공통코드 시드 — 근무상태
-- ------------------------------------------------------------
INSERT INTO TB_CD_GRP (
    GRP_CD, GRP_NM_KO, GRP_NM_EN, GRP_NM_DE, SYS_YN, USE_YN, DSP_ORD, CRT_BY, UPD_BY
) VALUES (
    'WK_STS', '근무상태', 'Work Status', 'Arbeitsstatus', 'Y', 'Y', 20, 'SYSTEM', 'SYSTEM'
);

SET @WK_STS_GRP_ID = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'WK_STS' LIMIT 1);

INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, USE_YN, DSP_ORD, CRT_BY, UPD_BY) VALUES
(@WK_STS_GRP_ID, 'NM', '정상',  'Normal',      'Normal',       'Y', 1, 'SYSTEM', 'SYSTEM'),
(@WK_STS_GRP_ID, 'LT', '지각',  'Late',        'Verspätung',   'Y', 2, 'SYSTEM', 'SYSTEM'),
(@WK_STS_GRP_ID, 'EL', '조퇴',  'Early Leave', 'Frühzeitig',   'Y', 3, 'SYSTEM', 'SYSTEM'),
(@WK_STS_GRP_ID, 'AB', '결근',  'Absent',      'Abwesend',     'Y', 4, 'SYSTEM', 'SYSTEM');

-- ------------------------------------------------------------
-- 8. 공통코드 시드 — 독일 지역코드
-- ------------------------------------------------------------
INSERT INTO TB_CD_GRP (
    GRP_CD, GRP_NM_KO, GRP_NM_EN, GRP_NM_DE, SYS_YN, USE_YN, DSP_ORD, CRT_BY, UPD_BY
) VALUES (
    'DE_RGN', '독일 지역', 'Germany Region', 'Bundesland', 'Y', 'Y', 21, 'SYSTEM', 'SYSTEM'
);

SET @DE_RGN_GRP_ID = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'DE_RGN' LIMIT 1);

INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, USE_YN, DSP_ORD, CRT_BY, UPD_BY) VALUES
(@DE_RGN_GRP_ID, 'BW', '바덴-뷔르템베르크',       'Baden-Württemberg',        'Baden-Württemberg',        'Y',  1, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'BY', '바이에른',                 'Bavaria',                  'Bayern',                   'Y',  2, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'BE', '베를린',                   'Berlin',                   'Berlin',                   'Y',  3, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'BB', '브란덴부르크',             'Brandenburg',              'Brandenburg',              'Y',  4, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'HB', '브레멘',                   'Bremen',                   'Bremen',                   'Y',  5, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'HH', '함부르크',                 'Hamburg',                  'Hamburg',                  'Y',  6, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'HE', '헤센',                     'Hesse',                    'Hessen',                   'Y',  7, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'MV', '메클렌부르크-포어포메른',   'Mecklenburg-Vorpommern',   'Mecklenburg-Vorpommern',   'Y',  8, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'NI', '니더작센',                 'Lower Saxony',             'Niedersachsen',            'Y',  9, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'NW', '노르트라인-베스트팔렌',     'North Rhine-Westphalia',   'Nordrhein-Westfalen',      'Y', 10, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'RP', '라인란트-팔츠',            'Rhineland-Palatinate',     'Rheinland-Pfalz',          'Y', 11, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'SL', '자를란트',                 'Saarland',                 'Saarland',                 'Y', 12, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'SN', '작센',                     'Saxony',                   'Sachsen',                  'Y', 13, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'ST', '작센-안할트',              'Saxony-Anhalt',            'Sachsen-Anhalt',           'Y', 14, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'SH', '슐레스비히-홀슈타인',      'Schleswig-Holstein',       'Schleswig-Holstein',       'Y', 15, 'SYSTEM', 'SYSTEM'),
(@DE_RGN_GRP_ID, 'TH', '튀링엔',                   'Thuringia',                'Thüringen',                'Y', 16, 'SYSTEM', 'SYSTEM');

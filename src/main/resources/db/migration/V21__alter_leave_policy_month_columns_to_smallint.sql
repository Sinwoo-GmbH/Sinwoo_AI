-- ============================================================
-- V21: TB_LEAVE_CO_POLICY 월/일 컬럼 TINYINT → SMALLINT
-- ============================================================
-- 사유: Entity 필드 타입이 Short (= SMALLINT)이라
--       Hibernate schema validation에서 TINYINT와 불일치 에러.
-- 영향: TB_LEAVE_CO_POLICY, TB_LEAVE_CO_POLICY_HIST 양쪽 동시 변경.
-- ============================================================

ALTER TABLE TB_LEAVE_CO_POLICY
    MODIFY FISCAL_START_MM     SMALLINT NOT NULL DEFAULT 1,
    MODIFY CARRYOVER_EXPIRE_MM SMALLINT NOT NULL DEFAULT 3,
    MODIFY CARRYOVER_EXPIRE_DD SMALLINT NOT NULL DEFAULT 31;

ALTER TABLE TB_LEAVE_CO_POLICY_HIST
    MODIFY FISCAL_START_MM     SMALLINT NOT NULL,
    MODIFY CARRYOVER_EXPIRE_MM SMALLINT NOT NULL,
    MODIFY CARRYOVER_EXPIRE_DD SMALLINT NOT NULL;

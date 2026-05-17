-- ============================================================
-- V23: 모든 *_CD 값 간소화 (2~4자 영문대문자, 언더바 제거) +
--      CUSTOMER 메뉴 트리 재구성
-- ============================================================
-- 규칙: CLAUDE.md "공통코드" 섹션 참조
--   - 모든 *_CD 값 2~4자, 언더바 없음
--   - 충돌 시 최대 6자 예외 (예: ADMIN)
--   - 같은 의미는 공유 가능 (PLT/CST/ADM 등 SCOPE 공통)
-- ============================================================

-- ============================================================
-- PART 1. 공통 CD 값 일괄 단축 (기존 데이터 UPDATE)
-- ============================================================

-- ── 1-1. ROLE 관련 CD ────────────────────────────────────────
UPDATE TB_ROLE SET ROLE_SCOPE_CD = 'PLT' WHERE ROLE_SCOPE_CD = 'PLATFORM';
UPDATE TB_ROLE SET ROLE_SCOPE_CD = 'CST' WHERE ROLE_SCOPE_CD = 'CUSTOMER';
UPDATE TB_ROLE SET ROLE_SCOPE_CD = 'ADM' WHERE ROLE_SCOPE_CD = 'ADMIN';

UPDATE TB_ROLE SET ROLE_D1_CD = 'PLT' WHERE ROLE_D1_CD = 'PLATFORM';
UPDATE TB_ROLE SET ROLE_D1_CD = 'CST' WHERE ROLE_D1_CD = 'CUSTOMER';

UPDATE TB_ROLE SET ROLE_D2_CD = 'ADM' WHERE ROLE_D2_CD = 'ADMIN';
UPDATE TB_ROLE SET ROLE_D2_CD = 'USR' WHERE ROLE_D2_CD = 'USER';
-- ROLE_D2_CD: HR, FI 는 이미 짧음 (변경 없음)

UPDATE TB_ROLE SET ROLE_GRP_CD = 'PLT' WHERE ROLE_GRP_CD = 'PLATFORM';
UPDATE TB_ROLE SET ROLE_GRP_CD = 'CST' WHERE ROLE_GRP_CD = 'CUSTOMER';

-- ── 1-2. MNU_SCOPE_CD ────────────────────────────────────────
UPDATE TB_MNU SET MNU_SCOPE_CD = 'PLT' WHERE MNU_SCOPE_CD = 'PLATFORM';
UPDATE TB_MNU SET MNU_SCOPE_CD = 'CST' WHERE MNU_SCOPE_CD = 'CUSTOMER';
UPDATE TB_MNU SET MNU_SCOPE_CD = 'ADM' WHERE MNU_SCOPE_CD = 'ADMIN';

-- ── 1-3. BILL_GATE_CD ────────────────────────────────────────
UPDATE TB_MNU SET BILL_GATE_CD = 'PCST' WHERE BILL_GATE_CD = 'PAID_CUSTOMER_ADMIN';

-- ── 1-4. TENANT_TP_CD ────────────────────────────────────────
UPDATE TB_TENANT SET TENANT_TP_CD = 'INTL' WHERE TENANT_TP_CD = 'INTERNAL';
UPDATE TB_TENANT SET TENANT_TP_CD = 'CST'  WHERE TENANT_TP_CD = 'CUSTOMER';

-- ── 1-5. REQ_TP_CD (결재선 도메인 구분) ──────────────────────
UPDATE TB_APRV_LINE SET REQ_TP_CD = 'LV' WHERE REQ_TP_CD = 'LEAVE';
UPDATE TB_APRV_LINE SET REQ_TP_CD = 'TR' WHERE REQ_TP_CD = 'TRIP';
UPDATE TB_APRV_LINE SET REQ_TP_CD = 'EX' WHERE REQ_TP_CD = 'EXPENSE';

-- ── 1-6. LEAVE 정책 CD ───────────────────────────────────────
UPDATE TB_LEAVE_CO_POLICY SET GRANT_TIMING_CD  = 'FSC'  WHERE GRANT_TIMING_CD  = 'FISCAL';
UPDATE TB_LEAVE_CO_POLICY SET GRANT_TIMING_CD  = 'HIR'  WHERE GRANT_TIMING_CD  = 'HIRE';
UPDATE TB_LEAVE_CO_POLICY SET GRANT_TYPE_CD    = 'FLT'  WHERE GRANT_TYPE_CD    = 'FLAT';
UPDATE TB_LEAVE_CO_POLICY SET GRANT_TYPE_CD    = 'TRD'  WHERE GRANT_TYPE_CD    = 'TIERED';
UPDATE TB_LEAVE_CO_POLICY SET EXPIRE_ACTION_CD = 'ZERO' WHERE EXPIRE_ACTION_CD = 'AUTO_ZERO';
UPDATE TB_LEAVE_CO_POLICY SET EXPIRE_ACTION_CD = 'CARY' WHERE EXPIRE_ACTION_CD = 'CARRY';

UPDATE TB_LEAVE_CO_POLICY_HIST SET GRANT_TIMING_CD  = 'FSC'  WHERE GRANT_TIMING_CD  = 'FISCAL';
UPDATE TB_LEAVE_CO_POLICY_HIST SET GRANT_TIMING_CD  = 'HIR'  WHERE GRANT_TIMING_CD  = 'HIRE';
UPDATE TB_LEAVE_CO_POLICY_HIST SET GRANT_TYPE_CD    = 'FLT'  WHERE GRANT_TYPE_CD    = 'FLAT';
UPDATE TB_LEAVE_CO_POLICY_HIST SET GRANT_TYPE_CD    = 'TRD'  WHERE GRANT_TYPE_CD    = 'TIERED';
UPDATE TB_LEAVE_CO_POLICY_HIST SET EXPIRE_ACTION_CD = 'ZERO' WHERE EXPIRE_ACTION_CD = 'AUTO_ZERO';
UPDATE TB_LEAVE_CO_POLICY_HIST SET EXPIRE_ACTION_CD = 'CARY' WHERE EXPIRE_ACTION_CD = 'CARRY';

-- ============================================================
-- PART 2. ADMIN scope 메뉴 CD 단축 (기존 메뉴 유지, CD만 변경)
-- ============================================================
-- TB_CD (i18n 번역) 의 CD 값도 같이 업데이트 (FK 아니지만 lookup key)

UPDATE TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
   SET c.CD = 'ADSH'  WHERE c.CD = 'MNU_ADMIN_DASH';
UPDATE TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
   SET c.CD = 'TNT'   WHERE c.CD = 'MNU_ADMIN_TENANT';
UPDATE TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
   SET c.CD = 'AUTH'  WHERE c.CD = 'MNU_ADMIN_AUTH';
UPDATE TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
   SET c.CD = 'BILL'  WHERE c.CD = 'MNU_ADMIN_BILL';
UPDATE TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
   SET c.CD = 'MENU'  WHERE c.CD = 'MNU_ADMIN_MENU';

UPDATE TB_MNU SET MNU_CD = 'ADSH', MNU_NM_CD = 'MNU.ADM.DSH'  WHERE MNU_CD = 'MNU_ADMIN_DASH';
UPDATE TB_MNU SET MNU_CD = 'TNT',  MNU_NM_CD = 'MNU.ADM.TNT'  WHERE MNU_CD = 'MNU_ADMIN_TENANT';
UPDATE TB_MNU SET MNU_CD = 'AUTH', MNU_NM_CD = 'MNU.ADM.AUTH' WHERE MNU_CD = 'MNU_ADMIN_AUTH';
UPDATE TB_MNU SET MNU_CD = 'BILL', MNU_NM_CD = 'MNU.ADM.BILL' WHERE MNU_CD = 'MNU_ADMIN_BILL';
UPDATE TB_MNU SET MNU_CD = 'MENU', MNU_NM_CD = 'MNU.ADM.MENU' WHERE MNU_CD = 'MNU_ADMIN_MENU';

-- ============================================================
-- PART 3. CUSTOMER scope 메뉴 트리 전면 재구성
-- ============================================================
-- 기존 CUSTOMER 메뉴 전부 삭제 후 새 트리 INSERT
-- (Admin 트리 = MNU_CUSTOMER_ADMIN + 3 children 도 함께 재INSERT)

-- ── 3-1. 기존 CUSTOMER scope 권한 매핑 및 번역 정리 ─────────
DELETE a FROM TB_ROLE_MNU_AUTH a
  JOIN TB_MNU m ON m.ID = a.MNU_ID
 WHERE m.MNU_SCOPE_CD = 'CST';

DELETE c FROM TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
 WHERE c.CD LIKE 'MNU_CUSTOMER_%';

-- 자식 → 부모 순으로 UP_MNU_ID NULL 처리 (FK 자기참조 회피)
UPDATE TB_MNU SET UP_MNU_ID = NULL WHERE MNU_SCOPE_CD = 'CST';

DELETE FROM TB_MNU WHERE MNU_SCOPE_CD = 'CST';

-- ── 3-2. 새 CUSTOMER 메뉴 트리 INSERT ────────────────────────
-- Top-level
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('DASH',  'MNU.CST.DASH',  'Dashboard',     'CST', NULL, '/customer/dashboard',     'layout-dashboard', 10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('WT',    'MNU.CST.WT',    'Working Time',  'CST', NULL, NULL,                       'clock',            20, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RQST',  'MNU.CST.RQST',  'Request',       'CST', NULL, NULL,                       'file-text',        30, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('CLMS',  'MNU.CST.CLMS',  'My Claims',     'CST', NULL, NULL,                       'inbox',            40, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RPRT',  'MNU.CST.RPRT',  'Reports',       'CST', NULL, NULL,                       'bar-chart',        50, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('FNC',   'MNU.CST.FNC',   'Finance',       'CST', NULL, '/customer/finance',        'wallet',           60, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('ADMIN', 'MNU.CST.ADMIN', 'Admin',         'CST', NULL, NULL,                       'shield',           70, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- 부모 ID 캡처
SET @WT_ID    := (SELECT ID FROM TB_MNU WHERE MNU_CD = 'WT'    AND MNU_SCOPE_CD = 'CST');
SET @RQST_ID  := (SELECT ID FROM TB_MNU WHERE MNU_CD = 'RQST'  AND MNU_SCOPE_CD = 'CST');
SET @CLMS_ID  := (SELECT ID FROM TB_MNU WHERE MNU_CD = 'CLMS'  AND MNU_SCOPE_CD = 'CST');
SET @RPRT_ID  := (SELECT ID FROM TB_MNU WHERE MNU_CD = 'RPRT'  AND MNU_SCOPE_CD = 'CST');
SET @ADMIN_ID := (SELECT ID FROM TB_MNU WHERE MNU_CD = 'ADMIN' AND MNU_SCOPE_CD = 'CST');

-- Working Time children
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('MYWT', 'MNU.CST.WT.MY',   'My Working Time',   'CST', @WT_ID, '/customer/working-time/my',   'user-clock', 10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('TMWT', 'MNU.CST.WT.TEAM', 'Team Working Time', 'CST', @WT_ID, '/customer/working-time/team', 'users',      20, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- Request children
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('RQLV', 'MNU.CST.RQST.LV', 'Leave',         'CST', @RQST_ID, '/customer/requests/leave',         'calendar-check', 10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RQTR', 'MNU.CST.RQST.TR', 'Business Trip', 'CST', @RQST_ID, '/customer/requests/business-trip', 'plane',          20, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RQEX', 'MNU.CST.RQST.EX', 'Expense',       'CST', @RQST_ID, '/customer/requests/expense',       'receipt',        30, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RQIN', 'MNU.CST.RQST.IN', 'Inbox',         'CST', @RQST_ID, '/customer/requests/inbox',         'mail-check',     40, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- My Claims children
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('CMEX', 'MNU.CST.CLMS.EX', 'Expense',       'CST', @CLMS_ID, '/customer/my-claims/expense',       'receipt',    10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('CMTR', 'MNU.CST.CLMS.TR', 'Business Trip', 'CST', @CLMS_ID, '/customer/my-claims/business-trip', 'plane',      20, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('CMIN', 'MNU.CST.CLMS.IN', 'Inbox',         'CST', @CLMS_ID, '/customer/my-claims/inbox',         'mail-check', 30, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- Reports children
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('RPWT', 'MNU.CST.RPRT.WT', 'Work Time',         'CST', @RPRT_ID, '/customer/reports/work-time',         'clock',       10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('RPEX', 'MNU.CST.RPRT.EX', 'Monthly Expenses',  'CST', @RPRT_ID, '/customer/reports/monthly-expenses',  'trending-up', 20, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- Admin children (기존 Customer Admin 트리 유지)
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, BILL_GATE_CD, CRT_BY, UPD_BY) VALUES
  ('LVPL', 'MNU.CST.ADMIN.LVPL', 'Leave Policy',     'CST', @ADMIN_ID, '/customer/admin/leave-policy', 'settings', 10, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('LVGR', 'MNU.CST.ADMIN.LVGR', 'Leave Grants',     'CST', @ADMIN_ID, '/customer/admin/leave-grants', 'list',     20, 'Y', NULL, 'SYSTEM', 'SYSTEM'),
  ('COHL', 'MNU.CST.ADMIN.COHL', 'Company Holidays', 'CST', @ADMIN_ID, '/customer/admin/holidays',     'calendar', 30, 'Y', NULL, 'SYSTEM', 'SYSTEM');

-- ── 3-3. TB_CD (MNU_NM 그룹) 번역 시드 ───────────────────────
SET @MNU_GRP_ID := (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'MNU_NM' LIMIT 1);

INSERT INTO TB_CD (GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE, USE_YN, DSP_ORD, CRT_BY, UPD_BY) VALUES
  (@MNU_GRP_ID, 'DASH',  '대시보드',          'Dashboard',         'Dashboard',          'Y', 10, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'WT',    '근무시간',          'Working Time',      'Arbeitszeit',        'Y', 20, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'MYWT',  '내 근무시간',       'My Working Time',   'Meine Arbeitszeit',  'Y', 21, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'TMWT',  '팀 근무시간',       'Team Working Time', 'Team-Arbeitszeit',   'Y', 22, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RQST',  '신청',              'Request',           'Anträge',            'Y', 30, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RQLV',  '휴가 신청',         'Leave',             'Urlaub',             'Y', 31, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RQTR',  '출장 신청',         'Business Trip',     'Geschäftsreise',     'Y', 32, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RQEX',  '경비 신청',         'Expense',           'Spesen',             'Y', 33, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RQIN',  '결재함',            'Inbox',             'Eingang',            'Y', 34, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'CLMS',  '내 신청 내역',      'My Claims',         'Meine Anträge',      'Y', 40, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'CMEX',  '경비 내역',         'Expense',           'Spesen',             'Y', 41, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'CMTR',  '출장 내역',         'Business Trip',     'Geschäftsreise',     'Y', 42, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'CMIN',  '결재 진행 내역',    'Inbox',             'Eingang',            'Y', 43, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RPRT',  '리포트',            'Reports',           'Berichte',           'Y', 50, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RPWT',  '근무시간 리포트',   'Work Time',         'Arbeitszeit',        'Y', 51, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'RPEX',  '월별 경비 리포트',  'Monthly Expenses',  'Monatliche Spesen',  'Y', 52, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'FNC',   '재무',              'Finance',           'Finanzen',           'Y', 60, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'ADMIN', '관리자',            'Admin',             'Verwaltung',         'Y', 70, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'LVPL',  '휴가 정책',         'Leave Policy',      'Urlaubsregelung',    'Y', 71, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'LVGR',  '휴가 부여',         'Leave Grants',      'Urlaubsvergabe',     'Y', 72, 'SYSTEM', 'SYSTEM'),
  (@MNU_GRP_ID, 'COHL',  '회사 휴일',         'Company Holidays',  'Betriebsferien',     'Y', 73, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
  CD_NM_KO = VALUES(CD_NM_KO),
  CD_NM_EN = VALUES(CD_NM_EN),
  CD_NM_DE = VALUES(CD_NM_DE),
  USE_YN   = VALUES(USE_YN),
  DSP_ORD  = VALUES(DSP_ORD),
  UPD_BY   = 'SYSTEM';

-- ============================================================
-- PART 4. TB_ROLE_MNU_AUTH 매핑 (새 CUSTOMER 트리)
-- ============================================================
-- 권한 정책:
--   PADM : 전부 (Platform Admin = ALL)
--   CADM : 전부 (Customer Admin)
--   HR   : ADMIN 트리 제외한 일반 메뉴 + Admin 트리(Leave Policy만 접근)
--          → 단순화: HR 도 ADMIN 트리 전체 제외 (요구사항: Admin = Customer Admin↑)
--   FI   : ADMIN 트리 제외 + Finance 포함
--   USR  : ADMIN 트리 제외 + Finance 제외 + Team Working Time 제외
--   Team Working Time : CADM, PADM 만 (팀장급은 추후 직원정보 직급으로 별도 처리)

-- 4-1) PADM: 모든 CUSTOMER 메뉴
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'PADM'),
       m.ID, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
  FROM TB_MNU m WHERE m.MNU_SCOPE_CD = 'CST';

-- 4-2) CADM: 모든 CUSTOMER 메뉴
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'CADM'),
       m.ID, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
  FROM TB_MNU m WHERE m.MNU_SCOPE_CD = 'CST';

-- 4-3) HR: ADMIN 트리(ADMIN, LVPL, LVGR, COHL) + Team Working Time 제외
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'HR'),
       m.ID, 'Y', 'Y', 'Y', 'N', 'N', 'Y', 'SYSTEM', 'SYSTEM'
  FROM TB_MNU m
 WHERE m.MNU_SCOPE_CD = 'CST'
   AND m.MNU_CD NOT IN ('ADMIN', 'LVPL', 'LVGR', 'COHL', 'TMWT');

-- 4-4) FI: ADMIN 트리 + Team Working Time 제외, Finance 포함
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'FI'),
       m.ID, 'Y', 'Y', 'Y', 'N', 'N', 'Y', 'SYSTEM', 'SYSTEM'
  FROM TB_MNU m
 WHERE m.MNU_SCOPE_CD = 'CST'
   AND m.MNU_CD NOT IN ('ADMIN', 'LVPL', 'LVGR', 'COHL', 'TMWT');

-- 4-5) USR: ADMIN 트리 + Team Working Time + Finance 제외
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'USR'),
       m.ID, 'Y', 'N', 'N', 'N', 'N', 'N', 'SYSTEM', 'SYSTEM'
  FROM TB_MNU m
 WHERE m.MNU_SCOPE_CD = 'CST'
   AND m.MNU_CD NOT IN ('ADMIN', 'LVPL', 'LVGR', 'COHL', 'TMWT', 'FNC');

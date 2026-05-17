-- ============================================================
-- V19: Role 단순화 (14개 → 5개) + Customer Admin 메뉴 트리 추가
-- ============================================================
-- 변경 사항:
--   1) Role 14개 → 5개 (PADM/CADM/HR/FI/USR)
--   2) ROLE_LVL_CD 위계 부여 (높을수록 권한 큼)
--   3) 기존 TB_USR_ROLE / TB_ROLE_MNU_AUTH 모두 클리어 후 재구성
--   4) ggamgang@sinwoo-itc.com → PADM 부여
--   5) 새 메뉴: Customer Admin > Leave Policy / Leave Grants / Company Holidays
--   6) 기존 메뉴는 PADM에만 매핑 (사용자가 추후 권한 지정)
--   7) 새 메뉴는 권한별 매핑 (Leave Policy=LVL>=30, 나머지=CADM)
-- ============================================================

-- ── 1. 기존 권한 매핑/사용자-역할 클리어 ─────────────────────
DELETE FROM TB_ROLE_MNU_AUTH;
DELETE FROM TB_USR_ROLE;

-- ── 2. 기존 14개 ROLE 삭제 ──────────────────────────────────
DELETE FROM TB_ROLE;
ALTER TABLE TB_ROLE AUTO_INCREMENT = 1;

-- ── 3. 새 5개 ROLE 시드 ─────────────────────────────────────
INSERT INTO TB_ROLE (ROLE_CD, ROLE_NM, ROLE_SCOPE_CD, ROLE_LVL_CD, CRT_BY, UPD_BY) VALUES
  ('PADM', 'Platform Admin',   'PLATFORM', '100', 'SYSTEM', 'SYSTEM'),
  ('CADM', 'Customer Admin',   'CUSTOMER', '50',  'SYSTEM', 'SYSTEM'),
  ('HR',   'Customer HR',      'CUSTOMER', '30',  'SYSTEM', 'SYSTEM'),
  ('FI',   'Customer Finance', 'CUSTOMER', '20',  'SYSTEM', 'SYSTEM'),
  ('USR',  'User',             'CUSTOMER', '10',  'SYSTEM', 'SYSTEM');

-- ── 4. ggamgang@sinwoo-itc.com (USR ID=1) → PADM 부여 ───────
INSERT INTO TB_USR_ROLE (USR_ID, ROLE_ID, CRT_BY, UPD_BY)
SELECT 1, r.ID, 'SYSTEM', 'SYSTEM' FROM TB_ROLE r WHERE r.ROLE_CD = 'PADM';

-- ── 5. Customer Admin 메뉴 트리 추가 (CUSTOMER 스코프) ─────
-- DSP_ORD=60 (기존 5개 메뉴 다음 위치)
INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, CRT_BY, UPD_BY) VALUES
  ('MNU_CUSTOMER_ADMIN',              'MNU.CUSTOMER.ADMIN',              'Customer Admin',   'CUSTOMER', NULL, NULL,                                'shield', 60, 'Y', 'SYSTEM', 'SYSTEM');

SET @ADMIN_ID := LAST_INSERT_ID();

INSERT INTO TB_MNU (MNU_CD, MNU_NM_CD, MNU_NM, MNU_SCOPE_CD, UP_MNU_ID, PATH_URI, ICON_NM, DSP_ORD, USE_YN, CRT_BY, UPD_BY) VALUES
  ('MNU_CUSTOMER_ADMIN_LEAVE_POLICY', 'MNU.CUSTOMER.ADMIN.LEAVE_POLICY', 'Leave Policy',     'CUSTOMER', @ADMIN_ID, '/customer/admin/leave-policy', 'settings', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('MNU_CUSTOMER_ADMIN_LEAVE_GRANT',  'MNU.CUSTOMER.ADMIN.LEAVE_GRANT',  'Leave Grants',     'CUSTOMER', @ADMIN_ID, '/customer/admin/leave-grants', 'list',     20, 'Y', 'SYSTEM', 'SYSTEM'),
  ('MNU_CUSTOMER_ADMIN_CO_HOLIDAY',   'MNU.CUSTOMER.ADMIN.CO_HOLIDAY',   'Company Holidays', 'CUSTOMER', @ADMIN_ID, '/customer/admin/holidays',     'calendar', 30, 'Y', 'SYSTEM', 'SYSTEM');

-- ── 6. TB_ROLE_MNU_AUTH 매핑 ────────────────────────────────
-- 6-1) 모든 기존 메뉴 → PADM (Platform Admin) 만 부여
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT
  (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'PADM'),
  m.ID, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM TB_MNU m
WHERE m.USE_YN = 'Y';

-- 6-2) Customer Admin 트리 → CADM 부여 (CADM = Customer Admin)
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT
  (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'CADM'),
  m.ID, 'Y', 'Y', 'Y', 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM TB_MNU m
WHERE m.MNU_CD IN (
  'MNU_CUSTOMER_ADMIN',
  'MNU_CUSTOMER_ADMIN_LEAVE_POLICY',
  'MNU_CUSTOMER_ADMIN_LEAVE_GRANT',
  'MNU_CUSTOMER_ADMIN_CO_HOLIDAY'
);

-- 6-3) Leave Policy + Customer Admin 트리 → HR 부여 (LVL >= 30 정책)
--      HR admin이 트리를 보고 그 안의 Leave Policy 항목까지 접근 가능
INSERT INTO TB_ROLE_MNU_AUTH (ROLE_ID, MNU_ID, VIEW_YN, CRT_YN, UPD_YN, DEL_YN, APRV_YN, EXPRT_YN, CRT_BY, UPD_BY)
SELECT
  (SELECT ID FROM TB_ROLE WHERE ROLE_CD = 'HR'),
  m.ID, 'Y', 'Y', 'Y', 'N', 'N', 'Y', 'SYSTEM', 'SYSTEM'
FROM TB_MNU m
WHERE m.MNU_CD IN (
  'MNU_CUSTOMER_ADMIN',
  'MNU_CUSTOMER_ADMIN_LEAVE_POLICY'
);

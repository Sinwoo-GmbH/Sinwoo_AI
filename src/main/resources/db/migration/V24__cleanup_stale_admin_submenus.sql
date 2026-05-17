-- ============================================================
-- V24: TB_MNU 미사용 ADMIN sub-menu 정리 + MENU 상위 메뉴 위치 보정
-- ============================================================
-- 대상: V14에서 시드된 17개 ADMIN 하위 메뉴 — UI 라우팅/컴포넌트 없음
--       (MNU_ADMIN_AUDIT, _TENANT_LIST, _TENANT_SETTINGS, _ROLE_POLICY,
--        _PLAN_CATALOG, _PAYMENT_GATES, _UPGRADE_QUEUE, _MENU_TREE,
--        _TAB_POLICY, _DEPTH_POLICY, _COMPANY_PROFILE, _WORKSPACE_POLICY,
--        _MENU_POLICY, _DEPTH_EDITOR, _CHANGE_HISTORY, _ACCESS_LOGS,
--        _COMPLIANCE)
-- 또한 MENU(=MNU_ADMIN_MENU)의 UP_MNU_ID 가 AUTH(3) 아래로 잘못 매달려 있음
--      → NULL 로 보정해 top-level 로 복원
-- 유지: ADM scope 5개 top-level (ADSH/TNT/AUTH/BILL/MENU)
-- ============================================================

-- ── 1. 자식의 UP_MNU_ID 끊기 (자기참조 FK 회피) ──────────────
UPDATE TB_MNU
   SET UP_MNU_ID = NULL
 WHERE MNU_SCOPE_CD = 'ADM'
   AND MNU_CD LIKE 'MNU_ADMIN_%';

-- ── 2. TB_ROLE_MNU_AUTH 정리 ─────────────────────────────────
DELETE a FROM TB_ROLE_MNU_AUTH a
  JOIN TB_MNU m ON m.ID = a.MNU_ID
 WHERE m.MNU_SCOPE_CD = 'ADM'
   AND m.MNU_CD LIKE 'MNU_ADMIN_%';

-- ── 3. TB_CD 번역 정리 (MNU_NM 그룹의 옛 ADMIN 항목) ──────────
DELETE c FROM TB_CD c
  JOIN TB_CD_GRP g ON g.ID = c.GRP_ID AND g.GRP_CD = 'MNU_NM'
 WHERE c.CD LIKE 'MNU_ADMIN_%';

-- ── 4. TB_MNU 본체 삭제 ──────────────────────────────────────
DELETE FROM TB_MNU
 WHERE MNU_SCOPE_CD = 'ADM'
   AND MNU_CD LIKE 'MNU_ADMIN_%';

-- ── 5. MENU top-level 복원 ───────────────────────────────────
UPDATE TB_MNU SET UP_MNU_ID = NULL WHERE MNU_CD = 'MENU' AND MNU_SCOPE_CD = 'ADM';

SET @MNU_GRP_ID = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'MNU_NM' LIMIT 1);

INSERT INTO TB_CD (
    GRP_ID,
    CD,
    CD_NM_KO,
    CD_NM_EN,
    CD_NM_DE,
    CD_DESC_KO,
    CD_DESC_EN,
    CD_DESC_DE,
    USE_YN,
    DSP_ORD,
    CRT_BY,
    UPD_BY
) VALUES
    (@MNU_GRP_ID, 'MNU_ADMIN_DASH', '관리자 대시보드', 'Admin Dashboard', 'Admin-Dashboard', NULL, NULL, NULL, 'Y', 10, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_ADMIN_TENANT', '테넌트 관리', 'Tenant Management', 'Mandantenverwaltung', NULL, NULL, NULL, 'Y', 20, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_ADMIN_AUTH', '권한 관리', 'Authorization Management', 'Berechtigungsverwaltung', NULL, NULL, NULL, 'Y', 30, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_ADMIN_BILL', '결제 관리', 'Billing Management', 'Abrechnungsverwaltung', NULL, NULL, NULL, 'Y', 40, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_ADMIN_MENU', '메뉴 관리', 'Menu Management', 'Menüverwaltung', NULL, NULL, NULL, 'Y', 50, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_CUSTOMER_DASH', '고객 대시보드', 'Customer Dashboard', 'Kunden-Dashboard', NULL, NULL, NULL, 'Y', 10, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_CUSTOMER_TEAM', '팀 관리', 'Team Management', 'Teamverwaltung', NULL, NULL, NULL, 'Y', 20, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_CUSTOMER_HR', '인사 관리', 'HR Management', 'HR-Verwaltung', NULL, NULL, NULL, 'Y', 30, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_CUSTOMER_FIN', '재무 관리', 'Finance Management', 'Finanzverwaltung', NULL, NULL, NULL, 'Y', 40, 'SYSTEM', 'SYSTEM'),
    (@MNU_GRP_ID, 'MNU_CUSTOMER_PAY', '결제 센터', 'Payment Center', 'Zahlungscenter', NULL, NULL, NULL, 'Y', 50, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    CD_NM_KO = VALUES(CD_NM_KO),
    CD_NM_EN = VALUES(CD_NM_EN),
    CD_NM_DE = VALUES(CD_NM_DE),
    USE_YN = VALUES(USE_YN),
    DSP_ORD = VALUES(DSP_ORD),
    UPD_BY = 'SYSTEM';

INSERT INTO TB_CD_GRP (
    GRP_CD, GRP_NM_KO, GRP_NM_EN, GRP_NM_DE, SYS_YN, USE_YN, DSP_ORD, CRT_BY, UPD_BY
)
SELECT
    'ATTND_FLAG', '근태 구분', 'Attendance Flag', 'Anwesenheitskennzeichen', 'Y', 'Y', 40, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM TB_CD_GRP
    WHERE GRP_CD = 'ATTND_FLAG'
);

SET @ATTND_FLAG_GRP_ID = (
    SELECT ID
    FROM TB_CD_GRP
    WHERE GRP_CD = 'ATTND_FLAG'
    LIMIT 1
);

INSERT INTO TB_CD (
    GRP_ID, CD, CD_NM_KO, CD_NM_EN, CD_NM_DE,
    CD_DESC_KO, CD_DESC_EN, CD_DESC_DE,
    USE_YN, DSP_ORD, CRT_BY, UPD_BY
) VALUES
    (
        @ATTND_FLAG_GRP_ID, 'CHECKED_IN', '출근', 'Check-in', 'Eingecheckt',
        '출근만 기록된 상태', 'Attendance check-in state', 'Status mit erfasster Arbeitsaufnahme',
        'Y', 10, 'SYSTEM', 'SYSTEM'
    ),
    (
        @ATTND_FLAG_GRP_ID, 'CHECKED_OUT', '퇴근', 'Check-out', 'Ausgecheckt',
        '출근과 퇴근이 모두 기록된 상태', 'Attendance check-out state', 'Status mit erfasster Arbeitsbeendigung',
        'Y', 20, 'SYSTEM', 'SYSTEM'
    ),
    (
        @ATTND_FLAG_GRP_ID, 'LEAVE', '휴가', 'Leave', 'Urlaub',
        '휴가 또는 부재로 처리된 상태', 'Leave state', 'Status für Urlaub oder genehmigte Abwesenheit',
        'Y', 30, 'SYSTEM', 'SYSTEM'
    ),
    (
        @ATTND_FLAG_GRP_ID, 'BUSINESS_TRIP', '출장', 'Business Trip', 'Dienstreise',
        '출장 일정으로 처리된 상태', 'Business trip state', 'Status für Dienstreise',
        'Y', 40, 'SYSTEM', 'SYSTEM'
    )
ON DUPLICATE KEY UPDATE
    CD_NM_KO = VALUES(CD_NM_KO),
    CD_NM_EN = VALUES(CD_NM_EN),
    CD_NM_DE = VALUES(CD_NM_DE),
    CD_DESC_KO = VALUES(CD_DESC_KO),
    CD_DESC_EN = VALUES(CD_DESC_EN),
    CD_DESC_DE = VALUES(CD_DESC_DE),
    USE_YN = VALUES(USE_YN),
    DSP_ORD = VALUES(DSP_ORD),
    UPD_BY = VALUES(UPD_BY);

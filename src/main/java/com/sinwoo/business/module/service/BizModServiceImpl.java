package com.sinwoo.business.module.service;

import com.sinwoo.business.module.dto.BizModListResponse;
import com.sinwoo.business.module.dto.BizModMetricResponse;
import com.sinwoo.business.module.dto.BizModResponse;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BizModServiceImpl implements BizModService {

    private static final String CO_SCOPE = "TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId)";
    private static final String ACTIVE_CO_SCOPE = CO_SCOPE + " AND STS_CD = 'ACTIVE'";

    private static final List<BizModDefinition> MODULES = List.of(
            mod(
                    "REQ_WORK_TIME",
                    "MNU_CUSTOMER_MY_TIME",
                    "REQUEST",
                    text("근무시간", "Work Time", "Arbeitszeit"),
                    text("AS-IS tb_daily_work_time의 S/E 행을 하루 단위 업무 시간으로 정리합니다.", "Normalizes AS-IS work time start/end rows into one daily business entry.", "Normalisiert Arbeitszeit-Start/Ende je Arbeitstag."),
                    "RQW",
                    "/request/workTime",
                    "TB_WORK_TIME_ENTRY",
                    List.of("TB_WORK_TIME_ENTRY"),
                    "SELECT COUNT(*) FROM TB_WORK_TIME_ENTRY WHERE " + ACTIVE_CO_SCOPE
            ),
            mod("REQ_EXPENSE", "MNU_BIZ_REQ_EXPENSE", "REQUEST", text("비용 신청", "Expense", "Ausgabe"),
                    text("일반 비용 신청과 승인 대상을 관리합니다.", "Manages general expense requests and approvals.", "Verwaltet allgemeine Ausgabenantraege."),
                    "RQX", "/request/expense", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_EXP_RATE", "TB_EXP_ATTACH", "TB_EXP_APRV"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE + " AND COALESCE(EXP_TP_CD, '') NOT IN ('PO','SO','PAY','OPEX')"),
            mod("REQ_TRIP", "MNU_BIZ_REQ_TRIP", "REQUEST", text("출장 신청", "Business Trip", "Geschaeftsreise"),
                    text("출장 계획, 승인선, 출장비 연계를 관리합니다.", "Manages business trip plans, approvals, and expense linkage.", "Verwaltet Reiseplanung, Freigabe und Kostenbezug."),
                    "EQB", "/request/buTrip", "TB_BIZ_TRIP", List.of("TB_BIZ_TRIP", "TB_BIZ_TRIP_APRV", "TB_DAILY_ALLOWANCE"),
                    "SELECT COUNT(*) FROM TB_BIZ_TRIP WHERE " + ACTIVE_CO_SCOPE),
            mod("REQ_LEAVE", "MNU_CUSTOMER_LEAVE", "REQUEST", text("휴가 신청", "Vacation", "Urlaub"),
                    text("휴가 신청, 승인, 차감 수량을 관리합니다.", "Manages leave requests, approvals, and leave-day usage.", "Verwaltet Urlaubsantraege und Freigaben."),
                    "EQV", "/request/vac", "TB_LEAVE_REQ", List.of("TB_LEAVE_REQ", "TB_LEAVE_APRV"),
                    "SELECT COUNT(*) FROM TB_LEAVE_REQ WHERE " + ACTIVE_CO_SCOPE),
            mod("CLAIM_EXPENSE", "MNU_BIZ_CLAIM_EXPENSE", "MY_CLAIMS", text("비용 청구", "Expense Claims", "Ausgaben"),
                    text("직원이 제출한 월 비용 청구 문서를 관리합니다.", "Manages monthly expense claim documents submitted by emps.", "Verwaltet monatliche Ausgabenabrechnungen."),
                    "MCX", "/myClaim/monthExp", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_EXP_RATE", "TB_EXP_ATTACH", "TB_EXP_APRV"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE + " AND BIZ_TRIP_ID IS NULL"),
            mod("CLAIM_TRAVEL_EXP", "MNU_BIZ_CLAIM_TRAVEL_EXP", "MY_CLAIMS", text("출장비 청구", "Travel Expense", "Reisekosten"),
                    text("출장 계획과 연결된 출장비 청구를 관리합니다.", "Manages travel expense claims linked to business trips.", "Verwaltet Reisekosten zu Geschaeftsreisen."),
                    "MCT", "/myClaim/travelExp", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_BIZ_TRIP"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE + " AND BIZ_TRIP_ID IS NOT NULL"),
            mod("RPT_WORK_TIME", "MNU_BIZ_RPT_WORK_TIME", "REPORT", text("근무시간 리포트", "Work Time", "Arbeitszeit"),
                    text("직원/부서별 근무시간 이력을 조회합니다.", "Reports work-time history by emp and dept.", "Berichtet Arbeitszeiten nach Mitarbeiter und Abteilung."),
                    "RPW", "/report/workTime", "TB_WORK_TIME_ENTRY", List.of("TB_WORK_TIME_ENTRY", "TB_ATTND"),
                    "SELECT COUNT(*) FROM TB_WORK_TIME_ENTRY WHERE " + ACTIVE_CO_SCOPE),
            mod("RPT_MONTHLY_EXP", "MNU_BIZ_RPT_MONTHLY_EXP", "REPORT", text("월 비용 리포트", "Monthly Expenses", "Monatliche Ausgaben"),
                    text("월 비용 문서와 비용 유형별 집계를 조회합니다.", "Reports monthly expense documents and type summaries.", "Berichtet monatliche Ausgaben."),
                    "RPM", "/report/monthlyExp", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_TXN_PURCHASE", "MNU_BIZ_FIN_TXN_PURCHASE", "FINANCE_TXN", text("매입", "Purchase", "Einkauf"),
                    text("매입 전표와 비용 라인을 관리합니다.", "Manages purchase documents and expense lines.", "Verwaltet Einkaufsbelege."),
                    "FITP", "/finance/trans/purchase", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_EXP_RATE", "TB_ASSET"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE + " AND EXP_TP_CD = 'PO'"),
            mod("FIN_TXN_SALES", "MNU_BIZ_FIN_TXN_SALES", "FINANCE_TXN", text("매출", "Sales", "Verkauf"),
                    text("매출 전표와 고정 매출 연계를 관리합니다.", "Manages sales documents and fixed-sales linkage.", "Verwaltet Verkaufsbelege."),
                    "FITS", "/finance/trans/sales", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_OP_SALES"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE + " AND EXP_TP_CD = 'SO'"),
            mod("FIN_TXN_OPEX", "MNU_BIZ_FIN_TXN_OPEX", "FINANCE_TXN", text("운영비", "OpEx", "Betriebsausgaben"),
                    text("운영비 거래와 비용 스케줄을 관리합니다.", "Manages operating expense transactions and schedules.", "Verwaltet Betriebsausgaben."),
                    "FITO", "/finance/trans/opexp", "TB_OP_EXP", List.of("TB_OP_EXP", "TB_OP_EXP_RATE", "TB_OP_EXP_SCHD", "TB_OP_EXP_ATTACH"),
                    "SELECT COUNT(*) FROM TB_OP_EXP WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_TXN_CORP_ACC", "MNU_BIZ_FIN_TXN_CORP_ACC", "FINANCE_TXN", text("법인계좌 거래", "Corporate Account", "Firmenkonto"),
                    text("법인계좌 입출금, 매핑, 승인 처리를 관리합니다.", "Manages bank transactions, expense mapping, and approvals.", "Verwaltet Kontotransaktionen und Freigaben."),
                    "FITC", "/finance/trans/corporate", "TB_CORP_TXN", List.of("TB_CORP_ACC", "TB_CORP_TXN", "TB_CORP_TXN_APRV", "TB_BANK_EXP_MAP"),
                    "SELECT COUNT(*) FROM TB_CORP_TXN WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_TXN_PAYROLL", "MNU_BIZ_FIN_TXN_PAYROLL", "FINANCE_TXN", text("급여", "Payroll", "Gehaltsabrechnung"),
                    text("급여 헤더와 급여 비용 라인을 관리합니다.", "Manages payroll headers and payroll expense lines.", "Verwaltet Gehaltsabrechnungen."),
                    "FITPAY", "/finance/trans/payroll", "TB_PAYROLL_HD", List.of("TB_PAYROLL_HD", "TB_PAYROLL_LINE"),
                    "SELECT COUNT(*) FROM TB_PAYROLL_HD WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_MGT_EXPENSE", "MNU_CUSTOMER_EXPENSE_REVIEW", "FINANCE_MGT", text("비용 관리", "Expense", "Ausgaben"),
                    text("전체 비용 문서의 승인, 반려, 삭제 업무를 관리합니다.", "Manages approval, rejection, and deletion of expense documents.", "Verwaltet Freigaben fuer Ausgaben."),
                    "FIMX", "/finance/mgt/expense", "TB_EXP_DOC", List.of("TB_EXP_DOC", "TB_EXP_LINE", "TB_EXP_APRV"),
                    "SELECT COUNT(*) FROM TB_EXP_DOC WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_MGT_BALANCE", "MNU_BIZ_FIN_MGT_BALANCE", "FINANCE_MGT", text("잔액 관리", "Balance", "Kontostand"),
                    text("법인계좌 잔액과 거래 매핑 상태를 관리합니다.", "Manages corporate account balances and transaction mapping.", "Verwaltet Kontostaende und Zuordnungen."),
                    "FIMB", "/finance/mgt/balance", "TB_CORP_TXN", List.of("TB_CORP_ACC", "TB_CORP_TXN", "TB_BANK_EXP_MAP"),
                    "SELECT COUNT(*) FROM TB_CORP_TXN WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_MGT_CLOSING", "MNU_BIZ_FIN_MGT_CLOSING", "FINANCE_MGT", text("마감", "Closing", "Abschluss"),
                    text("월/연 단위 회계 마감 상태를 관리합니다.", "Manages monthly and yearly closing status.", "Verwaltet Monats- und Jahresabschluss."),
                    "FIMV", "/finance/mgt/closing", "TB_PERIOD_CLOSE", List.of("TB_PERIOD_CLOSE"),
                    "SELECT COUNT(*) FROM TB_PERIOD_CLOSE WHERE " + CO_SCOPE),
            mod("FIN_MGT_ASSET", "MNU_BIZ_FIN_MGT_ASSET", "FINANCE_MGT", text("자산", "Asset", "Anlage"),
                    text("자산 등록과 감가상각 스케줄을 관리합니다.", "Manages assets and depreciation schedules.", "Verwaltet Anlagen und Abschreibungen."),
                    "FIMA", "/finance/mgt/asset", "TB_ASSET", List.of("TB_ASSET", "TB_ASSET_SCHD", "TB_ASSET_ATTACH"),
                    "SELECT COUNT(*) FROM TB_ASSET WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_MGT_FIXED_OPEX", "MNU_BIZ_FIN_MGT_FIXED_OPEX", "FINANCE_MGT", text("고정 운영비", "Fixed OpEx", "Fixe Kosten"),
                    text("반복 운영비 계약과 예정 스케줄을 관리합니다.", "Manages recurring operating expense contracts and schedules.", "Verwaltet wiederkehrende Kosten."),
                    "FIMO", "/finance/mgt/opexp", "TB_OP_EXP", List.of("TB_OP_EXP", "TB_OP_EXP_SCHD"),
                    "SELECT COUNT(*) FROM TB_OP_EXP WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_MGT_FIXED_SALES", "MNU_BIZ_FIN_MGT_FIXED_SALES", "FINANCE_MGT", text("고정 매출", "Fixed Sales", "Fixumsatz"),
                    text("반복 매출 계약과 예정 스케줄을 관리합니다.", "Manages recurring sales contracts and schedules.", "Verwaltet wiederkehrende Umsaetze."),
                    "FIMS", "/finance/mgt/opsales", "TB_OP_SALES", List.of("TB_OP_SALES", "TB_OP_SALES_SCHD", "TB_OP_SALES_ATTACH"),
                    "SELECT COUNT(*) FROM TB_OP_SALES WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_RPT_LEDGER", "MNU_BIZ_FIN_RPT_LEDGER", "FINANCE_REPORT", text("계정 원장", "Account Ledger", "Sachkontenblatt"),
                    text("비용/은행 거래를 원장 관점으로 조회합니다.", "Reports expense and bank transactions from ledger perspective.", "Berichtet Transaktionen als Kontenblatt."),
                    "FIRA", "/finance/reports/accLedger", "VW_ACCOUNT_LEDGER", List.of("VW_ACCOUNT_LEDGER", "TB_EXP_RATE", "TB_CORP_TXN"),
                    "SELECT COUNT(*) FROM VW_ACCOUNT_LEDGER WHERE TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId) AND COALESCE(STS_CD, 'ACTIVE') <> 'DELETED'"),
            mod("FIN_RPT_ASSET", "MNU_BIZ_FIN_RPT_ASSET", "FINANCE_REPORT", text("자산 목록", "Asset List", "Anlagenliste"),
                    text("자산과 감가상각 현황을 조회합니다.", "Reports asset and depreciation status.", "Berichtet Anlagenstatus."),
                    "FIRASSET", "/finance/reports/asset", "TB_ASSET", List.of("TB_ASSET", "TB_ASSET_SCHD"),
                    "SELECT COUNT(*) FROM TB_ASSET WHERE " + ACTIVE_CO_SCOPE),
            mod("FIN_RPT_ANNUAL", "MNU_BIZ_FIN_RPT_ANNUAL", "FINANCE_REPORT", text("연간 리포트", "Annual Reports", "Jahresberichte"),
                    text("재무제표 템플릿과 원장 집계를 기준으로 연간 보고서를 구성합니다.", "Builds annual reports from statement templates and ledger aggregation.", "Erstellt Jahresberichte."),
                    "FIRN", "/finance/reports/annualReports", "TB_FI_STMT_TEMPLATE", List.of("TB_FI_STMT_TEMPLATE", "TB_FI_STMT_LINE", "TB_FI_STMT_LINE_ACC", "VW_ACCOUNT_LEDGER"),
                    "SELECT COUNT(*) FROM TB_FI_STMT_TEMPLATE WHERE " + ACTIVE_CO_SCOPE),
            mod("MST_EMP", "MNU_CUSTOMER_EMPLOYEES", "MASTER", text("직원", "Emps", "Mitarbeiter"),
                    text("직원 기본정보와 AS-IS HR 확장 프로필을 관리합니다.", "Manages emps and AS-IS HR profile extensions.", "Verwaltet Mitarbeiterprofile."),
                    "ADUE", "/admin/empMgt", "TB_EMP_HR_PROFILE", List.of("TB_EMP", "TB_EMP_HR_PROFILE"),
                    "SELECT COUNT(*) FROM TB_EMP_HR_PROFILE WHERE " + ACTIVE_CO_SCOPE),
            mod("MST_DEPT", "MNU_CUSTOMER_DEPARTMENTS", "MASTER", text("부서", "Depts", "Abteilungen"),
                    text("부서와 휴가 기준을 관리합니다.", "Manages depts and leave defaults.", "Verwaltet Abteilungen."),
                    "ADUD", "/admin/deptMgt", "TB_DEPT", List.of("TB_DEPT"),
                    "SELECT COUNT(*) FROM TB_DEPT WHERE " + ACTIVE_CO_SCOPE),
            mod("MST_ACC", "MNU_BIZ_MST_ACC", "MASTER", text("계정 코드", "Account Codes", "Kontencodes"),
                    text("재무 계정 코드와 전표 분류 속성을 관리합니다.", "Manages financial account codes and posting attributes.", "Verwaltet Kontencodes."),
                    "ADUA_ACC", "/admin/accCodeMgt", "TB_FIN_ACC", List.of("TB_FIN_ACC"),
                    "SELECT COUNT(*) FROM TB_FIN_ACC WHERE " + CO_SCOPE + " AND COALESCE(USE_YN, 'Y') = 'Y'"),
            mod("MST_CORP_ACC", "MNU_BIZ_MST_CORP_ACC", "MASTER", text("법인 계좌", "Corporate Accounts", "Firmenkonten"),
                    text("법인계좌와 은행 기본정보를 관리합니다.", "Manages corporate accounts and bank master data.", "Verwaltet Firmenkonten."),
                    "ADUC", "/admin/accCardMgt", "TB_CORP_ACC", List.of("TB_CORP_ACC"),
                    "SELECT COUNT(*) FROM TB_CORP_ACC WHERE " + CO_SCOPE + " AND COALESCE(USE_YN, 'Y') = 'Y'"),
            mod("MST_DAILY_ALLOWANCE", "MNU_BIZ_MST_DAILY_ALLOWANCE", "MASTER", text("일비", "Daily Allowance", "Tagegeld"),
                    text("국가/도시별 출장 일비 기준을 관리합니다.", "Manages daily allowance by country and city.", "Verwaltet Tagegeldsaetze."),
                    "ADUA_DAILY", "/admin/dlyExpMgt", "TB_DAILY_ALLOWANCE", List.of("TB_DAILY_ALLOWANCE"),
                    "SELECT COUNT(*) FROM TB_DAILY_ALLOWANCE WHERE (TENANT_ID IS NULL OR TENANT_ID = :tenantId) AND COALESCE(USE_YN, 'Y') = 'Y'"),
            mod("MST_CORP_CAL", "MNU_BIZ_MST_CORP_CAL", "MASTER", text("회사 달력", "Corporate Calendar", "Firmenkalender"),
                    text("회사 휴일과 반복 휴일을 관리합니다.", "Manages company holidays and annual holidays.", "Verwaltet Firmenfeiertage."),
                    "ADUL", "/admin/corpCalMgt", "TB_CO_HOLI", List.of("TB_CO_HOLI", "TB_HOLI"),
                    "SELECT COUNT(*) FROM TB_CO_HOLI WHERE " + CO_SCOPE + " AND COALESCE(USE_YN, 'Y') = 'Y'"),
            mod("MST_ACC_MAPPING", "MNU_BIZ_MST_ACC_MAPPING", "MASTER", text("계정 매핑", "Account Mapping", "Kontenzuordnung"),
                    text("이월 계정과 결산 계정 매핑을 관리합니다.", "Manages carryover and closing account mappings.", "Verwaltet Kontenzuordnungen."),
                    "ADUM", "/admin/accCodeMapping", "TB_FIN_CARRYOVER_MAP", List.of("TB_FIN_CARRYOVER_MAP", "TB_FIN_ACC"),
                    "SELECT COUNT(*) FROM TB_FIN_CARRYOVER_MAP WHERE " + CO_SCOPE + " AND COALESCE(USE_YN, 'Y') = 'Y'"),
            mod("MST_FI_STMT", "MNU_BIZ_MST_FI_STMT", "MASTER", text("재무제표", "Financial Statements", "Finanzberichte"),
                    text("재무제표 템플릿, 라인, 계정 연결을 관리합니다.", "Manages statement templates, lines, and account links.", "Verwaltet Berichtsvorlagen."),
                    "ADUF", "/admin/financialStatements", "TB_FI_STMT_TEMPLATE", List.of("TB_FI_STMT_TEMPLATE", "TB_FI_STMT_LINE", "TB_FI_STMT_LINE_ACC"),
                    "SELECT COUNT(*) FROM TB_FI_STMT_TEMPLATE WHERE " + ACTIVE_CO_SCOPE)
    );

    private static final Map<String, BizModDefinition> MOD_BY_CD = MODULES.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(BizModDefinition::modCd, item -> item));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public BizModListResponse getBizMods(AuthenticatedUsr authenticatedUsr, Locale locale) {
        return new BizModListResponse(
                MODULES.stream()
                        .map(mod -> toResponse(mod, authenticatedUsr, locale))
                        .toList()
        );
    }

    @Override
    public BizModResponse getBizMod(AuthenticatedUsr authenticatedUsr, String modCd, Locale locale) {
        BizModDefinition mod = MOD_BY_CD.get(normalizeModCd(modCd));
        if (mod == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_MODULE_NOT_FOUND", "Business module not found");
        }
        return toResponse(mod, authenticatedUsr, locale);
    }

    private BizModResponse toResponse(
            BizModDefinition definition,
            AuthenticatedUsr authenticatedUsr,
            Locale locale
    ) {
        long itemCnt = queryCount(definition.countSql(), authenticatedUsr);
        String tableSummary = String.join(", ", definition.tblNms());
        return new BizModResponse(
                definition.modCd(),
                definition.mnuCd(),
                definition.groupCd(),
                definition.modNm().resolve(locale),
                definition.desc().resolve(locale),
                definition.legacyMnuCd(),
                definition.legacyUri(),
                definition.primaryTblNm(),
                definition.tblNms(),
                itemCnt,
                List.of(
                        new BizModMetricResponse("ITEM_CNT", "Items", Long.toString(itemCnt)),
                        new BizModMetricResponse("PRIMARY_TABLE", "Primary table", definition.primaryTblNm()),
                        new BizModMetricResponse("TABLES", "Tables", tableSummary)
                )
        );
    }

    private long queryCount(String sql, AuthenticatedUsr authenticatedUsr) {
        Long value = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("tenantId", authenticatedUsr.tenantId())
                        .addValue("coId", authenticatedUsr.coId())
                        .addValue("usrId", authenticatedUsr.usrId()),
                Long.class
        );
        return value == null ? 0L : value;
    }

    private static String normalizeModCd(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static BizModDefinition mod(
            String modCd,
            String mnuCd,
            String groupCd,
            LocalizedText modNm,
            LocalizedText desc,
            String legacyMnuCd,
            String legacyUri,
            String primaryTblNm,
            List<String> tblNms,
            String countSql
    ) {
        return new BizModDefinition(
                modCd,
                mnuCd,
                groupCd,
                modNm,
                desc,
                legacyMnuCd,
                legacyUri,
                primaryTblNm,
                tblNms,
                countSql
        );
    }

    private static LocalizedText text(String ko, String en, String de) {
        return new LocalizedText(ko, en, de);
    }

    private record BizModDefinition(
            String modCd,
            String mnuCd,
            String groupCd,
            LocalizedText modNm,
            LocalizedText desc,
            String legacyMnuCd,
            String legacyUri,
            String primaryTblNm,
            List<String> tblNms,
            String countSql
    ) {
    }

    private record LocalizedText(String ko, String en, String de) {

        String resolve(Locale locale) {
            if (locale == null) {
                return en;
            }
            return switch (locale.getLanguage()) {
                case "ko" -> ko;
                case "de" -> de;
                default -> en;
            };
        }
    }
}

package com.sinwoo.business.service;

import com.sinwoo.business.dto.BusinessRecordColumnResponse;
import com.sinwoo.business.dto.BusinessRecordListResponse;
import com.sinwoo.business.dto.BusinessRecordQuery;
import com.sinwoo.business.dto.BusinessRecordResponse;
import com.sinwoo.business.dto.BusinessRecordSaveRequest;
import com.sinwoo.business.dto.BusinessRecordStatusRequest;
import com.sinwoo.business.dto.BusinessRelatedListResponse;
import com.sinwoo.business.dto.BusinessRelatedTableResponse;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessRecordServiceImpl implements BusinessRecordService {

    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String CO_SCOPE = "TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId)";
    private static final String ACTIVE_CO_SCOPE = CO_SCOPE + " AND COALESCE(STS_CD, 'ACTIVE') <> 'DELETED'";
    private static final String ACTIVE_USE_CO_SCOPE = CO_SCOPE + " AND COALESCE(USE_YN, 'Y') = 'Y'";
    private static final String DAILY_ALLOWANCE_SCOPE =
            "(TENANT_ID IS NULL OR TENANT_ID = :tenantId) AND COALESCE(USE_YN, 'Y') = 'Y'";
    private static final String DAILY_ALLOWANCE_MUTATION_SCOPE = "TENANT_ID = :tenantId";
    private static final Set<String> ALLOWED_STATUS_CODES = Set.of("SV", "RQ", "CF", "RJ", "AP");
    private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter TS_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Map<String, RecordDefinition> DEFINITIONS = buildDefinitions();
    private static final Map<String, List<RelationDefinition>> RELATIONS = buildRelations();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public BusinessRecordListResponse getRecords(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            BusinessRecordQuery query,
            Locale locale
    ) {
        RecordDefinition definition = requireDefinition(moduleCd);
        PageSpec pageSpec = normalizePage(query);
        MapSqlParameterSource params = baseParams(authenticatedUser)
                .addValue("size", pageSpec.size())
                .addValue("offset", pageSpec.page() * pageSpec.size());

        String whereSql = buildWhereSql(definition, query, params);
        long totalCount = queryTotalCount(definition, whereSql, params);
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT " + selectColumnsSql(definition) + " FROM " + definition.tableNm()
                        + " WHERE " + whereSql
                        + " ORDER BY " + definition.orderBySql()
                        + " LIMIT :size OFFSET :offset",
                params,
                recordRowMapper(definition)
        );

        return new BusinessRecordListResponse(
                definition.moduleCd(),
                definition.tableNm(),
                definition.creatable(),
                definition.editable(),
                definition.deletable(),
                definition.columns().stream()
                        .filter(column -> column.visible() || column.writable())
                        .map(column -> column.toResponse(locale))
                        .toList(),
                rows,
                totalCount,
                pageSpec.page(),
                pageSpec.size()
        );
    }

    @Override
    @Transactional
    public BusinessRecordResponse createRecord(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            BusinessRecordSaveRequest request
    ) {
        RecordDefinition definition = requireDefinition(moduleCd);
        if (!definition.creatable()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_CREATE_NOT_ALLOWED", "Record creation is not allowed for this module");
        }

        Map<String, Object> insertValues = new LinkedHashMap<>();
        if (definition.tenantScoped()) {
            insertValues.put("TENANT_ID", authenticatedUser.tenantId());
        }
        if (definition.companyScoped()) {
            insertValues.put("CO_ID", resolveCompanyId(authenticatedUser));
        }
        insertValues.putAll(definition.fixedValues());
        mergeWritableValues(definition, request == null ? null : request.values(), insertValues);
        fillRequiredDefaults(definition, insertValues);
        insertValues.putIfAbsent("CRT_BY", actor(authenticatedUser));
        insertValues.putIfAbsent("UPD_BY", actor(authenticatedUser));

        String columnSql = String.join(", ", insertValues.keySet());
        String valueSql = insertValues.keySet().stream().map(column -> ":" + column).collect(Collectors.joining(", "));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "INSERT INTO " + definition.tableNm() + " (" + columnSql + ") VALUES (" + valueSql + ")",
                new MapSqlParameterSource(insertValues),
                keyHolder,
                new String[]{definition.idColumn()}
        );

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "BUSINESS_RECORD_CREATE_FAILED", "Failed to create business record");
        }
        return findRecord(authenticatedUser, definition, key.longValue());
    }

    @Override
    public BusinessRelatedListResponse getRelatedRecords(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            Long recordId
    ) {
        if (recordId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_ID_REQUIRED", "Record id is required");
        }
        RecordDefinition definition = requireDefinition(moduleCd);
        ensureRecordExists(authenticatedUser, definition, recordId);

        List<BusinessRelatedTableResponse> tables = RELATIONS.getOrDefault(definition.moduleCd(), List.of()).stream()
                .map(relation -> queryRelation(authenticatedUser, recordId, relation))
                .toList();
        return new BusinessRelatedListResponse(definition.moduleCd(), recordId, tables);
    }

    @Override
    @Transactional
    public BusinessRecordResponse updateRecord(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            Long recordId,
            BusinessRecordSaveRequest request
    ) {
        RecordDefinition definition = requireDefinition(moduleCd);
        if (!definition.editable()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_UPDATE_NOT_ALLOWED", "Record update is not allowed for this module");
        }
        if (recordId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_ID_REQUIRED", "Record id is required");
        }

        Map<String, Object> values = new LinkedHashMap<>();
        mergeWritableValues(definition, request == null ? null : request.values(), values);
        values.remove(definition.idColumn());
        values.keySet().removeAll(definition.fixedValues().keySet());
        if (values.isEmpty()) {
            return findRecord(authenticatedUser, definition, recordId);
        }
        values.put("UPD_BY", actor(authenticatedUser));
        values.put("id", recordId);

        String setSql = values.keySet().stream()
                .filter(column -> !"id".equals(column))
                .map(column -> column + " = :" + column)
                .collect(Collectors.joining(", "));
        MapSqlParameterSource params = baseParams(authenticatedUser);
        values.forEach(params::addValue);
        int updated = jdbcTemplate.update(
                "UPDATE " + definition.tableNm()
                        + " SET " + setSql + ", UPD_DTM = CURRENT_TIMESTAMP"
                        + " WHERE " + definition.idColumn() + " = :id AND " + definition.mutationWhereSql(),
                params
        );
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_RECORD_NOT_FOUND", "Business record not found");
        }
        return findRecord(authenticatedUser, definition, recordId);
    }

    @Override
    @Transactional
    public BusinessRecordResponse updateRecordStatus(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            Long recordId,
            BusinessRecordStatusRequest request
    ) {
        RecordDefinition definition = requireDefinition(moduleCd);
        if (!definition.hasColumn("APRV_STS_CD")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_STATUS_NOT_SUPPORTED", "Approval status is not supported for this module");
        }
        String nextStatus = request == null || request.aprvStsCd() == null
                ? ""
                : request.aprvStsCd().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUS_CODES.contains(nextStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_STATUS_INVALID", "Approval status is invalid");
        }

        MapSqlParameterSource params = baseParams(authenticatedUser)
                .addValue("id", recordId)
                .addValue("aprvStsCd", nextStatus)
                .addValue("rejectReason", request == null ? null : blankToNull(request.rejectReason()))
                .addValue("updBy", actor(authenticatedUser));
        String rejectSetSql = definition.hasColumn("REJ_RSN_CNTS") ? ", REJ_RSN_CNTS = :rejectReason" : "";
        int updated = jdbcTemplate.update(
                "UPDATE " + definition.tableNm()
                        + " SET APRV_STS_CD = :aprvStsCd" + rejectSetSql + ", UPD_BY = :updBy, UPD_DTM = CURRENT_TIMESTAMP"
                        + " WHERE " + definition.idColumn() + " = :id AND " + definition.mutationWhereSql(),
                params
        );
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_RECORD_NOT_FOUND", "Business record not found");
        }
        return findRecord(authenticatedUser, definition, recordId);
    }

    @Override
    @Transactional
    public void deleteRecord(AuthenticatedUser authenticatedUser, String moduleCd, Long recordId) {
        RecordDefinition definition = requireDefinition(moduleCd);
        if (!definition.deletable()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_DELETE_NOT_ALLOWED", "Record delete is not allowed for this module");
        }
        if (recordId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_ID_REQUIRED", "Record id is required");
        }

        MapSqlParameterSource params = baseParams(authenticatedUser)
                .addValue("id", recordId)
                .addValue("updBy", actor(authenticatedUser));
        String deleteSql;
        if (definition.hasColumn("STS_CD")) {
            deleteSql = "UPDATE " + definition.tableNm()
                    + " SET STS_CD = 'DELETED', UPD_BY = :updBy, UPD_DTM = CURRENT_TIMESTAMP"
                    + " WHERE " + definition.idColumn() + " = :id AND " + definition.mutationWhereSql();
        } else if (definition.hasColumn("USE_YN")) {
            deleteSql = "UPDATE " + definition.tableNm()
                    + " SET USE_YN = 'N', UPD_BY = :updBy, UPD_DTM = CURRENT_TIMESTAMP"
                    + " WHERE " + definition.idColumn() + " = :id AND " + definition.mutationWhereSql();
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_DELETE_NOT_SUPPORTED", "Soft delete is not supported for this module");
        }

        int updated = jdbcTemplate.update(deleteSql, params);
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_RECORD_NOT_FOUND", "Business record not found");
        }
    }

    private BusinessRecordResponse findRecord(AuthenticatedUser authenticatedUser, RecordDefinition definition, Long recordId) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForObject(
                    "SELECT " + selectColumnsSql(definition)
                            + " FROM " + definition.tableNm()
                            + " WHERE " + definition.idColumn() + " = :id AND " + definition.queryWhereSql(),
                    baseParams(authenticatedUser).addValue("id", recordId),
                    recordRowMapper(definition)
            );
            return new BusinessRecordResponse(definition.moduleCd(), definition.tableNm(), row);
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_RECORD_NOT_FOUND", "Business record not found");
        }
    }

    private void ensureRecordExists(AuthenticatedUser authenticatedUser, RecordDefinition definition, Long recordId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + definition.tableNm()
                        + " WHERE " + definition.idColumn() + " = :id AND " + definition.queryWhereSql(),
                baseParams(authenticatedUser).addValue("id", recordId),
                Long.class
        );
        if (count == null || count == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_RECORD_NOT_FOUND", "Business record not found");
        }
    }

    private BusinessRelatedTableResponse queryRelation(
            AuthenticatedUser authenticatedUser,
            Long recordId,
            RelationDefinition relation
    ) {
        MapSqlParameterSource params = baseParams(authenticatedUser).addValue("recordId", recordId);
        Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + relation.tableNm() + " WHERE " + relation.whereSql(),
                params,
                Long.class
        );
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "SELECT " + String.join(", ", relation.columns())
                        + " FROM " + relation.tableNm()
                        + " WHERE " + relation.whereSql()
                        + " ORDER BY " + relation.orderBySql()
                        + " LIMIT 20",
                params,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (String column : relation.columns()) {
                        row.put(column, normalizeJdbcValue(rs.getObject(column)));
                    }
                    return row;
                }
        );
        return new BusinessRelatedTableResponse(
                relation.tableNm(),
                relation.label(),
                relation.columns(),
                rows,
                totalCount == null ? 0L : totalCount
        );
    }

    private String buildWhereSql(RecordDefinition definition, BusinessRecordQuery query, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();
        conditions.add(definition.queryWhereSql());
        String keyword = query == null ? null : blankToNull(query.keyword());
        if (keyword != null) {
            params.addValue("keyword", "%" + keyword.toLowerCase(Locale.ROOT) + "%");
            String keywordSql = definition.columns().stream()
                    .filter(ColumnDefinition::searchable)
                    .map(column -> "LOWER(CAST(" + column.columnNm() + " AS CHAR)) LIKE :keyword")
                    .collect(Collectors.joining(" OR "));
            if (!keywordSql.isBlank()) {
                conditions.add("(" + keywordSql + ")");
            }
        }
        String yearMonth = normalizeYearMonth(query == null ? null : query.yearMonth());
        if (yearMonth != null && definition.periodWhereSql() != null && !definition.periodWhereSql().isBlank()) {
            params.addValue("yearMonth", yearMonth);
            params.addValue("yearMonthCompact", yearMonth.replace("-", ""));
            conditions.add("(" + definition.periodWhereSql() + ")");
        }
        return String.join(" AND ", conditions);
    }

    private long queryTotalCount(RecordDefinition definition, String whereSql, MapSqlParameterSource params) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + definition.tableNm() + " WHERE " + whereSql,
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    private RowMapper<Map<String, Object>> recordRowMapper(RecordDefinition definition) {
        return (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            for (ColumnDefinition column : definition.columns()) {
                if (!column.visible() && !column.writable() && !column.key()) {
                    continue;
                }
                row.put(column.columnNm(), normalizeJdbcValue(rs.getObject(column.columnNm())));
            }
            return row;
        };
    }

    private Object normalizeJdbcValue(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof Time time) {
            return time.toLocalTime().toString();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toString();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros();
        }
        return value;
    }

    private String selectColumnsSql(RecordDefinition definition) {
        return definition.columns().stream()
                .filter(column -> column.visible() || column.writable() || column.key())
                .map(ColumnDefinition::columnNm)
                .collect(Collectors.joining(", "));
    }

    private void mergeWritableValues(RecordDefinition definition, Map<String, Object> requestValues, Map<String, Object> target) {
        if (requestValues == null || requestValues.isEmpty()) {
            return;
        }
        Map<String, ColumnDefinition> writableColumns = definition.columns().stream()
                .filter(ColumnDefinition::writable)
                .filter(column -> !definition.fixedValues().containsKey(column.columnNm()))
                .collect(Collectors.toMap(ColumnDefinition::columnNm, column -> column));
        requestValues.forEach((rawKey, rawValue) -> {
            String columnNm = rawKey == null ? "" : rawKey.trim().toUpperCase(Locale.ROOT);
            ColumnDefinition column = writableColumns.get(columnNm);
            if (column == null || column.key()) {
                return;
            }
            Object normalized = normalizeRequestValue(column, rawValue);
            if (normalized == null && column.required()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_REQUIRED_VALUE_MISSING", column.columnNm() + " is required");
            }
            target.put(column.columnNm(), normalized);
        });
    }

    private Object normalizeRequestValue(ColumnDefinition column, Object value) {
        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            if ("yn".equals(column.dataTpCd())) {
                return normalizeYn(trimmed);
            }
            return trimmed;
        }
        if (value instanceof Boolean bool && "yn".equals(column.dataTpCd())) {
            return bool ? "Y" : "N";
        }
        return value;
    }

    private String normalizeYn(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("TRUE".equals(normalized) || "1".equals(normalized)) {
            return "Y";
        }
        if ("FALSE".equals(normalized) || "0".equals(normalized)) {
            return "N";
        }
        return normalized.startsWith("Y") ? "Y" : "N";
    }

    private void fillRequiredDefaults(RecordDefinition definition, Map<String, Object> values) {
        for (String columnNm : definition.requiredInsertColumns()) {
            if (values.containsKey(columnNm) && values.get(columnNm) != null) {
                continue;
            }
            Object defaultValue = defaultValue(definition, columnNm);
            if (defaultValue == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_RECORD_REQUIRED_VALUE_MISSING", columnNm + " is required");
            }
            values.put(columnNm, defaultValue);
        }
    }

    private Object defaultValue(RecordDefinition definition, String columnNm) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        String ym = today.format(YM_FORMATTER);
        String key = now.format(TS_KEY_FORMATTER);
        return switch (columnNm) {
            case "WORK_DT", "STR_DT", "END_DT", "EXP_DT", "PURCHASE_DT", "PAYMENT_DT", "HOLI_DT" -> today;
            case "START_TM", "STR_TM" -> LocalTime.of(9, 0);
            case "END_TM" -> LocalTime.of(18, 0);
            case "EXP_YEAR", "TXN_YEAR", "APPLY_YEAR" -> Integer.toString(today.getYear());
            case "EXP_MONTH", "TXN_MONTH" -> "%02d".formatted(today.getMonthValue());
            case "PERIOD_YM", "OPEXP_YM", "START_YM" -> ym;
            case "ACC_CD", "LEGACY_EXP_ACC_CD" -> "ACC" + key;
            case "ACC_NM" -> "New account";
            case "LEGACY_ACCOUNT_ID" -> "CORP" + key;
            case "ACCOUNT_NO" -> "TEMP-" + key;
            case "CTRY_CD" -> "KR";
            case "CTRY_NM_LOC" -> "Korea";
            case "CITY_CD" -> "SEOUL";
            case "CITY_NM" -> "Seoul";
            case "CURR_CD" -> "KRW";
            case "ASSET_NM" -> "New asset";
            case "ASSET_TP_CD" -> "GEN";
            case "OPEXP_NM" -> "New operating expense";
            case "OPSALES_NM" -> "New fixed sale";
            case "TEMPLATE_NM" -> "New financial statement";
            case "HOLI_NM" -> "New holiday";
            case "FROM_NM", "TO_NM", "CUSTOMER_NM" -> "";
            case "APRV_STS_CD" -> "SV";
            case "STS_CD" -> "ACTIVE";
            case "USE_YN" -> "Y";
            default -> definition.fixedValues().get(columnNm);
        };
    }

    private Long resolveCompanyId(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.coId() != null) {
            return authenticatedUser.coId();
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT ID FROM TB_CO WHERE TENANT_ID = :tenantId ORDER BY ID LIMIT 1",
                    new MapSqlParameterSource("tenantId", authenticatedUser.tenantId()),
                    Long.class
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BUSINESS_COMPANY_REQUIRED", "Company is required");
        }
    }

    private MapSqlParameterSource baseParams(AuthenticatedUser authenticatedUser) {
        return new MapSqlParameterSource()
                .addValue("tenantId", authenticatedUser.tenantId())
                .addValue("coId", authenticatedUser.coId())
                .addValue("usrId", authenticatedUser.usrId());
    }

    private String actor(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.lgnId() != null && !authenticatedUser.lgnId().isBlank()) {
            return authenticatedUser.lgnId();
        }
        return authenticatedUser.usrId() == null ? "SYSTEM" : "USR:" + authenticatedUser.usrId();
    }

    private RecordDefinition requireDefinition(String moduleCd) {
        RecordDefinition definition = DEFINITIONS.get(normalizeModuleCd(moduleCd));
        if (definition == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "BUSINESS_MODULE_NOT_FOUND", "Business module not found");
        }
        return definition;
    }

    private PageSpec normalizePage(BusinessRecordQuery query) {
        int page = query == null ? 0 : Math.max(query.page(), 0);
        int size = query == null || query.size() <= 0 ? DEFAULT_PAGE_SIZE : Math.min(query.size(), MAX_PAGE_SIZE);
        return new PageSpec(page, size);
    }

    private String normalizeModuleCd(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeYearMonth(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        String compact = trimmed.replace("-", "");
        if (compact.length() == 6 && compact.chars().allMatch(Character::isDigit)) {
            return compact.substring(0, 4) + "-" + compact.substring(4);
        }
        return null;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Map<String, RecordDefinition> buildDefinitions() {
        Map<String, RecordDefinition> definitions = new LinkedHashMap<>();
        put(definitions, def("REQ_WORK_TIME", "TB_WORK_TIME_ENTRY", ACTIVE_CO_SCOPE,
                "DATE_FORMAT(WORK_DT, '%Y-%m') = :yearMonth", "WORK_DT DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("WORK_DT", "근무일", "date", true, true, true, true),
                        col("START_TM", "시작", "time", true, true, true, true),
                        col("END_TM", "종료", "time", true, true, false, true),
                        col("APRV_STS_CD", "상태", "code", true, true, false, true),
                        col("SRC_CD", "출처", "code", true, false, false, true),
                        col("NOTE", "메모", "text", true, true, false, true),
                        hidden("STS_CD"),
                        hidden("TENANT_ID"),
                        hidden("CO_ID")
                ), "WORK_DT"));

        put(definitions, expDoc("REQ_EXPENSE", "COALESCE(EXP_TP_CD, '') NOT IN ('PO','SO','PAY','OPEX') AND BIZ_TRIP_ID IS NULL", map("EXP_TP_CD", "EXP")));
        put(definitions, expDoc("CLAIM_EXPENSE", "BIZ_TRIP_ID IS NULL", map("EXP_TP_CD", "EXP")));
        put(definitions, expDoc("CLAIM_TRAVEL_EXP", "BIZ_TRIP_ID IS NOT NULL", map("EXP_TP_CD", "TRVL")));
        put(definitions, expDoc("RPT_MONTHLY_EXP", "1 = 1", map(), false, false, false));
        put(definitions, expDoc("FIN_TXN_PURCHASE", "EXP_TP_CD = 'PO'", map("EXP_TP_CD", "PO")));
        put(definitions, expDoc("FIN_TXN_SALES", "EXP_TP_CD = 'SO'", map("EXP_TP_CD", "SO")));
        put(definitions, expDoc("FIN_MGT_EXPENSE", "1 = 1", map()));

        put(definitions, def("REQ_TRIP", "TB_BIZ_TRIP", ACTIVE_CO_SCOPE,
                "DATE_FORMAT(COALESCE(STR_DT, CRT_DTM), '%Y-%m') = :yearMonth", "COALESCE(STR_DT, CRT_DTM) DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("FROM_NM", "출발지", "text", true, true, true, true),
                        col("TO_NM", "도착지", "text", true, true, true, true),
                        col("CUSTOMER_NM", "고객", "text", true, true, false, true),
                        col("STR_DT", "시작일", "date", true, true, false, true),
                        col("END_DT", "종료일", "date", true, true, false, true),
                        col("APRV_STS_CD", "상태", "code", true, true, false, true),
                        col("BIZ_TRIP_RSN_CD", "사유", "code", true, true, false, true),
                        col("TRIP_EXP_INCL_YN", "출장비", "yn", true, true, false, true),
                        col("TRIP_FLAT_INCL_YN", "정액", "yn", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                ), "FROM_NM", "TO_NM", "CUSTOMER_NM"));

        put(definitions, def("REQ_LEAVE", "TB_LEAVE_REQ", ACTIVE_CO_SCOPE,
                "DATE_FORMAT(COALESCE(STR_DT, CRT_DTM), '%Y-%m') = :yearMonth", "COALESCE(STR_DT, CRT_DTM) DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("VAC_TP_CD", "휴가유형", "code", true, true, false, true),
                        col("DAY_TP_CD", "일수유형", "code", true, true, false, true),
                        col("STR_DT", "시작일", "date", true, true, false, true),
                        col("END_DT", "종료일", "date", true, true, false, true),
                        col("USE_VAC_CNT", "사용일수", "decimal", true, true, false, true),
                        col("APRV_STS_CD", "상태", "code", true, true, false, true),
                        col("REQ_RSN_CNTS", "사유", "text", true, true, false, true),
                        hidden("REJ_RSN_CNTS"), hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                )));

        put(definitions, def("RPT_WORK_TIME", "TB_WORK_TIME_ENTRY", ACTIVE_CO_SCOPE,
                "DATE_FORMAT(WORK_DT, '%Y-%m') = :yearMonth", "WORK_DT DESC, ID DESC",
                false, false, false, map(), cols(
                        key(),
                        col("WORK_DT", "근무일", "date", true, false, true, true),
                        col("START_TM", "시작", "time", true, false, true, true),
                        col("END_TM", "종료", "time", true, false, true, true),
                        col("APRV_STS_CD", "상태", "code", true, false, true, true),
                        col("NOTE", "메모", "text", true, false, false, true)
                )));

        put(definitions, def("FIN_TXN_OPEX", "TB_OP_EXP", ACTIVE_CO_SCOPE,
                "REPLACE(COALESCE(OPEXP_YM, CONCAT(EXP_YEAR, EXP_MONTH), DATE_FORMAT(CRT_DTM, '%Y%m')), '-', '') = :yearMonthCompact",
                "COALESCE(OPEXP_YM, CONCAT(EXP_YEAR, EXP_MONTH), DATE_FORMAT(CRT_DTM, '%Y%m')) DESC, ID DESC",
                true, true, true, map("EXP_TP_CD", "OPEX"), opExpCols(), "OPEXP_NM"));
        put(definitions, def("FIN_MGT_FIXED_OPEX", "TB_OP_EXP", ACTIVE_CO_SCOPE,
                "REPLACE(COALESCE(OPEXP_YM, DATE_FORMAT(CRT_DTM, '%Y%m')), '-', '') = :yearMonthCompact",
                "COALESCE(OPEXP_YM, DATE_FORMAT(CRT_DTM, '%Y%m')) DESC, ID DESC",
                true, true, true, map(), opExpCols(), "OPEXP_NM"));
        put(definitions, def("FIN_TXN_CORP_ACC", "TB_CORP_TXN", ACTIVE_CO_SCOPE,
                "CONCAT(TXN_YEAR, '-', TXN_MONTH) = :yearMonth", "COALESCE(TXN_DTM, CRT_DTM) DESC, ID DESC",
                false, true, true, map(), cols(
                        key(),
                        col("LEGACY_TRANSACTION_ID", "거래키", "text", true, false, true, true),
                        col("TXN_TP_CD", "유형", "code", true, true, false, true),
                        col("WITHDRAW_AMT", "출금", "decimal", true, true, false, true),
                        col("DEPOSIT_AMT", "입금", "decimal", true, true, false, true),
                        col("BALANCE_AMT", "잔액", "decimal", true, true, false, true),
                        col("TXN_DTM", "거래일시", "datetime", true, true, false, true),
                        col("TXN_DESC", "내용", "text", true, true, false, true),
                        col("APRV_STS_CD", "승인상태", "code", true, true, false, true),
                        col("APRV_YN", "승인", "yn", true, true, false, true),
                        hidden("REJ_RSN_CNTS"), hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                )));
        put(definitions, def("FIN_MGT_BALANCE", "TB_CORP_TXN", ACTIVE_CO_SCOPE,
                "CONCAT(TXN_YEAR, '-', TXN_MONTH) = :yearMonth", "COALESCE(TXN_DTM, CRT_DTM) DESC, ID DESC",
                false, true, true, map(), definitions.get("FIN_TXN_CORP_ACC").columns()));
        put(definitions, def("FIN_TXN_PAYROLL", "TB_PAYROLL_HD", ACTIVE_CO_SCOPE,
                "CONCAT(EXP_YEAR, '-', EXP_MONTH) = :yearMonth", "EXP_YEAR DESC, EXP_MONTH DESC, ID DESC",
                true, true, true, map("EXP_TP_CD", "PAY"), cols(
                        key(),
                        col("EXP_YEAR", "연도", "text", true, true, true, true),
                        col("EXP_MONTH", "월", "text", true, true, true, true),
                        col("EXP_TP_CD", "유형", "code", true, true, false, true),
                        col("EXP_STS_CD", "처리상태", "code", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                ), "EXP_YEAR", "EXP_MONTH"));

        put(definitions, def("FIN_MGT_CLOSING", "TB_PERIOD_CLOSE", CO_SCOPE,
                "REPLACE(PERIOD_YM, '-', '') = :yearMonthCompact", "PERIOD_YM DESC, ID DESC",
                true, true, false, map(), cols(
                        key(),
                        col("PERIOD_YM", "기간", "text", true, true, true, true),
                        col("CLOSED_YN", "마감", "yn", true, true, true, true),
                        col("FINAL_YN", "최종", "yn", true, true, true, true),
                        col("NOTE", "메모", "text", true, true, false, true),
                        hidden("TENANT_ID"), hidden("CO_ID")
                ), "PERIOD_YM"));
        put(definitions, def("FIN_MGT_ASSET", "TB_ASSET", ACTIVE_CO_SCOPE,
                "REPLACE(START_YM, '-', '') = :yearMonthCompact", "START_YM DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("ASSET_NM", "자산명", "text", true, true, true, true),
                        col("ASSET_CD", "자산코드", "text", true, true, false, true),
                        col("ASSET_TP_CD", "자산유형", "code", true, true, true, true),
                        col("DEPR_MONTH_CNT", "상각개월", "number", true, true, false, true),
                        col("START_YM", "시작월", "text", true, true, true, true),
                        col("NET_AMT", "금액", "decimal", true, true, false, true),
                        col("PURCHASE_DT", "구매일", "date", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                ), "ASSET_NM", "ASSET_TP_CD", "START_YM"));
        put(definitions, def("FIN_MGT_FIXED_SALES", "TB_OP_SALES", ACTIVE_CO_SCOPE,
                "DATE_FORMAT(COALESCE(STR_DT, CRT_DTM), '%Y-%m') = :yearMonth", "COALESCE(STR_DT, CRT_DTM) DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("OPSALES_NM", "고정매출명", "text", true, true, true, true),
                        col("CYCLE_MONTH_CNT", "주기개월", "number", true, true, false, true),
                        col("MONTH_AMT", "월금액", "decimal", true, true, false, true),
                        col("STR_DT", "시작일", "date", true, true, false, true),
                        col("END_DT", "종료일", "date", true, true, false, true),
                        col("PAYMENT_DT", "지급일", "date", true, true, false, true),
                        col("FIXED_SALES_YN", "고정매출", "yn", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                ), "OPSALES_NM"));

        put(definitions, def("FIN_RPT_LEDGER", "VW_ACCOUNT_LEDGER",
                "TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId)",
                "TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId)",
                "CONCAT(EXP_YEAR, '-', EXP_MONTH) = :yearMonth", "EXP_YEAR DESC, EXP_MONTH DESC, EXP_DOC_ID DESC",
                false, false, false, false, true, map(), cols(
                        column("EXP_DOC_ID", "문서ID", "number", true, false, false, false, true),
                        column("EXP_LINE_ID", "라인ID", "number", true, false, false, false, true),
                        col("EXP_YEAR", "연도", "text", true, false, true, true),
                        col("EXP_MONTH", "월", "text", true, false, true, true),
                        col("EXP_DT", "일자", "date", true, false, true, true),
                        col("EXP_PLACE_NM", "거래처", "text", true, false, false, true),
                        col("ACC_CD", "계정", "text", true, false, true, true),
                        col("ACC_NM", "계정명", "text", true, false, true, true),
                        col("AMT", "금액", "decimal", true, false, true, true),
                        col("DRCR_CD", "차대", "code", true, false, false, true)
                )));
        put(definitions, def("FIN_RPT_ASSET", "TB_ASSET", ACTIVE_CO_SCOPE,
                "REPLACE(START_YM, '-', '') = :yearMonthCompact", "START_YM DESC, ID DESC",
                false, false, false, map(), definitions.get("FIN_MGT_ASSET").columns()));
        put(definitions, def("FIN_RPT_ANNUAL", "TB_FI_STMT_TEMPLATE", ACTIVE_CO_SCOPE,
                null, "ID DESC", false, false, false, map(), fiStmtCols()));

        put(definitions, def("MST_EMP", "TB_EMP_HR_PROFILE", ACTIVE_CO_SCOPE,
                null, "ID DESC", false, true, true, map(), cols(
                        key(),
                        col("LEGACY_EMP_ID", "직원키", "text", true, false, true, true),
                        col("RANK_CD", "직급", "code", true, true, false, true),
                        col("POSITION_CD", "직책", "code", true, true, false, true),
                        col("SEX_CD", "성별", "code", true, true, false, true),
                        col("BIRTH_DT", "생년월일", "date", true, true, false, true),
                        col("WORK_STR_TM", "근무시작", "time", true, true, false, true),
                        col("WORK_END_TM", "근무종료", "time", true, true, false, true),
                        col("STD_VAC_CNT", "기본휴가", "decimal", true, true, false, true),
                        col("CURR_VAC_CNT", "현재휴가", "decimal", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                )));
        put(definitions, def("MST_DEPT", "TB_DEPT", ACTIVE_CO_SCOPE, ACTIVE_CO_SCOPE,
                null, "DEPT_CD ASC, ID ASC", false, false, false, false, true, map(), cols(
                        key(),
                        col("DEPT_CD", "부서코드", "text", true, false, true, true),
                        col("DEPT_NM", "부서명", "text", true, false, true, true),
                        col("DEPT_LVL_NO", "레벨", "number", true, false, false, true),
                        col("STS_CD", "상태", "code", true, false, false, true)
                )));
        put(definitions, def("MST_ACC", "TB_FIN_ACC", ACTIVE_USE_CO_SCOPE,
                null, "DSP_ORD ASC, ACC_CD ASC", true, true, true, map(), finAccCols(), "ACC_CD", "ACC_NM", "LEGACY_EXP_ACC_CD"));
        put(definitions, def("MST_CORP_ACC", "TB_CORP_ACC", ACTIVE_USE_CO_SCOPE,
                null, "ID DESC", true, true, true, map(), corpAccCols(), "LEGACY_ACCOUNT_ID", "ACCOUNT_NO"));
        put(definitions, def("MST_DAILY_ALLOWANCE", "TB_DAILY_ALLOWANCE", DAILY_ALLOWANCE_SCOPE, DAILY_ALLOWANCE_MUTATION_SCOPE,
                null, "DSP_ORD ASC, CTRY_CD ASC, CITY_CD ASC", true, true, true, true, false, map(), dailyAllowanceCols(),
                "CTRY_CD", "CTRY_NM_LOC", "CITY_CD", "CITY_NM", "CURR_CD"));
        put(definitions, def("MST_CORP_CAL", "TB_CO_HOLI", ACTIVE_USE_CO_SCOPE,
                "APPLY_YEAR = :yearMonthCompact OR APPLY_YEAR = LEFT(:yearMonthCompact, 4)", "HOLI_DT DESC, ID DESC",
                true, true, true, map(), cols(
                        key(),
                        col("HOLI_NM", "휴일명", "text", true, true, true, true),
                        col("APPLY_YEAR", "적용연도", "text", true, true, true, true),
                        col("HOLI_DT", "휴일", "date", true, true, true, true),
                        col("ANNUAL_YN", "매년", "yn", true, true, false, true),
                        col("USE_YN", "사용", "yn", true, true, false, true),
                        hidden("TENANT_ID"), hidden("CO_ID")
                ), "HOLI_NM", "APPLY_YEAR", "HOLI_DT"));
        put(definitions, def("MST_ACC_MAPPING", "TB_FIN_CARRYOVER_MAP", ACTIVE_USE_CO_SCOPE,
                null, "CARRYOVER_CD ASC, ID ASC", false, true, true, map(), cols(
                        key(),
                        col("CARRYOVER_CD", "이월코드", "text", true, true, true, true),
                        col("ACC_ID", "계정ID", "number", true, true, true, true),
                        col("USE_YN", "사용", "yn", true, true, false, true),
                        hidden("TENANT_ID"), hidden("CO_ID")
                )));
        put(definitions, def("MST_FI_STMT", "TB_FI_STMT_TEMPLATE", ACTIVE_CO_SCOPE,
                null, "ID DESC", true, true, true, map(), fiStmtCols(), "TEMPLATE_NM"));
        return Map.copyOf(definitions);
    }

    private static Map<String, List<RelationDefinition>> buildRelations() {
        Map<String, List<RelationDefinition>> relations = new LinkedHashMap<>();
        String childScope = "TENANT_ID = :tenantId AND (:coId IS NULL OR CO_ID = :coId)";
        String activeChildScope = childScope + " AND COALESCE(STS_CD, 'ACTIVE') <> 'DELETED'";

        List<RelationDefinition> expDocRelations = List.of(
                rel("TB_EXP_LINE", "Expense lines", "EXP_DOC_ID = :recordId AND " + activeChildScope,
                        "ID DESC", "ID", "EXP_DT", "EXP_TP_CD", "EXP_PLACE_NM", "EXP_TOTAL_AMT", "EXP_PAID_AMT", "EXP_UNPAID_AMT", "MAP_STS_CD"),
                rel("TB_EXP_RATE", "Tax rates",
                        "EXP_LINE_ID IN (SELECT ID FROM TB_EXP_LINE WHERE EXP_DOC_ID = :recordId AND " + activeChildScope + ") AND " + childScope,
                        "ID DESC", "ID", "EXP_LINE_ID", "EXP_EXCL_AMT", "EXP_TAX_AMT", "EXP_INCL_AMT", "EXP_RATE"),
                rel("TB_EXP_APRV", "Approvals", "EXP_DOC_ID = :recordId AND " + activeChildScope,
                        "APRV_LVL_NO ASC, ID ASC", "ID", "APRV_TP_CD", "APRV_LVL_NO", "APRV_STS_CD", "APRV_YN", "FINAL_YN"),
                rel("TB_EXP_ATTACH", "Attachments",
                        "EXP_LINE_ID IN (SELECT ID FROM TB_EXP_LINE WHERE EXP_DOC_ID = :recordId AND " + activeChildScope + ") AND " + childScope,
                        "ID DESC", "ID", "EXP_LINE_ID", "ORIG_FILE_NM", "FILE_EXT_NM", "FILE_SIZE", "FILE_SRC_CD")
        );
        for (String moduleCd : List.of(
                "REQ_EXPENSE", "CLAIM_EXPENSE", "CLAIM_TRAVEL_EXP", "RPT_MONTHLY_EXP",
                "FIN_TXN_PURCHASE", "FIN_TXN_SALES", "FIN_MGT_EXPENSE"
        )) {
            relations.put(moduleCd, expDocRelations);
        }

        relations.put("REQ_TRIP", List.of(
                rel("TB_BIZ_TRIP_APRV", "Approvals", "BIZ_TRIP_ID = :recordId AND " + activeChildScope,
                        "APRV_LVL_NO ASC, ID ASC", "ID", "APRV_TP_CD", "APRV_LVL_NO", "APRV_STS_CD", "APRV_YN", "FINAL_YN")
        ));
        relations.put("REQ_LEAVE", List.of(
                rel("TB_LEAVE_APRV", "Approvals", "LEAVE_REQ_ID = :recordId AND " + activeChildScope,
                        "APRV_LVL_NO ASC, ID ASC", "ID", "APRV_TP_CD", "APRV_LVL_NO", "APRV_STS_CD", "APRV_YN", "FINAL_YN")
        ));
        List<RelationDefinition> corpTxnRelations = List.of(
                rel("TB_BANK_EXP_MAP", "Expense mappings", "CORP_TXN_ID = :recordId AND " + activeChildScope,
                        "ID DESC", "ID", "EXP_DOC_ID", "EXP_LINE_ID", "PAID_CD", "PAID_AMT", "UNPAID_AMT", "MAP_STS_CD"),
                rel("TB_CORP_TXN_APRV", "Approvals", "CORP_TXN_ID = :recordId AND " + activeChildScope,
                        "APRV_LVL_NO ASC, ID ASC", "ID", "APRV_TP_CD", "APRV_LVL_NO", "APRV_STS_CD", "APRV_YN", "FINAL_YN")
        );
        relations.put("FIN_TXN_CORP_ACC", corpTxnRelations);
        relations.put("FIN_MGT_BALANCE", corpTxnRelations);
        relations.put("MST_CORP_ACC", List.of(
                rel("TB_CORP_TXN", "Transactions", "CORP_ACC_ID = :recordId AND " + activeChildScope,
                        "COALESCE(TXN_DTM, CRT_DTM) DESC, ID DESC", "ID", "TXN_TP_CD", "WITHDRAW_AMT", "DEPOSIT_AMT", "BALANCE_AMT", "TXN_DTM", "APRV_STS_CD")
        ));
        List<RelationDefinition> opExpRelations = List.of(
                rel("TB_OP_EXP_SCHD", "Schedules", "OP_EXP_ID = :recordId AND " + activeChildScope,
                        "EXP_YM DESC, ID DESC", "ID", "EXP_YM", "DUE_DT", "SCHD_AMT", "PROC_YN", "PROC_DT"),
                rel("TB_OP_EXP_RATE", "Tax rates", "OP_EXP_ID = :recordId AND " + childScope,
                        "ID DESC", "ID", "EXP_EXCL_AMT", "EXP_TAX_AMT", "EXP_INCL_AMT", "EXP_RATE"),
                rel("TB_OP_EXP_ATTACH", "Attachments", "OP_EXP_ID = :recordId AND " + childScope,
                        "ID DESC", "ID", "ORIG_FILE_NM", "FILE_EXT_NM", "FILE_SIZE", "FILE_SRC_CD")
        );
        relations.put("FIN_TXN_OPEX", opExpRelations);
        relations.put("FIN_MGT_FIXED_OPEX", opExpRelations);
        List<RelationDefinition> assetRelations = List.of(
                rel("TB_ASSET_SCHD", "Depreciation schedules", "ASSET_ID = :recordId AND " + activeChildScope,
                        "SCHD_YM DESC, ID DESC", "ID", "SCHD_YM", "DEPR_AMT", "REMAINING_AMT", "PROC_YN"),
                rel("TB_ASSET_ATTACH", "Attachments", "ASSET_ID = :recordId AND " + childScope,
                        "ID DESC", "ID", "ORIG_FILE_NM", "FILE_EXT_NM", "FILE_SIZE")
        );
        relations.put("FIN_MGT_ASSET", assetRelations);
        relations.put("FIN_RPT_ASSET", assetRelations);
        relations.put("FIN_MGT_FIXED_SALES", List.of(
                rel("TB_OP_SALES_SCHD", "Schedules", "OP_SALES_ID = :recordId AND " + activeChildScope,
                        "EXP_YM DESC, ID DESC", "ID", "EXP_YM", "DUE_DT", "SCHD_AMT", "PROC_YN", "PROC_DT"),
                rel("TB_OP_SALES_ATTACH", "Attachments", "OP_SALES_ID = :recordId AND " + childScope,
                        "ID DESC", "ID", "ORIG_FILE_NM", "FILE_EXT_NM", "FILE_SIZE")
        ));
        relations.put("FIN_TXN_PAYROLL", List.of(
                rel("TB_PAYROLL_LINE", "Payroll lines", "PAYROLL_HD_ID = :recordId AND " + activeChildScope,
                        "ID DESC", "ID", "EXP_DT", "PAY_ITEM_NM", "EXP_ACC_CD", "EXP_TOTAL_AMT", "SI_ER_AMT", "EXP_PAID_AMT")
        ));
        relations.put("MST_ACC", List.of(
                rel("TB_FIN_CARRYOVER_MAP", "Carryover mappings", "ACC_ID = :recordId AND " + childScope + " AND COALESCE(USE_YN, 'Y') = 'Y'",
                        "ID DESC", "ID", "CARRYOVER_CD", "USE_YN")
        ));
        List<RelationDefinition> fiStmtRelations = List.of(
                rel("TB_FI_STMT_LINE", "Statement lines", "TEMPLATE_ID = :recordId AND " + activeChildScope,
                        "DSP_ORD ASC, ID ASC", "ID", "ROOT_KIND_CD", "STMT_SCOPE_CD", "LINE_NM", "DSP_ORD", "STS_CD"),
                rel("TB_FI_STMT_LINE_ACC", "Line accounts",
                        "LINE_ID IN (SELECT ID FROM TB_FI_STMT_LINE WHERE TEMPLATE_ID = :recordId AND " + activeChildScope + ") AND " + activeChildScope,
                        "ID ASC", "ID", "LINE_ID", "ACC_ID", "LEGACY_ACC_CD", "STS_CD")
        );
        relations.put("MST_FI_STMT", fiStmtRelations);
        relations.put("FIN_RPT_ANNUAL", fiStmtRelations);
        return Map.copyOf(relations);
    }

    private static RecordDefinition expDoc(String moduleCd, String extraWhereSql, Map<String, Object> fixedValues) {
        return expDoc(moduleCd, extraWhereSql, fixedValues, true, true, true);
    }

    private static RecordDefinition expDoc(
            String moduleCd,
            String extraWhereSql,
            Map<String, Object> fixedValues,
            boolean creatable,
            boolean editable,
            boolean deletable
    ) {
        return def(moduleCd, "TB_EXP_DOC", ACTIVE_CO_SCOPE + " AND " + extraWhereSql,
                "CONCAT(EXP_YEAR, '-', EXP_MONTH) = :yearMonth", "EXP_YEAR DESC, EXP_MONTH DESC, ID DESC",
                creatable, editable, deletable, fixedValues, cols(
                        key(),
                        col("EXP_YEAR", "연도", "text", true, true, true, true),
                        col("EXP_MONTH", "월", "text", true, true, true, true),
                        col("EXP_TP_CD", "유형", "code", true, true, false, true),
                        col("APRV_STS_CD", "상태", "code", true, true, false, true),
                        col("APRV_CURR_LVL_NO", "승인단계", "number", false, true, false, true),
                        col("APRV_TOT_LVL_NO", "총단계", "number", false, true, false, true),
                        col("REJ_RSN_CNTS", "반려사유", "text", true, true, false, true),
                        hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
                ), "EXP_YEAR", "EXP_MONTH");
    }

    private static List<ColumnDefinition> opExpCols() {
        return cols(
                key(),
                col("OPEXP_NM", "운영비명", "text", true, true, true, true),
                col("OPEXP_YM", "기준월", "text", true, true, false, true),
                col("MONTH_AMT", "월금액", "decimal", true, true, false, true),
                col("CYCLE_MONTH_CNT", "주기개월", "number", true, true, false, true),
                col("STR_DT", "시작일", "date", true, true, false, true),
                col("END_DT", "종료일", "date", true, true, false, true),
                col("PAYMENT_DT", "지급일", "date", true, true, false, true),
                col("APRV_STS_CD", "승인상태", "code", true, true, false, true),
                col("PROC_YN", "처리", "yn", true, true, false, true),
                col("NOTE", "메모", "text", true, true, false, true),
                hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
        );
    }

    private static List<ColumnDefinition> finAccCols() {
        return cols(
                key(),
                col("ACC_CD", "계정코드", "text", true, true, true, true),
                col("ACC_NM", "계정명", "text", true, true, true, true),
                col("PURCHASE_YN", "매입", "yn", true, true, false, true),
                col("SALES_YN", "매출", "yn", true, true, false, true),
                col("EXPENSE_YN", "비용", "yn", true, true, false, true),
                col("FIXED_EXPENSE_YN", "고정비", "yn", true, true, false, true),
                col("DRCR_CD", "차대", "code", true, true, false, true),
                col("CARRYOVER_CD", "이월", "code", true, true, false, true),
                col("DSP_ORD", "순서", "number", false, true, false, true),
                col("USE_YN", "사용", "yn", true, true, false, true),
                hidden("LEGACY_EXP_ACC_CD"), hidden("TENANT_ID"), hidden("CO_ID")
        );
    }

    private static List<ColumnDefinition> corpAccCols() {
        return cols(
                key(),
                col("ACCOUNT_NO", "계좌번호", "text", true, true, true, true),
                col("BANK_CD", "은행코드", "code", true, true, false, true),
                col("BANK_NM", "은행명", "text", true, true, false, true),
                col("IBAN", "IBAN", "text", true, true, false, true),
                col("BIC_CD", "BIC", "text", true, true, false, true),
                col("CURR_CD", "통화", "text", true, true, false, true),
                col("BALANCE_AMT", "잔액", "decimal", true, true, false, true),
                col("USE_YN", "사용", "yn", true, true, false, true),
                hidden("LEGACY_ACCOUNT_ID"), hidden("TENANT_ID"), hidden("CO_ID")
        );
    }

    private static List<ColumnDefinition> dailyAllowanceCols() {
        return cols(
                key(),
                col("CTRY_CD", "국가코드", "text", true, true, true, true),
                col("CTRY_NM_LOC", "국가명", "text", true, true, true, true),
                col("CITY_CD", "도시코드", "text", true, true, true, true),
                col("CITY_NM", "도시명", "text", true, true, true, true),
                col("CURR_CD", "통화", "text", true, true, true, true),
                col("ALL_DAY_AMT", "일비", "decimal", true, true, false, true),
                col("HALF_DAY_AMT", "반일", "decimal", true, true, false, true),
                col("FLAT_AMT", "정액", "decimal", true, true, false, true),
                col("USE_YN", "사용", "yn", true, true, false, true),
                hidden("TENANT_ID")
        );
    }

    private static List<ColumnDefinition> fiStmtCols() {
        return cols(
                key(),
                col("TEMPLATE_NM", "템플릿명", "text", true, true, true, true),
                col("TEMPLATE_DISP_NM", "표시명", "text", true, true, false, true),
                col("ACTIVE_YN", "활성", "yn", true, true, false, true),
                col("LOCK_YN", "잠금", "yn", true, true, false, true),
                col("SAMPLE_YN", "샘플", "yn", true, true, false, true),
                hidden("STS_CD"), hidden("TENANT_ID"), hidden("CO_ID")
        );
    }

    private static RecordDefinition def(
            String moduleCd,
            String tableNm,
            String queryWhereSql,
            String periodWhereSql,
            String orderBySql,
            boolean creatable,
            boolean editable,
            boolean deletable,
            Map<String, Object> fixedValues,
            List<ColumnDefinition> columns,
            String... requiredInsertColumns
    ) {
        return def(moduleCd, tableNm, queryWhereSql, queryWhereSql, periodWhereSql, orderBySql,
                creatable, editable, deletable, true, false, fixedValues, columns, requiredInsertColumns);
    }

    private static RecordDefinition def(
            String moduleCd,
            String tableNm,
            String queryWhereSql,
            String mutationWhereSql,
            String periodWhereSql,
            String orderBySql,
            boolean creatable,
            boolean editable,
            boolean deletable,
            boolean tenantScoped,
            boolean viewOnly,
            Map<String, Object> fixedValues,
            List<ColumnDefinition> columns,
            String... requiredInsertColumns
    ) {
        boolean companyScoped = columns.stream().anyMatch(column -> "CO_ID".equals(column.columnNm()));
        Set<String> required = new LinkedHashSet<>();
        required.addAll(Arrays.asList(requiredInsertColumns));
        fixedValues.keySet().forEach(required::add);
        return new RecordDefinition(
                moduleCd,
                tableNm,
                "ID",
                queryWhereSql,
                mutationWhereSql,
                periodWhereSql,
                orderBySql,
                creatable && !viewOnly,
                editable && !viewOnly,
                deletable && !viewOnly,
                tenantScoped,
                companyScoped,
                Map.copyOf(fixedValues),
                columns,
                Set.copyOf(required)
        );
    }

    private static void put(Map<String, RecordDefinition> definitions, RecordDefinition definition) {
        definitions.put(definition.moduleCd(), definition);
    }

    private static List<ColumnDefinition> cols(ColumnDefinition... columns) {
        List<ColumnDefinition> result = new ArrayList<>();
        int order = 10;
        for (ColumnDefinition column : columns) {
            result.add(column.withOrder(order));
            order += 10;
        }
        return List.copyOf(result);
    }

    private static ColumnDefinition key() {
        return column("ID", "ID", "number", false, false, true, false, true);
    }

    private static ColumnDefinition hidden(String columnNm) {
        return column(columnNm, columnNm, "text", false, false, false, false, false);
    }

    private static ColumnDefinition col(
            String columnNm,
            String label,
            String dataTpCd,
            boolean searchable,
            boolean writable,
            boolean required,
            boolean visible
    ) {
        return column(columnNm, label, dataTpCd, searchable, writable, required, visible, false);
    }

    private static ColumnDefinition column(
            String columnNm,
            String label,
            String dataTpCd,
            boolean searchable,
            boolean writable,
            boolean required,
            boolean visible,
            boolean key
    ) {
        return new ColumnDefinition(columnNm, label, dataTpCd, searchable, writable, required, visible, key, 0);
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("pairs must be even");
        }
        for (int i = 0; i < pairs.length; i += 2) {
            result.put((String) pairs[i], pairs[i + 1]);
        }
        return result;
    }

    private static RelationDefinition rel(
            String tableNm,
            String label,
            String whereSql,
            String orderBySql,
            String... columns
    ) {
        return new RelationDefinition(tableNm, label, whereSql, orderBySql, List.of(columns));
    }

    private record PageSpec(int page, int size) {
    }

    private record RecordDefinition(
            String moduleCd,
            String tableNm,
            String idColumn,
            String queryWhereSql,
            String mutationWhereSql,
            String periodWhereSql,
            String orderBySql,
            boolean creatable,
            boolean editable,
            boolean deletable,
            boolean tenantScoped,
            boolean companyScoped,
            Map<String, Object> fixedValues,
            List<ColumnDefinition> columns,
            Set<String> requiredInsertColumns
    ) {
        boolean hasColumn(String columnNm) {
            return columns.stream().anyMatch(column -> column.columnNm().equals(columnNm));
        }
    }

    private record RelationDefinition(
            String tableNm,
            String label,
            String whereSql,
            String orderBySql,
            List<String> columns
    ) {
    }

    private record ColumnDefinition(
            String columnNm,
            String label,
            String dataTpCd,
            boolean searchable,
            boolean writable,
            boolean required,
            boolean visible,
            boolean key,
            int order
    ) {
        ColumnDefinition withOrder(int nextOrder) {
            return new ColumnDefinition(columnNm, label, dataTpCd, searchable, writable, required, visible, key, nextOrder);
        }

        BusinessRecordColumnResponse toResponse(Locale locale) {
            return new BusinessRecordColumnResponse(columnNm, label, dataTpCd, key, visible, writable, required, order);
        }
    }
}

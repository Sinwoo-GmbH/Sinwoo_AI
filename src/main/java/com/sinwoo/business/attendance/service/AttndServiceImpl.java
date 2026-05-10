package com.sinwoo.business.attendance.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.business.attendance.domain.AttndRec;
import com.sinwoo.business.attendance.domain.HoliCache;
import com.sinwoo.business.attendance.dto.AttndCalDayResponse;
import com.sinwoo.business.attendance.dto.AttndHoliFocusResponse;
import com.sinwoo.business.attendance.dto.AttndManualEntryRequest;
import com.sinwoo.business.attendance.dto.AttndMonthSummaryResponse;
import com.sinwoo.business.attendance.dto.AttndTodayResponse;
import com.sinwoo.business.attendance.dto.AttndWidgetPolicyResponse;
import com.sinwoo.business.attendance.dto.AttndWidgetResponse;
import com.sinwoo.business.attendance.repository.AttndRecRepository;
import com.sinwoo.business.attendance.repository.HoliCacheRepository;
import com.sinwoo.business.attendance.support.AttndProperties;
import com.sinwoo.business.attendance.support.AttndStatusCd;
import com.sinwoo.platform.code.service.CommonCdService;
import com.sinwoo.platform.code.support.CommonCdGroupCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.util.CommonBizConst;
import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.emp.domain.Emp;
import com.sinwoo.platform.emp.repository.EmpRepository;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.worklocation.domain.WorkLoc;
import com.sinwoo.platform.worklocation.repository.WorkLocRepository;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttndServiceImpl implements AttndService {

    private static final Logger log = LoggerFactory.getLogger(AttndServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AttndRecRepository attndRecRepository;
    private final HoliCacheRepository holiCacheRepository;
    private final EmpRepository empRepository;
    private final CoRepository coRepository;
    private final WorkLocRepository workLocRepository;
    private final TenantRepository tenantRepository;
    private final CommonCdService commonCdService;
    private final ObjectMapper objectMapper;
    private final AttndProperties attndProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public AttndWidgetResponse getAttndWidget(
            AuthenticatedUsr authenticatedUsr,
            YearMonth yearMonth,
            Locale locale
    ) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        ZoneId bizZoneId = attndProperties.bizZoneId();
        YearMonth targetMonth = yearMonth == null ? YearMonth.now(bizZoneId) : yearMonth;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();
        LocalDate gridStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = monthEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate today = LocalDate.now(bizZoneId);

        HolidayContext holidayContext = resolveHolidayContext(user);
        ensureHoliCache(targetMonth.getYear(), holidayContext.ctryCd());

        Map<LocalDate, HoliCache> holidayByDate = buildHolidayMap(
                holiCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                        holidayContext.ctryCd(),
                        resolveRegionScope(holidayContext.regionCd()),
                        gridStart,
                        gridEnd
                )
        );

        Map<LocalDate, AttndRec> attndByDate = attndRecRepository
                .findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtAsc(
                        user.tenantId(),
                        user.usrId(),
                        gridStart,
                        gridEnd
                )
                .stream()
                .collect(LinkedHashMap::new, (map, record) -> map.put(record.getAttndDt(), record), Map::putAll);

        List<AttndCalDayResponse> dayList = new ArrayList<>();
        LocalDate cursor = gridStart;
        while (!cursor.isAfter(gridEnd)) {
            HoliCache holiday = holidayByDate.get(cursor);
            AttndRec record = attndByDate.get(cursor);
            dayList.add(new AttndCalDayResponse(
                    cursor.format(DATE_FORMAT),
                    cursor.getDayOfMonth(),
                    cursor.getMonthValue() == targetMonth.getMonthValue(),
                    cursor.equals(today),
                    isWeekend(cursor),
                    holiday != null,
                    holiday == null ? null : resolveHolidayName(holiday, locale),
                    record == null ? AttndStatusCd.NONE : record.getAttndStsCd(),
                    formatTime(record == null ? null : record.getChkinDtm()),
                    formatTime(record == null ? null : record.getChkoutDtm())
            ));
            cursor = cursor.plusDays(1);
        }

        AttndMonthSummaryResponse summary = new AttndMonthSummaryResponse(
                (int) attndByDate.values().stream()
                        .filter(record -> record.getAttndDt() != null)
                        .filter(record -> YearMonth.from(record.getAttndDt()).equals(targetMonth))
                        .filter(record -> record.getChkinDtm() != null || record.getChkoutDtm() != null)
                        .count(),
                (int) holidayByDate.values().stream()
                        .filter(holiday -> holiday.getHoliDt() != null)
                        .filter(holiday -> YearMonth.from(holiday.getHoliDt()).equals(targetMonth))
                        .count(),
                (int) monthStart.datesUntil(monthEnd.plusDays(1))
                        .filter(this::isWeekend)
                        .count()
        );

        AttndHoliFocusResponse holidayFocus = holidayByDate.values().stream()
                .filter(holiday -> holiday.getHoliDt() != null)
                .filter(holiday -> YearMonth.from(holiday.getHoliDt()).equals(targetMonth))
                .sorted((left, right) -> {
                    boolean leftUpcoming = !left.getHoliDt().isBefore(today);
                    boolean rightUpcoming = !right.getHoliDt().isBefore(today);
                    if (leftUpcoming != rightUpcoming) {
                        return leftUpcoming ? -1 : 1;
                    }
                    return left.getHoliDt().compareTo(right.getHoliDt());
                })
                .map(holiday -> new AttndHoliFocusResponse(
                        holiday.getHoliDt().format(DATE_FORMAT),
                        resolveHolidayName(holiday, locale)
                ))
                .findFirst()
                .orElse(null);

        return new AttndWidgetResponse(
                targetMonth.format(MONTH_FORMAT),
                holidayContext.regionCd(),
                new AttndWidgetPolicyResponse(
                        attndProperties.bizTmznId(),
                        attndProperties.dfltChkinTm(),
                        attndProperties.dfltChkoutTm(),
                        CommonCdGroupCd.ATTND_FLAG,
                        AttndStatusCd.CHECKED_IN,
                        AttndStatusCd.CHECKED_OUT,
                        AttndStatusCd.LEAVE,
                        AttndStatusCd.BUSINESS_TRIP
                ),
                toTodayResponse(today, attndByDate.get(today), holidayByDate.get(today), locale),
                summary,
                holidayFocus,
                dayList
        );
    }

    @Override
    @Transactional
    public AttndTodayResponse checkIn(AuthenticatedUsr authenticatedUsr, Locale locale) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        LocalDate today = LocalDate.now(attndProperties.bizZoneId());
        OffsetDateTime now = OffsetDateTime.now(attndProperties.bizZoneId());
        AttndContext attndContext = resolveAttndContext(user);
        AttndRec attndRec = attndRecRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), today)
                .orElseGet(() -> AttndRec.createCheckedIn(
                        user.tenantId(),
                        attndContext.coId(),
                        user.usrId(),
                        attndContext.empId(),
                        today,
                        now
                ));

        if (attndRec.getId() != null) {
            attndRec.applyCheckIn(now);
        }

        AttndRec saved = attndRecRepository.save(attndRec);
        HoliCache holiday = findHolidayForDate(resolveHolidayContext(user), today);
        return toTodayResponse(today, saved, holiday, locale);
    }

    @Override
    @Transactional
    public AttndTodayResponse checkOut(AuthenticatedUsr authenticatedUsr, Locale locale) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        LocalDate today = LocalDate.now(attndProperties.bizZoneId());
        OffsetDateTime now = OffsetDateTime.now(attndProperties.bizZoneId());
        resolveAttndContext(user);
        AttndRec attndRec = attndRecRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in is required before check-out"));

        if (attndRec.getChkinDtm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in is required before check-out");
        }

        attndRec.applyCheckOut(now);
        AttndRec saved = attndRecRepository.save(attndRec);
        HoliCache holiday = findHolidayForDate(resolveHolidayContext(user), today);
        return toTodayResponse(today, saved, holiday, locale);
    }

    @Override
    @Transactional
    public AttndTodayResponse saveManualEntry(
            AuthenticatedUsr authenticatedUsr,
            AttndManualEntryRequest request,
            Locale locale
    ) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        AttndContext attndContext = resolveAttndContext(user);
        LocalDate attndDate = parseAttndDate(request.attndDt());
        String attndStatus = normalizeAttndStatus(request.attndStsCd());
        Optional<AttndRec> existingRec = attndRecRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), attndDate);
        boolean statusUsesNoTime = AttndStatusCd.isNoTimeStatus(attndStatus);
        OffsetDateTime checkInValue = statusUsesNoTime
                ? null
                : mergeAttndTime(attndDate, request.chkinTm(), existingRec.map(AttndRec::getChkinDtm).orElse(null));
        OffsetDateTime checkOutValue = statusUsesNoTime
                ? null
                : mergeAttndTime(attndDate, request.chkoutTm(), existingRec.map(AttndRec::getChkoutDtm).orElse(null));

        if (!statusUsesNoTime && checkInValue == null && checkOutValue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in or check-out time is required");
        }

        if (checkInValue != null && checkOutValue != null && checkOutValue.isBefore(checkInValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-out time must be later than check-in time");
        }

        AttndRec attndRec = existingRec
                .orElseGet(() -> AttndRec.createManual(
                        user.tenantId(),
                        attndContext.coId(),
                        user.usrId(),
                        attndContext.empId(),
                        attndDate,
                        checkInValue,
                        checkOutValue,
                        attndStatus
                ));

        if (attndRec.getId() != null) {
            attndRec.applyManualEntry(checkInValue, checkOutValue, attndStatus);
        }

        AttndRec saved = attndRecRepository.save(attndRec);
        HoliCache holiday = findHolidayForDate(resolveHolidayContext(user), attndDate);
        return toTodayResponse(attndDate, saved, holiday, locale);
    }

    private AuthenticatedUsr requireUsr(AuthenticatedUsr authenticatedUsr) {
        if (authenticatedUsr == null || authenticatedUsr.usrId() == null || authenticatedUsr.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authenticatedUsr;
    }

    private AttndContext resolveAttndContext(AuthenticatedUsr authenticatedUsr) {
        Optional<Emp> emp = empRepository.findByUsrId(authenticatedUsr.usrId());
        Long companyId = authenticatedUsr.coId();

        if (companyId == null && emp.isPresent()) {
            companyId = emp.get().getCoId();
        }

        if (companyId == null) {
            companyId = resolveOrCreateTenantCoId(authenticatedUsr.tenantId());
        }

        if (companyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attnd can be recorded after this user is linked to a customer company or emp profile"
            );
        }

        return new AttndContext(companyId, emp.map(Emp::getId).orElse(null));
    }

    private Long resolveOrCreateTenantCoId(Long tenantId) {
        Optional<Co> existingCo = coRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .findFirst();
        if (existingCo.isPresent()) {
            return existingCo.get().getId();
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant context is required for attendance"));

        String baseCoCd = normalizeTenantCoCd(tenant.getTenantCd());
        String coCd = baseCoCd;
        int suffix = 1;
        while (coRepository.existsByTenantIdAndCoCdIgnoreCase(tenantId, coCd)) {
            suffix++;
            coCd = baseCoCd + suffix;
        }

        Co company = Co.create(
                tenantId,
                coCd,
                tenant.getTenantNm(),
                null,
                attndProperties.dfltCtryCd(),
                attndProperties.dfltRegionCd(),
                null,
                null,
                CommonBizConst.STS_CD_ACTIVE
        );
        return coRepository.save(company).getId();
    }

    private LocalDate parseAttndDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now(attndProperties.bizZoneId());
        }

        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attnd date must follow yyyy-MM-dd");
        }
    }

    private OffsetDateTime parseAttndTime(LocalDate attndDate, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        try {
            LocalTime localTime = LocalTime.parse(value, TIME_FORMAT);
            ZoneId bizZoneId = attndProperties.bizZoneId();
            return OffsetDateTime.of(attndDate, localTime, bizZoneId.getRules().getOffset(attndDate.atTime(localTime)));
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must follow HH:mm");
        }
    }

    private OffsetDateTime mergeAttndTime(LocalDate attndDate, String incomingValue, OffsetDateTime currentValue) {
        if (incomingValue == null || incomingValue.isBlank()) {
            return currentValue;
        }
        return parseAttndTime(attndDate, incomingValue, "Time must follow HH:mm");
    }

    private String normalizeAttndStatus(String value) {
        String normalized = AttndStatusCd.normalizeOrNull(value);
        if (normalized == null) {
            return null;
        }
        if (!AttndStatusCd.isSupported(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attnd status is invalid");
        }
        return normalized;
    }

    private HolidayContext resolveHolidayContext(AuthenticatedUsr authenticatedUsr) {
        Optional<Emp> emp = empRepository.findByUsrId(authenticatedUsr.usrId());
        if (emp.isPresent() && emp.get().getWorkLocId() != null) {
            Optional<WorkLoc> workLoc = workLocRepository.findByIdAndTenantIdAndCoId(
                    emp.get().getWorkLocId(),
                    authenticatedUsr.tenantId(),
                    emp.get().getCoId()
            );
            if (workLoc.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCd(workLoc.get().getCtryCd()),
                        normalizeRegionCd(workLoc.get().getRegionCd())
                );
            }
        }

        Long companyId = authenticatedUsr.coId();
        if (companyId == null && emp.isPresent()) {
            companyId = emp.get().getCoId();
        }
        if (companyId != null) {
            Optional<Co> company = coRepository.findByIdAndTenantId(companyId, authenticatedUsr.tenantId());
            if (company.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCd(company.get().getHqCtryCd()),
                        normalizeRegionCd(company.get().getHqRegionCd())
                );
            }
        }

        return new HolidayContext(attndProperties.dfltCtryCd(), attndProperties.dfltRegionCd());
    }

    private List<String> resolveRegionScope(String regionCd) {
        if (regionCd == null || regionCd.isBlank() || attndProperties.dfltRegionCd().equalsIgnoreCase(regionCd)) {
            return List.of(attndProperties.dfltRegionCd());
        }
        return List.of(attndProperties.dfltRegionCd(), regionCd.toUpperCase(Locale.ROOT));
    }

    private Map<LocalDate, HoliCache> buildHolidayMap(List<HoliCache> holidays) {
        Map<LocalDate, HoliCache> holidayByDate = new LinkedHashMap<>();
        for (HoliCache holiday : holidays) {
            HoliCache current = holidayByDate.get(holiday.getHoliDt());
            if (current == null || (attndProperties.dfltRegionCd().equals(current.getRegionCd())
                    && !attndProperties.dfltRegionCd().equals(holiday.getRegionCd()))) {
                holidayByDate.put(holiday.getHoliDt(), holiday);
            }
        }
        return holidayByDate;
    }

    private AttndTodayResponse toTodayResponse(
            LocalDate today,
            AttndRec attndRec,
            HoliCache holiday,
            Locale locale
    ) {
        String statusCd = attndRec == null ? AttndStatusCd.NONE : attndRec.getAttndStsCd();
        boolean checkedIn = attndRec != null && attndRec.getChkinDtm() != null;
        boolean checkedOut = attndRec != null && attndRec.getChkoutDtm() != null;
        boolean onLeave = AttndStatusCd.isLeave(statusCd);
        return new AttndTodayResponse(
                today.format(DATE_FORMAT),
                statusCd,
                resolveStatusName(statusCd, locale),
                attndRec == null ? null : formatDateTime(attndRec.getChkinDtm()),
                attndRec == null ? null : formatDateTime(attndRec.getChkoutDtm()),
                !onLeave && !checkedIn,
                !onLeave && checkedIn && !checkedOut,
                holiday != null,
                holiday == null ? null : resolveHolidayName(holiday, locale)
        );
    }

    private boolean isWeekend(LocalDate value) {
        DayOfWeek dayOfWeek = value.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private String resolveHolidayName(HoliCache holiday, Locale locale) {
        return "de".equalsIgnoreCase(locale.getLanguage()) ? holiday.getHoliNmLoc() : holiday.getHoliNmEn();
    }

    private String resolveStatusName(String statusCd, Locale locale) {
        return commonCdService.resolveDspNm(
                CommonCdGroupCd.ATTND_FLAG,
                statusCd,
                AttndStatusCd.fallbackDisplayValue(statusCd)
        );
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null
                ? null
                : value.atZoneSameInstant(attndProperties.bizZoneId()).format(DATETIME_FORMAT);
    }

    private String formatTime(OffsetDateTime value) {
        return value == null
                ? null
                : value.atZoneSameInstant(attndProperties.bizZoneId()).toLocalTime().format(TIME_FORMAT);
    }

    private HoliCache findHolidayForDate(HolidayContext holidayContext, LocalDate date) {
        List<HoliCache> holidays = holiCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                holidayContext.ctryCd(),
                resolveRegionScope(holidayContext.regionCd()),
                date,
                date
        );
        return buildHolidayMap(holidays).get(date);
    }

    private void ensureHoliCache(int year, String ctryCd) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        if (holiCacheRepository.countByCtryCdAndHoliDtBetween(ctryCd, startDate, endDate) > 0) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(attndProperties.holiApiUrl(), year, ctryCd)))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Holiday API request failed with status {}", response.statusCode());
                return;
            }

            List<NagerHolidayResponse> holidays = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<NagerHolidayResponse>>() {
                    }
            );

            for (NagerHolidayResponse holiday : holidays) {
                LocalDate holidayDate = LocalDate.parse(holiday.date());
                if (holiday.counties() == null || holiday.counties().isEmpty()) {
                    upsertHoliday(ctryCd, attndProperties.dfltRegionCd(), holidayDate, holiday);
                    continue;
                }
                for (String county : holiday.counties()) {
                    String regionCd = normalizeCountyCd(ctryCd, county);
                    upsertHoliday(ctryCd, regionCd, holidayDate, holiday);
                }
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to refresh German holidays for year {}", year, exception);
        }
    }

    private void upsertHoliday(String ctryCd, String regionCd, LocalDate holidayDate, NagerHolidayResponse holiday) {
        Optional<HoliCache> existing = holiCacheRepository.findByCtryCdAndRegionCdAndHoliDt(ctryCd, regionCd, holidayDate);
        if (existing.isPresent()) {
            HoliCache holiCache = existing.get();
            holiCache.refresh(
                    safeHolidayName(holiday.localName(), holiday.name()),
                    safeHolidayName(holiday.name(), holiday.localName()),
                    holiday.global() ? CommonBizConst.YN_Y : CommonBizConst.YN_N,
                    attndProperties.holiSource()
            );
            holiCacheRepository.save(holiCache);
            return;
        }

        holiCacheRepository.save(HoliCache.create(
                ctryCd,
                regionCd,
                holidayDate,
                safeHolidayName(holiday.localName(), holiday.name()),
                safeHolidayName(holiday.name(), holiday.localName()),
                holiday.global() ? CommonBizConst.YN_Y : CommonBizConst.YN_N,
                attndProperties.holiSource()
        ));
    }

    private String safeHolidayName(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return "Holiday";
    }

    private String normalizeCountryCd(String value) {
        if (value == null || value.isBlank()) {
            return attndProperties.dfltCtryCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRegionCd(String value) {
        if (value == null || value.isBlank()) {
            return attndProperties.dfltRegionCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCountyCd(String ctryCd, String county) {
        if (county == null || county.isBlank()) {
            return attndProperties.dfltRegionCd();
        }

        String normalizedCounty = county.trim().toUpperCase(Locale.ROOT);
        String prefix = normalizeCountryCd(ctryCd) + "-";
        if (normalizedCounty.startsWith(prefix)) {
            return normalizedCounty.substring(prefix.length());
        }
        return normalizedCounty;
    }

    private String normalizeTenantCoCd(String tenantCd) {
        if (tenantCd == null || tenantCd.isBlank()) {
            return "DEFAULT_CO";
        }
        String normalized = tenantCd.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        return normalized.length() > 100 ? normalized.substring(0, 100) : normalized;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NagerHolidayResponse(
            String date,
            String localName,
            String name,
            boolean global,
            @JsonProperty("counties") List<String> counties
    ) {
    }

    private record HolidayContext(String ctryCd, String regionCd) {
    }

    private record AttndContext(Long coId, Long empId) {
    }
}

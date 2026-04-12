package com.sinwoo.attendance.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.attendance.domain.AttendanceRecord;
import com.sinwoo.attendance.domain.HolidayCache;
import com.sinwoo.attendance.dto.AttendanceCalendarDayResponse;
import com.sinwoo.attendance.dto.AttendanceHolidayFocusResponse;
import com.sinwoo.attendance.dto.AttendanceManualEntryRequest;
import com.sinwoo.attendance.dto.AttendanceMonthSummaryResponse;
import com.sinwoo.attendance.dto.AttendanceTodayResponse;
import com.sinwoo.attendance.dto.AttendanceWidgetResponse;
import com.sinwoo.attendance.repository.AttendanceRecordRepository;
import com.sinwoo.attendance.repository.HolidayCacheRepository;
import com.sinwoo.code.service.CommonCodeService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.company.domain.Company;
import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.employee.domain.Employee;
import com.sinwoo.employee.repository.EmployeeRepository;
import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.repository.TenantRepository;
import com.sinwoo.worklocation.domain.WorkLocation;
import com.sinwoo.worklocation.repository.WorkLocationRepository;
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
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private static final String HOLIDAY_API_URL = "https://date.nager.at/api/v3/PublicHolidays/%d/%s";
    private static final String DEFAULT_COUNTRY_CD = "DE";
    private static final String DEFAULT_REGION_CD = "ALL";
    private static final String HOLIDAY_SOURCE = "NAGER";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Berlin");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final HolidayCacheRepository holidayCacheRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final WorkLocationRepository workLocationRepository;
    private final TenantRepository tenantRepository;
    private final CommonCodeService commonCodeService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public AttendanceWidgetResponse getAttendanceWidget(
            AuthenticatedUser authenticatedUser,
            YearMonth yearMonth,
            Locale locale
    ) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        YearMonth targetMonth = yearMonth == null ? YearMonth.now(BUSINESS_ZONE) : yearMonth;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();
        LocalDate gridStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate gridEnd = monthEnd.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        HolidayContext holidayContext = resolveHolidayContext(user);
        ensureHolidayCache(targetMonth.getYear(), holidayContext.ctryCd());

        Map<LocalDate, HolidayCache> holidayByDate = buildHolidayMap(
                holidayCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                        holidayContext.ctryCd(),
                        resolveRegionScope(holidayContext.regionCd()),
                        gridStart,
                        gridEnd
                )
        );

        Map<LocalDate, AttendanceRecord> attendanceByDate = attendanceRecordRepository
                .findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtAsc(
                        user.tenantId(),
                        user.usrId(),
                        gridStart,
                        gridEnd
                )
                .stream()
                .collect(LinkedHashMap::new, (map, record) -> map.put(record.getAttndDt(), record), Map::putAll);

        List<AttendanceCalendarDayResponse> dayList = new ArrayList<>();
        LocalDate cursor = gridStart;
        while (!cursor.isAfter(gridEnd)) {
            HolidayCache holiday = holidayByDate.get(cursor);
            AttendanceRecord record = attendanceByDate.get(cursor);
            dayList.add(new AttendanceCalendarDayResponse(
                    cursor.format(DATE_FORMAT),
                    cursor.getDayOfMonth(),
                    cursor.getMonthValue() == targetMonth.getMonthValue(),
                    cursor.equals(today),
                    isWeekend(cursor),
                    holiday != null,
                    holiday == null ? null : resolveHolidayName(holiday, locale),
                    record == null ? "NONE" : record.getAttndStsCd(),
                    formatTime(record == null ? null : record.getChkinDtm()),
                    formatTime(record == null ? null : record.getChkoutDtm())
            ));
            cursor = cursor.plusDays(1);
        }

        AttendanceMonthSummaryResponse summary = new AttendanceMonthSummaryResponse(
                (int) attendanceByDate.values().stream()
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

        AttendanceHolidayFocusResponse holidayFocus = holidayByDate.values().stream()
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
                .map(holiday -> new AttendanceHolidayFocusResponse(
                        holiday.getHoliDt().format(DATE_FORMAT),
                        resolveHolidayName(holiday, locale)
                ))
                .findFirst()
                .orElse(null);

        return new AttendanceWidgetResponse(
                targetMonth.format(MONTH_FORMAT),
                holidayContext.regionCd(),
                toTodayResponse(today, attendanceByDate.get(today), holidayByDate.get(today), locale),
                summary,
                holidayFocus,
                dayList
        );
    }

    @Override
    @Transactional
    public AttendanceTodayResponse checkIn(AuthenticatedUser authenticatedUser, Locale locale) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        OffsetDateTime now = OffsetDateTime.now(BUSINESS_ZONE);
        AttendanceContext attendanceContext = resolveAttendanceContext(user);
        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), today)
                .orElseGet(() -> AttendanceRecord.createCheckedIn(
                        user.tenantId(),
                        attendanceContext.coId(),
                        user.usrId(),
                        attendanceContext.empId(),
                        today,
                        now
                ));

        if (attendanceRecord.getId() != null) {
            attendanceRecord.applyCheckIn(now);
        }

        AttendanceRecord saved = attendanceRecordRepository.save(attendanceRecord);
        HolidayCache holiday = findHolidayForDate(resolveHolidayContext(user), today);
        return toTodayResponse(today, saved, holiday, locale);
    }

    @Override
    @Transactional
    public AttendanceTodayResponse checkOut(AuthenticatedUser authenticatedUser, Locale locale) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        OffsetDateTime now = OffsetDateTime.now(BUSINESS_ZONE);
        resolveAttendanceContext(user);
        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in is required before check-out"));

        if (attendanceRecord.getChkinDtm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in is required before check-out");
        }

        attendanceRecord.applyCheckOut(now);
        AttendanceRecord saved = attendanceRecordRepository.save(attendanceRecord);
        HolidayCache holiday = findHolidayForDate(resolveHolidayContext(user), today);
        return toTodayResponse(today, saved, holiday, locale);
    }

    @Override
    @Transactional
    public AttendanceTodayResponse saveManualEntry(
            AuthenticatedUser authenticatedUser,
            AttendanceManualEntryRequest request,
            Locale locale
    ) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        AttendanceContext attendanceContext = resolveAttendanceContext(user);
        LocalDate attndDate = parseAttendanceDate(request.attndDt());
        String attendanceStatus = normalizeAttendanceStatus(request.attndStsCd());
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository
                .findByTenantIdAndUsrIdAndAttndDt(user.tenantId(), user.usrId(), attndDate);
        boolean statusUsesNoTime = "LEAVE".equals(attendanceStatus) || "BUSINESS_TRIP".equals(attendanceStatus);
        OffsetDateTime checkInValue = statusUsesNoTime
                ? null
                : mergeAttendanceTime(attndDate, request.chkinTm(), existingRecord.map(AttendanceRecord::getChkinDtm).orElse(null));
        OffsetDateTime checkOutValue = statusUsesNoTime
                ? null
                : mergeAttendanceTime(attndDate, request.chkoutTm(), existingRecord.map(AttendanceRecord::getChkoutDtm).orElse(null));

        if (!statusUsesNoTime && checkInValue == null && checkOutValue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in or check-out time is required");
        }

        if (checkInValue != null && checkOutValue != null && checkOutValue.isBefore(checkInValue)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-out time must be later than check-in time");
        }

        AttendanceRecord attendanceRecord = existingRecord
                .orElseGet(() -> AttendanceRecord.createManual(
                        user.tenantId(),
                        attendanceContext.coId(),
                        user.usrId(),
                        attendanceContext.empId(),
                        attndDate,
                        checkInValue,
                        checkOutValue,
                        attendanceStatus
                ));

        if (attendanceRecord.getId() != null) {
            attendanceRecord.applyManualEntry(checkInValue, checkOutValue, attendanceStatus);
        }

        AttendanceRecord saved = attendanceRecordRepository.save(attendanceRecord);
        HolidayCache holiday = findHolidayForDate(resolveHolidayContext(user), attndDate);
        return toTodayResponse(attndDate, saved, holiday, locale);
    }

    private Long resolveEmployeeId(Long usrId) {
        return employeeRepository.findByUsrId(usrId)
                .map(Employee::getId)
                .orElse(null);
    }

    private AuthenticatedUser requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.usrId() == null || authenticatedUser.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authenticatedUser;
    }

    private AttendanceContext resolveAttendanceContext(AuthenticatedUser authenticatedUser) {
        Optional<Employee> employee = employeeRepository.findByUsrId(authenticatedUser.usrId());
        Long companyId = authenticatedUser.coId();

        if (companyId == null && employee.isPresent()) {
            companyId = employee.get().getCoId();
        }

        if (companyId == null) {
            companyId = resolveOrCreateTenantCompanyId(authenticatedUser.tenantId());
        }

        if (companyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance can be recorded after this user is linked to a customer company or employee profile"
            );
        }

        return new AttendanceContext(companyId, employee.map(Employee::getId).orElse(null));
    }

    private Long resolveOrCreateTenantCompanyId(Long tenantId) {
        Optional<Company> existingCompany = companyRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .findFirst();
        if (existingCompany.isPresent()) {
            return existingCompany.get().getId();
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant context is required for attendance"));

        String baseCompanyCode = normalizeTenantCompanyCode(tenant.getTenantCd());
        String companyCode = baseCompanyCode;
        int suffix = 1;
        while (companyRepository.existsByTenantIdAndCoCdIgnoreCase(tenantId, companyCode)) {
            suffix++;
            companyCode = baseCompanyCode + suffix;
        }

        Company company = Company.create(
                tenantId,
                companyCode,
                tenant.getTenantNm(),
                null,
                DEFAULT_COUNTRY_CD,
                DEFAULT_REGION_CD,
                null,
                null,
                "ACTIVE"
        );
        return companyRepository.save(company).getId();
    }

    private LocalDate parseAttendanceDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now(BUSINESS_ZONE);
        }

        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance date must follow yyyy-MM-dd");
        }
    }

    private OffsetDateTime parseAttendanceTime(LocalDate attndDate, String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        try {
            LocalTime localTime = LocalTime.parse(value, TIME_FORMAT);
            return OffsetDateTime.of(attndDate, localTime, BUSINESS_ZONE.getRules().getOffset(attndDate.atTime(localTime)));
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must follow HH:mm");
        }
    }

    private OffsetDateTime parseOptionalAttendanceTime(LocalDate attndDate, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseAttendanceTime(attndDate, value, "Time must follow HH:mm");
    }

    private OffsetDateTime mergeAttendanceTime(LocalDate attndDate, String incomingValue, OffsetDateTime currentValue) {
        if (incomingValue == null || incomingValue.isBlank()) {
            return currentValue;
        }
        return parseAttendanceTime(attndDate, incomingValue, "Time must follow HH:mm");
    }

    private String normalizeAttendanceStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CHECKED_IN", "CHECKED_OUT", "LEAVE", "BUSINESS_TRIP" -> normalized;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance status is invalid");
        };
    }

    private HolidayContext resolveHolidayContext(AuthenticatedUser authenticatedUser) {
        Optional<Employee> employee = employeeRepository.findByUsrId(authenticatedUser.usrId());
        if (employee.isPresent() && employee.get().getWorkLocId() != null) {
            Optional<WorkLocation> workLocation = workLocationRepository.findByIdAndTenantIdAndCoId(
                    employee.get().getWorkLocId(),
                    authenticatedUser.tenantId(),
                    employee.get().getCoId()
            );
            if (workLocation.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCode(workLocation.get().getCtryCd()),
                        normalizeRegionCode(workLocation.get().getRegionCd())
                );
            }
        }

        Long companyId = authenticatedUser.coId();
        if (companyId == null && employee.isPresent()) {
            companyId = employee.get().getCoId();
        }
        if (companyId != null) {
            Optional<Company> company = companyRepository.findByIdAndTenantId(companyId, authenticatedUser.tenantId());
            if (company.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCode(company.get().getHqCtryCd()),
                        normalizeRegionCode(company.get().getHqRegionCd())
                );
            }
        }

        return new HolidayContext(DEFAULT_COUNTRY_CD, DEFAULT_REGION_CD);
    }

    private List<String> resolveRegionScope(String regionCd) {
        if (regionCd == null || regionCd.isBlank() || DEFAULT_REGION_CD.equalsIgnoreCase(regionCd)) {
            return List.of(DEFAULT_REGION_CD);
        }
        return List.of(DEFAULT_REGION_CD, regionCd.toUpperCase(Locale.ROOT));
    }

    private Map<LocalDate, HolidayCache> buildHolidayMap(List<HolidayCache> holidays) {
        Map<LocalDate, HolidayCache> holidayByDate = new LinkedHashMap<>();
        for (HolidayCache holiday : holidays) {
            HolidayCache current = holidayByDate.get(holiday.getHoliDt());
            if (current == null || (DEFAULT_REGION_CD.equals(current.getRegionCd()) && !DEFAULT_REGION_CD.equals(holiday.getRegionCd()))) {
                holidayByDate.put(holiday.getHoliDt(), holiday);
            }
        }
        return holidayByDate;
    }

    private AttendanceTodayResponse toTodayResponse(
            LocalDate today,
            AttendanceRecord attendanceRecord,
            HolidayCache holiday,
            Locale locale
    ) {
        String statusCd = attendanceRecord == null ? "NONE" : attendanceRecord.getAttndStsCd();
        boolean checkedIn = attendanceRecord != null && attendanceRecord.getChkinDtm() != null;
        boolean checkedOut = attendanceRecord != null && attendanceRecord.getChkoutDtm() != null;
        boolean onLeave = "LEAVE".equalsIgnoreCase(statusCd);
        return new AttendanceTodayResponse(
                today.format(DATE_FORMAT),
                statusCd,
                resolveStatusName(statusCd, locale),
                attendanceRecord == null ? null : formatDateTime(attendanceRecord.getChkinDtm()),
                attendanceRecord == null ? null : formatDateTime(attendanceRecord.getChkoutDtm()),
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

    private String resolveHolidayName(HolidayCache holiday, Locale locale) {
        return "de".equalsIgnoreCase(locale.getLanguage()) ? holiday.getHoliNmLoc() : holiday.getHoliNmEn();
    }

    private String resolveStatusName(String statusCd, Locale locale) {
        return commonCodeService.resolveDisplayName("ATTND_FLAG", statusCd, fallbackStatusName(statusCd, locale));
    }

    private String fallbackStatusName(String statusCd, Locale locale) {
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        return switch (statusCd) {
            case "CHECKED_IN" -> switch (language) {
                case "de" -> "Eingecheckt";
                case "ko" -> "출근 완료";
                default -> "Checked in";
            };
            case "CHECKED_OUT" -> switch (language) {
                case "de" -> "Ausgecheckt";
                case "ko" -> "퇴근 완료";
                default -> "Checked out";
            };
            case "LEAVE" -> switch (language) {
                case "de" -> "Urlaub";
                case "ko" -> "휴가";
                default -> "Leave";
            };
            case "BUSINESS_TRIP" -> switch (language) {
                case "de" -> "Dienstreise";
                case "ko" -> "출장";
                default -> "Business trip";
            };
            default -> switch (language) {
                case "de" -> "Bereit";
                case "ko" -> "대기";
                default -> "Ready";
            };
        };
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null ? null : value.format(DATETIME_FORMAT);
    }

    private String formatTime(OffsetDateTime value) {
        return value == null ? null : value.toLocalTime().format(TIME_FORMAT);
    }

    private HolidayCache findHolidayForDate(HolidayContext holidayContext, LocalDate date) {
        List<HolidayCache> holidays = holidayCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                holidayContext.ctryCd(),
                resolveRegionScope(holidayContext.regionCd()),
                date,
                date
        );
        return buildHolidayMap(holidays).get(date);
    }

    private void ensureHolidayCache(int year, String ctryCd) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        if (holidayCacheRepository.countByCtryCdAndHoliDtBetween(ctryCd, startDate, endDate) > 0) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(HOLIDAY_API_URL, year, ctryCd)))
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
                    upsertHoliday(ctryCd, DEFAULT_REGION_CD, holidayDate, holiday);
                    continue;
                }
                for (String county : holiday.counties()) {
                    String regionCd = normalizeCountyCode(ctryCd, county);
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
        Optional<HolidayCache> existing = holidayCacheRepository.findByCtryCdAndRegionCdAndHoliDt(ctryCd, regionCd, holidayDate);
        if (existing.isPresent()) {
            HolidayCache holidayCache = existing.get();
            holidayCache.refresh(
                    safeHolidayName(holiday.localName(), holiday.name()),
                    safeHolidayName(holiday.name(), holiday.localName()),
                    holiday.global() ? "Y" : "N",
                    HOLIDAY_SOURCE
            );
            holidayCacheRepository.save(holidayCache);
            return;
        }

        holidayCacheRepository.save(HolidayCache.create(
                ctryCd,
                regionCd,
                holidayDate,
                safeHolidayName(holiday.localName(), holiday.name()),
                safeHolidayName(holiday.name(), holiday.localName()),
                holiday.global() ? "Y" : "N",
                HOLIDAY_SOURCE
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

    private String normalizeCountryCode(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_COUNTRY_CD;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRegionCode(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_REGION_CD;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCountyCode(String ctryCd, String county) {
        if (county == null || county.isBlank()) {
            return DEFAULT_REGION_CD;
        }

        String normalizedCounty = county.trim().toUpperCase(Locale.ROOT);
        String prefix = normalizeCountryCode(ctryCd) + "-";
        if (normalizedCounty.startsWith(prefix)) {
            return normalizedCounty.substring(prefix.length());
        }
        return normalizedCounty;
    }

    private String normalizeTenantCompanyCode(String tenantCd) {
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

    private record AttendanceContext(Long coId, Long empId) {
    }
}

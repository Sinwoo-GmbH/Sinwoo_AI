package com.sinwoo.platform.hol.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.department.domain.Dept;
import com.sinwoo.platform.department.repository.DeptRepository;
import com.sinwoo.platform.employee.domain.Emp;
import com.sinwoo.platform.employee.repository.EmpRepository;
import com.sinwoo.platform.hol.domain.RgnHol;
import com.sinwoo.platform.hol.dto.RgnHolListResponse;
import com.sinwoo.platform.hol.dto.RgnHolResponse;
import com.sinwoo.platform.hol.repository.RgnHolRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RgnHolServiceImpl implements RgnHolService {

    private static final String NAGER_API = "https://date.nager.at/api/v3/PublicHolidays/%d/DE";

    private final RgnHolRepository rgnHolRepository;
    private final EmpRepository empRepository;
    private final DeptRepository deptRepository;
    private final RestTemplate restTemplate;

    @Override
    public RgnHolListResponse getMyRgnHols(AuthenticatedUsr usr, Short yr) {
        String regionCd = resolveRegionCd(usr);
        List<String> regionCds = List.of("ALL", regionCd);
        List<RgnHolResponse> items = rgnHolRepository
                .findAllByYrAndRegionCdInOrderByHolidayDtAsc(yr, regionCds)
                .stream()
                .map(RgnHolResponse::from)
                .toList();
        return new RgnHolListResponse(items.size(), items);
    }

    @Override
    public RgnHolListResponse getMyRgnHolsByPeriod(AuthenticatedUsr usr, LocalDate from, LocalDate to) {
        String regionCd = resolveRegionCd(usr);
        List<String> regionCds = List.of("ALL", regionCd);
        List<RgnHolResponse> items = rgnHolRepository
                .findAllByHolidayDtBetweenAndRegionCdInOrderByHolidayDtAsc(from, to, regionCds)
                .stream()
                .map(RgnHolResponse::from)
                .toList();
        return new RgnHolListResponse(items.size(), items);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void syncRgnHols(Short yr) {
        // ALL 지역 데이터가 이미 있으면 skip
        if (rgnHolRepository.existsByYrAndRegionCd(yr, "ALL")) {
            log.debug("Region holidays for year {} already exist, skipping sync", yr);
            return;
        }

        log.info("Fetching public holidays for year {} from Nager.at API", yr);

        try {
            List<Map<String, Object>> holidays = restTemplate.getForObject(
                    NAGER_API.formatted(yr.intValue()), List.class);

            if (holidays == null || holidays.isEmpty()) {
                log.warn("No holidays returned from API for year {}", yr);
                return;
            }

            for (Map<String, Object> h : holidays) {
                LocalDate dt = LocalDate.parse((String) h.get("date"));
                String name = (String) h.get("localName");
                boolean isWeekend = dt.getDayOfWeek() == DayOfWeek.SATURDAY
                        || dt.getDayOfWeek() == DayOfWeek.SUNDAY;

                // counties가 null이면 전국(ALL)
                List<String> counties = (List<String>) h.get("counties");

                if (counties == null || counties.isEmpty()) {
                    saveIfNotExists(yr, "ALL", "Alle", dt, name, isWeekend);
                } else {
                    for (String county : counties) {
                        // "DE-NW" -> "NW"
                        String rgn = county.length() > 3 ? county.substring(3) : county;
                        saveIfNotExists(yr, rgn, rgn, dt, name, isWeekend);
                    }
                }
            }

            log.info("Synced {} holiday entries for year {}", holidays.size(), yr);
        } catch (Exception e) {
            log.error("Failed to sync region holidays for year {}: {}", yr, e.getMessage());
        }
    }

    /** 앱 시작 시 올해+내년 공휴일 자동 동기화 */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartupSyncHolidays() {
        short thisYear = (short) LocalDate.now().getYear();
        short nextYear = (short) (thisYear + 1);
        log.info("Startup: auto-syncing region holidays for {} and {}", thisYear, nextYear);
        syncRgnHols(thisYear);
        syncRgnHols(nextYear);
    }

    // ── helpers ──────────────────────────────────────────────

    private String resolveRegionCd(AuthenticatedUsr usr) {
        Emp emp = empRepository.findByUsrId(usr.usrId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No employee record linked to user"));

        if (emp.getDeptId() == null) {
            return "ALL";
        }

        return deptRepository.findById(emp.getDeptId())
                .map(Dept::getRegionCd)
                .filter(rc -> rc != null && !rc.isBlank())
                .orElse("ALL");
    }

    private void saveIfNotExists(Short yr, String regionCd, String regionNm,
                                  LocalDate dt, String name, boolean isWeekend) {
        try {
            rgnHolRepository.save(
                    RgnHol.create(yr, regionCd, regionNm, dt, name, isWeekend ? "Y" : "N")
            );
        } catch (Exception e) {
            // UK 중복 시 무시 (UPSERT 대체)
            log.debug("Duplicate holiday skipped: {} {} {}", yr, regionCd, dt);
        }
    }
}

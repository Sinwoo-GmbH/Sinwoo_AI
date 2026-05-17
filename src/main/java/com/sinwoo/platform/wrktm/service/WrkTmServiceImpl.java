package com.sinwoo.platform.wrktm.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.employee.domain.Emp;
import com.sinwoo.platform.employee.repository.EmpRepository;
import com.sinwoo.platform.wrktm.domain.WrkTm;
import com.sinwoo.platform.wrktm.dto.ClockInRequest;
import com.sinwoo.platform.wrktm.dto.ClockOutRequest;
import com.sinwoo.platform.wrktm.dto.SaveWrkTmRequest;
import com.sinwoo.platform.wrktm.dto.WrkTmListResponse;
import com.sinwoo.platform.wrktm.dto.WrkTmResponse;
import com.sinwoo.platform.wrktm.repository.WrkTmRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrkTmServiceImpl implements WrkTmService {

    private final WrkTmRepository wrkTmRepository;
    private final EmpRepository empRepository;
    private final CoRepository coRepository;

    @Override
    @Transactional
    public WrkTmResponse clockIn(AuthenticatedUsr usr, ClockInRequest request) {
        Emp emp = resolveEmp(usr);
        WrkTm wt = findOrCreate(usr.tenantId(), usr.coId(), emp.getId(), request.workDt());

        wt.clockIn(request.strTm());
        return WrkTmResponse.from(wrkTmRepository.save(wt));
    }

    @Override
    @Transactional
    public WrkTmResponse clockOut(AuthenticatedUsr usr, ClockOutRequest request) {
        Emp emp = resolveEmp(usr);
        WrkTm wt = findOrCreate(usr.tenantId(), usr.coId(), emp.getId(), request.workDt());

        Short workMin = null;
        if (wt.getStrTm() != null) {
            if (request.endTm().isBefore(wt.getStrTm())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
            }
            Co co = coRepository.findById(usr.coId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
            workMin = calcWorkMin(wt.getStrTm(), request.endTm(), co.getLunchStrTm(), co.getLunchEndTm());
        }
        wt.clockOut(request.endTm(), workMin);
        return WrkTmResponse.from(wrkTmRepository.save(wt));
    }

    @Override
    @Transactional
    public WrkTmResponse saveWrkTm(AuthenticatedUsr usr, SaveWrkTmRequest request) {
        Emp emp = resolveEmp(usr);

        if (request.strTm() != null && request.endTm() != null && request.endTm().isBefore(request.strTm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        WrkTm wt = findOrCreate(usr.tenantId(), usr.coId(), emp.getId(), request.workDt());

        Short workMin = null;
        if (request.strTm() != null && request.endTm() != null) {
            Co co = coRepository.findById(usr.coId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
            workMin = calcWorkMin(request.strTm(), request.endTm(), co.getLunchStrTm(), co.getLunchEndTm());
        }

        wt.updateTimes(request.strTm(), request.endTm(), workMin, request.rmk());
        return WrkTmResponse.from(wrkTmRepository.save(wt));
    }

    @Override
    @Transactional
    public void deleteWrkTm(AuthenticatedUsr usr, Long wrkTmId) {
        WrkTm wt = wrkTmRepository.findByIdAndTenantIdAndCoId(wrkTmId, usr.tenantId(), usr.coId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work time record not found"));
        wt.softDelete();
        wrkTmRepository.save(wt);
    }

    @Override
    public WrkTmResponse getWrkTm(AuthenticatedUsr usr, LocalDate workDt) {
        Emp emp = resolveEmp(usr);
        return wrkTmRepository.findByTenantIdAndCoIdAndEmpIdAndWorkDtAndDelYn(
                        usr.tenantId(), usr.coId(), emp.getId(), workDt, "N")
                .map(WrkTmResponse::from)
                .orElse(null);
    }

    @Override
    public WrkTmListResponse getMyWrkTms(AuthenticatedUsr usr, LocalDate from, LocalDate to) {
        Emp emp = resolveEmp(usr);
        List<WrkTmResponse> items = wrkTmRepository
                .findAllByTenantIdAndCoIdAndEmpIdAndWorkDtBetweenAndDelYnOrderByWorkDtAsc(
                        usr.tenantId(), usr.coId(), emp.getId(), from, to, "N")
                .stream()
                .map(WrkTmResponse::from)
                .toList();
        return new WrkTmListResponse(items.size(), items);
    }

    @Override
    public WrkTmListResponse getAllWrkTms(AuthenticatedUsr usr, LocalDate from, LocalDate to) {
        List<WrkTmResponse> items = wrkTmRepository
                .findAllByTenantIdAndCoIdAndWorkDtBetweenAndDelYnOrderByEmpIdAscWorkDtAsc(
                        usr.tenantId(), usr.coId(), from, to, "N")
                .stream()
                .map(WrkTmResponse::from)
                .toList();
        return new WrkTmListResponse(items.size(), items);
    }

    // ── helpers ──────────────────────────────────────────────

    private Emp resolveEmp(AuthenticatedUsr usr) {
        return empRepository.findByUsrId(usr.usrId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No employee record linked to user"));
    }

    private WrkTm findOrCreate(Long tenantId, Long coId, Long empId, LocalDate workDt) {
        return wrkTmRepository.findByTenantIdAndCoIdAndEmpIdAndWorkDtAndDelYn(
                        tenantId, coId, empId, workDt, "N")
                .orElse(WrkTm.create(tenantId, coId, empId, workDt, null, null, null, null));
    }

    /**
     * 실 근무시간(분) 계산.
     * 총 시간 = (endTm - strTm) 에서 점심시간 겹침분 차감.
     * 점심시간 미설정(null)이면 차감 없음.
     */
    static short calcWorkMin(LocalTime strTm, LocalTime endTm, LocalTime lunchStr, LocalTime lunchEnd) {
        long totalMin = Duration.between(strTm, endTm).toMinutes();

        if (lunchStr != null && lunchEnd != null) {
            // 점심시간과 근무시간의 겹침 계산
            LocalTime overlapStr = strTm.isAfter(lunchStr) ? strTm : lunchStr;
            LocalTime overlapEnd = endTm.isBefore(lunchEnd) ? endTm : lunchEnd;
            if (overlapStr.isBefore(overlapEnd)) {
                totalMin -= Duration.between(overlapStr, overlapEnd).toMinutes();
            }
        }

        return (short) Math.max(0, totalMin);
    }
}

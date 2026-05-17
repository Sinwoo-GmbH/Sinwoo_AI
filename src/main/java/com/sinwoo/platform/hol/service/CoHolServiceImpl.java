package com.sinwoo.platform.hol.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.hol.domain.CoHol;
import com.sinwoo.platform.hol.dto.CoHolListResponse;
import com.sinwoo.platform.hol.dto.CoHolResponse;
import com.sinwoo.platform.hol.dto.CreateCoHolRequest;
import com.sinwoo.platform.hol.dto.UpdateCoHolRequest;
import com.sinwoo.platform.hol.repository.CoHolRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoHolServiceImpl implements CoHolService {

    private final CoHolRepository coHolRepository;

    @Override
    public CoHolListResponse getCoHols(AuthenticatedUsr usr) {
        List<CoHolResponse> items = coHolRepository
                .findAllByTenantIdAndCoIdAndDelYnOrderByStrDtAsc(usr.tenantId(), usr.coId(), "N")
                .stream()
                .map(CoHolResponse::from)
                .toList();
        return new CoHolListResponse(items.size(), items);
    }

    @Override
    public CoHolListResponse getCoHolsByPeriod(AuthenticatedUsr usr, Short yr, LocalDate from, LocalDate to) {
        List<CoHolResponse> items = coHolRepository
                .findAllActiveByPeriod(usr.tenantId(), usr.coId(), yr, from, to)
                .stream()
                .map(CoHolResponse::from)
                .toList();
        return new CoHolListResponse(items.size(), items);
    }

    @Override
    @Transactional
    public CoHolResponse createCoHol(AuthenticatedUsr usr, CreateCoHolRequest request) {
        validateDates(request.strDt(), request.endDt());
        validateAnnualYr(request.annualYn(), request.applyYr());

        CoHol hol = CoHol.create(
                usr.tenantId(), usr.coId(),
                request.holidayNm().trim(),
                request.strDt(), request.endDt(),
                request.annualYn().toUpperCase(),
                request.applyYr()
        );
        return CoHolResponse.from(coHolRepository.save(hol));
    }

    @Override
    @Transactional
    public CoHolResponse updateCoHol(AuthenticatedUsr usr, Long coHolId, UpdateCoHolRequest request) {
        validateDates(request.strDt(), request.endDt());
        validateAnnualYr(request.annualYn(), request.applyYr());

        CoHol hol = coHolRepository.findByIdAndTenantIdAndCoId(coHolId, usr.tenantId(), usr.coId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company holiday not found"));

        if (hol.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company holiday not found");
        }

        hol.update(
                request.holidayNm().trim(),
                request.strDt(), request.endDt(),
                request.annualYn().toUpperCase(),
                request.applyYr()
        );
        return CoHolResponse.from(coHolRepository.save(hol));
    }

    @Override
    @Transactional
    public void deleteCoHol(AuthenticatedUsr usr, Long coHolId) {
        CoHol hol = coHolRepository.findByIdAndTenantIdAndCoId(coHolId, usr.tenantId(), usr.coId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company holiday not found"));
        hol.softDelete();
        coHolRepository.save(hol);
    }

    // ── validation ──────────────────────────────────────────

    private void validateDates(LocalDate strDt, LocalDate endDt) {
        if (endDt.isBefore(strDt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        }
    }

    private void validateAnnualYr(String annualYn, Short applyYr) {
        if ("Y".equalsIgnoreCase(annualYn) && applyYr != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply year must be 0 for annual holidays");
        }
        if ("N".equalsIgnoreCase(annualYn) && applyYr <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply year is required for non-annual holidays");
        }
    }
}

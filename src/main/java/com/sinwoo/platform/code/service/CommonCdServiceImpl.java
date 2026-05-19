package com.sinwoo.platform.code.service;

import static com.sinwoo.common.util.StringNormalizer.blankToNull;
import static com.sinwoo.common.util.StringNormalizer.normalizeYn;

import com.sinwoo.platform.code.domain.CdGroup;
import com.sinwoo.platform.code.domain.CommonCd;
import com.sinwoo.platform.code.dto.CdGroupListResponse;
import com.sinwoo.platform.code.dto.CdGroupResponse;
import com.sinwoo.platform.code.dto.CommonCdListResponse;
import com.sinwoo.platform.code.dto.CommonCdResponse;
import com.sinwoo.platform.code.dto.CreateCdGroupRequest;
import com.sinwoo.platform.code.dto.CreateCommonCdRequest;
import com.sinwoo.platform.code.dto.UpdateCdGroupRequest;
import com.sinwoo.platform.code.dto.UpdateCommonCdRequest;
import com.sinwoo.platform.code.repository.CdGroupRepository;
import com.sinwoo.platform.code.repository.CommonCdRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCdServiceImpl implements CommonCdService {

    private final CdGroupRepository cdGroupRepository;
    private final CommonCdRepository commonCdRepository;

    @Override
    @Transactional
    public CdGroupResponse createCdGroup(CreateCdGroupRequest request) {
        String normalizedGrpCd = normalizeCd(request.grpCd());
        if (cdGroupRepository.existsByGrpCdIgnoreCase(normalizedGrpCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code group already exists");
        }

        CdGroup group = CdGroup.create(
                normalizedGrpCd,
                normalizeLocalizedName(request.grpNmKo(), request.grpNmEn()),
                normalizeRequiredName(request.grpNmEn()),
                normalizeLocalizedName(request.grpNmDe(), request.grpNmEn()),
                normalizeYn(request.sysYn(), "N"),
                normalizeYn(request.useYn(), "Y"),
                normalizeOrder(request.dspOrd())
        );

        return toGroupResponse(cdGroupRepository.save(group));
    }

    @Override
    @Transactional
    public CdGroupResponse updateCdGroup(Long grpId, UpdateCdGroupRequest request) {
        CdGroup group = cdGroupRepository.findById(grpId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Code group not found"));

        group.update(
                normalizeLocalizedName(request.grpNmKo(), coalesce(request.grpNmEn(), group.getGrpNmEn(), group.getGrpNmKo())),
                normalizeLocalizedName(request.grpNmEn(), group.getGrpNmEn()),
                normalizeLocalizedName(request.grpNmDe(), coalesce(request.grpNmEn(), group.getGrpNmEn(), group.getGrpNmDe())),
                normalizeYn(request.useYn(), group.getUseYn()),
                normalizeOrder(request.dspOrd(), group.getDspOrd())
        );

        return toGroupResponse(group);
    }

    @Override
    public CdGroupListResponse getCdGroups() {
        List<CdGroupResponse> items = cdGroupRepository.findAllByOrderByDspOrdAscGrpCdAsc().stream()
                .map(this::toGroupResponse)
                .toList();
        return new CdGroupListResponse(items.size(), items);
    }

    @Override
    @Transactional
    public CommonCdResponse createCd(CreateCommonCdRequest request) {
        CdGroup group = cdGroupRepository.findByGrpCdIgnoreCase(normalizeCd(request.grpCd()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));

        String normalizedCd = normalizeCd(request.cd());
        if (commonCdRepository.existsByGrpIdAndCdIgnoreCase(group.getId(), normalizedCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code already exists in group");
        }

        CommonCd code = CommonCd.create(
                group.getId(),
                normalizedCd,
                normalizeLocalizedName(request.cdNmKo(), request.cdNmEn()),
                normalizeRequiredName(request.cdNmEn()),
                normalizeLocalizedName(request.cdNmDe(), request.cdNmEn()),
                blankToNull(request.cdDescKo()),
                blankToNull(request.cdDescEn()),
                blankToNull(request.cdDescDe()),
                normalizeYn(request.useYn(), "Y"),
                normalizeOrder(request.dspOrd())
        );

        return toCdResponse(commonCdRepository.save(code), group);
    }

    @Override
    @Transactional
    public CommonCdResponse updateCd(Long cdId, UpdateCommonCdRequest request) {
        CommonCd code = commonCdRepository.findById(cdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Code not found"));
        CdGroup group = cdGroupRepository.findById(code.getGrpId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));

        code.update(
                normalizeLocalizedName(request.cdNmKo(), coalesce(request.cdNmEn(), code.getCdNmEn(), code.getCdNmKo())),
                normalizeLocalizedName(request.cdNmEn(), code.getCdNmEn()),
                normalizeLocalizedName(request.cdNmDe(), coalesce(request.cdNmEn(), code.getCdNmEn(), code.getCdNmDe())),
                blankToNullPreserve(request.cdDescKo(), code.getCdDescKo()),
                blankToNullPreserve(request.cdDescEn(), code.getCdDescEn()),
                blankToNullPreserve(request.cdDescDe(), code.getCdDescDe()),
                normalizeYn(request.useYn(), code.getUseYn()),
                normalizeOrder(request.dspOrd(), code.getDspOrd())
        );

        return toCdResponse(code, group);
    }

    @Override
    public CommonCdListResponse getCds(String grpCd) {
        List<CommonCdResponse> items;
        if (grpCd == null || grpCd.isBlank()) {
            items = commonCdRepository.findAllSorted().stream()
                    .map(code -> toCdResponse(code, cdGroupRepository.findById(code.getGrpId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"))))
                    .toList();
        } else {
            CdGroup group = cdGroupRepository.findByGrpCdIgnoreCase(normalizeCd(grpCd))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));
            items = commonCdRepository.findByGrp(group.getId()).stream()
                    .map(code -> toCdResponse(code, group))
                    .toList();
        }
        return new CommonCdListResponse(items.size(), items);
    }

    @Override
    public String resolveDspNm(String grpCd, String cd, String fallbackName) {
        if (grpCd == null || grpCd.isBlank() || cd == null || cd.isBlank()) {
            return fallbackName;
        }

        return cdGroupRepository.findByGrpCdIgnoreCase(normalizeCd(grpCd))
                .flatMap(group -> commonCdRepository.findByGrpIdAndCdIgnoreCase(group.getId(), normalizeCd(cd)))
                .map(code -> localizedName(code, LocaleContextHolder.getLocale(), fallbackName))
                .orElse(fallbackName);
    }

    @Override
    @Transactional
    public void ensureCd(String grpCd, String cd, String fallbackName) {
        if (grpCd == null || grpCd.isBlank() || cd == null || cd.isBlank()) {
            return;
        }

        CdGroup group = cdGroupRepository.findByGrpCdIgnoreCase(normalizeCd(grpCd))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));

        if (commonCdRepository.existsByGrpIdAndCdIgnoreCase(group.getId(), normalizeCd(cd))) {
            return;
        }

        commonCdRepository.save(CommonCd.create(
                group.getId(),
                normalizeCd(cd),
                normalizeSeedName(fallbackName, cd),
                normalizeSeedName(fallbackName, cd),
                normalizeSeedName(fallbackName, cd),
                null,
                null,
                null,
                "Y",
                0
        ));
    }

    private CdGroupResponse toGroupResponse(CdGroup group) {
        return CdGroupResponse.from(group, localizedName(group, LocaleContextHolder.getLocale()));
    }

    private CommonCdResponse toCdResponse(CommonCd code, CdGroup group) {
        return CommonCdResponse.from(code, group.getGrpCd(), localizedName(code, LocaleContextHolder.getLocale(), code.getCd()));
    }

    private String localizedName(CdGroup group, Locale locale) {
        String language = normalizeLanguage(locale);
        return switch (language) {
            case "de" -> firstNonBlank(group.getGrpNmDe(), group.getGrpNmEn(), group.getGrpNmKo(), group.getGrpCd());
            case "ko" -> firstNonBlank(group.getGrpNmKo(), group.getGrpNmEn(), group.getGrpNmDe(), group.getGrpCd());
            default -> firstNonBlank(group.getGrpNmEn(), group.getGrpNmDe(), group.getGrpNmKo(), group.getGrpCd());
        };
    }

    private String localizedName(CommonCd code, Locale locale, String fallbackName) {
        String language = normalizeLanguage(locale);
        return switch (language) {
            case "de" -> firstNonBlank(code.getCdNmDe(), code.getCdNmEn(), code.getCdNmKo(), fallbackName, code.getCd());
            case "ko" -> firstNonBlank(code.getCdNmKo(), code.getCdNmEn(), code.getCdNmDe(), fallbackName, code.getCd());
            default -> firstNonBlank(code.getCdNmEn(), code.getCdNmDe(), code.getCdNmKo(), fallbackName, code.getCd());
        };
    }

    private String normalizeCd(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeRequiredName(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "English display name is required");
        }
        return value.trim();
    }

    private String normalizeLocalizedName(String value, String fallbackValue) {
        String fallback = normalizeRequiredName(fallbackValue);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private Integer normalizeOrder(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer normalizeOrder(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String blankToNullPreserve(String incomingValue, String currentValue) {
        if (incomingValue == null) {
            return currentValue;
        }
        return incomingValue.isBlank() ? null : incomingValue.trim();
    }

    private String normalizeLanguage(Locale locale) {
        return locale == null ? "en" : locale.getLanguage().toLowerCase(Locale.ROOT);
    }

    private String coalesce(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String normalizeSeedName(String fallbackName, String cd) {
        if (fallbackName != null && !fallbackName.isBlank()) {
            return fallbackName.trim();
        }
        return normalizeCd(cd);
    }
}

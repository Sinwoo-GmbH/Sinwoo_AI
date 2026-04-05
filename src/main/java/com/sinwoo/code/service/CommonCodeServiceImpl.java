package com.sinwoo.code.service;

import com.sinwoo.code.domain.CodeGroup;
import com.sinwoo.code.domain.CommonCode;
import com.sinwoo.code.dto.CodeGroupListResponse;
import com.sinwoo.code.dto.CodeGroupResponse;
import com.sinwoo.code.dto.CommonCodeListResponse;
import com.sinwoo.code.dto.CommonCodeResponse;
import com.sinwoo.code.dto.CreateCodeGroupRequest;
import com.sinwoo.code.dto.CreateCommonCodeRequest;
import com.sinwoo.code.dto.UpdateCodeGroupRequest;
import com.sinwoo.code.dto.UpdateCommonCodeRequest;
import com.sinwoo.code.repository.CodeGroupRepository;
import com.sinwoo.code.repository.CommonCodeRepository;
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
public class CommonCodeServiceImpl implements CommonCodeService {

    private final CodeGroupRepository codeGroupRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Override
    @Transactional
    public CodeGroupResponse createCodeGroup(CreateCodeGroupRequest request) {
        String normalizedGrpCd = normalizeCode(request.grpCd());
        if (codeGroupRepository.existsByGrpCdIgnoreCase(normalizedGrpCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code group already exists");
        }

        CodeGroup group = CodeGroup.create(
                normalizedGrpCd,
                normalizeLocalizedName(request.grpNmKo(), request.grpNmEn()),
                normalizeRequiredName(request.grpNmEn()),
                normalizeLocalizedName(request.grpNmDe(), request.grpNmEn()),
                normalizeYn(request.sysYn(), "N"),
                normalizeYn(request.useYn(), "Y"),
                normalizeOrder(request.dspOrd())
        );

        return toGroupResponse(codeGroupRepository.save(group));
    }

    @Override
    @Transactional
    public CodeGroupResponse updateCodeGroup(Long grpId, UpdateCodeGroupRequest request) {
        CodeGroup group = codeGroupRepository.findById(grpId)
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
    public CodeGroupListResponse getCodeGroups() {
        List<CodeGroupResponse> items = codeGroupRepository.findAllByOrderByDspOrdAscGrpCdAsc().stream()
                .map(this::toGroupResponse)
                .toList();
        return new CodeGroupListResponse(items.size(), items);
    }

    @Override
    @Transactional
    public CommonCodeResponse createCode(CreateCommonCodeRequest request) {
        CodeGroup group = codeGroupRepository.findByGrpCdIgnoreCase(normalizeCode(request.grpCd()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));

        String normalizedCd = normalizeCode(request.cd());
        if (commonCodeRepository.existsByGrpIdAndCdIgnoreCase(group.getId(), normalizedCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code already exists in group");
        }

        CommonCode code = CommonCode.create(
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

        return toCodeResponse(commonCodeRepository.save(code), group);
    }

    @Override
    @Transactional
    public CommonCodeResponse updateCode(Long cdId, UpdateCommonCodeRequest request) {
        CommonCode code = commonCodeRepository.findById(cdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Code not found"));
        CodeGroup group = codeGroupRepository.findById(code.getGrpId())
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

        return toCodeResponse(code, group);
    }

    @Override
    public CommonCodeListResponse getCodes(String grpCd) {
        List<CommonCodeResponse> items;
        if (grpCd == null || grpCd.isBlank()) {
            items = commonCodeRepository.findAllByOrderByGrpIdAscDspOrdAscIdAsc().stream()
                    .map(code -> toCodeResponse(code, codeGroupRepository.findById(code.getGrpId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"))))
                    .toList();
        } else {
            CodeGroup group = codeGroupRepository.findByGrpCdIgnoreCase(normalizeCode(grpCd))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));
            items = commonCodeRepository.findAllByGrpIdOrderByDspOrdAscIdAsc(group.getId()).stream()
                    .map(code -> toCodeResponse(code, group))
                    .toList();
        }
        return new CommonCodeListResponse(items.size(), items);
    }

    @Override
    public String resolveDisplayName(String grpCd, String cd, String fallbackName) {
        if (grpCd == null || grpCd.isBlank() || cd == null || cd.isBlank()) {
            return fallbackName;
        }

        return codeGroupRepository.findByGrpCdIgnoreCase(normalizeCode(grpCd))
                .flatMap(group -> commonCodeRepository.findByGrpIdAndCdIgnoreCase(group.getId(), normalizeCode(cd)))
                .map(code -> localizedName(code, LocaleContextHolder.getLocale(), fallbackName))
                .orElse(fallbackName);
    }

    @Override
    @Transactional
    public void ensureCode(String grpCd, String cd, String fallbackName) {
        if (grpCd == null || grpCd.isBlank() || cd == null || cd.isBlank()) {
            return;
        }

        CodeGroup group = codeGroupRepository.findByGrpCdIgnoreCase(normalizeCode(grpCd))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code group not found"));

        if (commonCodeRepository.existsByGrpIdAndCdIgnoreCase(group.getId(), normalizeCode(cd))) {
            return;
        }

        commonCodeRepository.save(CommonCode.create(
                group.getId(),
                normalizeCode(cd),
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

    private CodeGroupResponse toGroupResponse(CodeGroup group) {
        return CodeGroupResponse.from(group, localizedName(group, LocaleContextHolder.getLocale()));
    }

    private CommonCodeResponse toCodeResponse(CommonCode code, CodeGroup group) {
        return CommonCodeResponse.from(code, group.getGrpCd(), localizedName(code, LocaleContextHolder.getLocale(), code.getCd()));
    }

    private String localizedName(CodeGroup group, Locale locale) {
        String language = normalizeLanguage(locale);
        return switch (language) {
            case "de" -> firstNonBlank(group.getGrpNmDe(), group.getGrpNmEn(), group.getGrpNmKo(), group.getGrpCd());
            case "ko" -> firstNonBlank(group.getGrpNmKo(), group.getGrpNmEn(), group.getGrpNmDe(), group.getGrpCd());
            default -> firstNonBlank(group.getGrpNmEn(), group.getGrpNmDe(), group.getGrpNmKo(), group.getGrpCd());
        };
    }

    private String localizedName(CommonCode code, Locale locale, String fallbackName) {
        String language = normalizeLanguage(locale);
        return switch (language) {
            case "de" -> firstNonBlank(code.getCdNmDe(), code.getCdNmEn(), code.getCdNmKo(), fallbackName, code.getCd());
            case "ko" -> firstNonBlank(code.getCdNmKo(), code.getCdNmEn(), code.getCdNmDe(), fallbackName, code.getCd());
            default -> firstNonBlank(code.getCdNmEn(), code.getCdNmDe(), code.getCdNmKo(), fallbackName, code.getCd());
        };
    }

    private String normalizeCode(String value) {
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

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }

    private Integer normalizeOrder(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer normalizeOrder(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
        return normalizeCode(cd);
    }
}

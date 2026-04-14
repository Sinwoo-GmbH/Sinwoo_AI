package com.sinwoo.code.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sinwoo.code.domain.CodeGroup;
import com.sinwoo.code.domain.CommonCode;
import com.sinwoo.code.repository.CodeGroupRepository;
import com.sinwoo.code.repository.CommonCodeRepository;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommonCodeServiceImplTest {

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @InjectMocks
    private CommonCodeServiceImpl commonCodeService;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void resolveDisplayNameFallsBackToEnglishWhenRequestedLocaleLabelMissing() {
        CodeGroup group = CodeGroup.create("MNU_NM", "메뉴명", "Menu Name", "Menüname", "Y", "Y", 10);
        ReflectionTestUtils.setField(group, "id", 1L);

        CommonCode code = CommonCode.create(
                1L,
                "MNU_ADMIN_DASH",
                "개요",
                "Overview",
                "   ",
                null,
                null,
                null,
                "Y",
                10
        );

        given(codeGroupRepository.findByGrpCdIgnoreCase("MNU_NM")).willReturn(Optional.of(group));
        given(commonCodeRepository.findByGrpIdAndCdIgnoreCase(1L, "MNU_ADMIN_DASH")).willReturn(Optional.of(code));

        LocaleContextHolder.setLocale(Locale.GERMAN);

        String resolved = commonCodeService.resolveDisplayName("MNU_NM", "MNU_ADMIN_DASH", "Admin Dashboard");

        assertThat(resolved).isEqualTo("Overview");
    }

    @Test
    void resolveDisplayNameFallsBackToBaseNameWhenCodeDoesNotExist() {
        given(codeGroupRepository.findByGrpCdIgnoreCase("MNU_NM")).willReturn(Optional.empty());

        LocaleContextHolder.setLocale(Locale.GERMAN);

        String resolved = commonCodeService.resolveDisplayName("MNU_NM", "MNU_ADMIN_DASH", "Admin Dashboard");

        assertThat(resolved).isEqualTo("Admin Dashboard");
    }
}

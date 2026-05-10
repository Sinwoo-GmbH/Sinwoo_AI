package com.sinwoo.platform.code.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sinwoo.platform.code.domain.CdGroup;
import com.sinwoo.platform.code.domain.CommonCd;
import com.sinwoo.platform.code.repository.CdGroupRepository;
import com.sinwoo.platform.code.repository.CommonCdRepository;
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
class CommonCdServiceImplTest {

    @Mock
    private CdGroupRepository cdGroupRepository;

    @Mock
    private CommonCdRepository commonCdRepository;

    @InjectMocks
    private CommonCdServiceImpl commonCdService;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void resolveDspNmFallsBackToEnglishWhenRequestedLocaleLabelMissing() {
        CdGroup group = CdGroup.create("MNU_NM", "메뉴명", "Mnu Name", "Menüname", "Y", "Y", 10);
        ReflectionTestUtils.setField(group, "id", 1L);

        CommonCd code = CommonCd.create(
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

        given(cdGroupRepository.findByGrpCdIgnoreCase("MNU_NM")).willReturn(Optional.of(group));
        given(commonCdRepository.findByGrpIdAndCdIgnoreCase(1L, "MNU_ADMIN_DASH")).willReturn(Optional.of(code));

        LocaleContextHolder.setLocale(Locale.GERMAN);

        String resolved = commonCdService.resolveDspNm("MNU_NM", "MNU_ADMIN_DASH", "Admin Dashboard");

        assertThat(resolved).isEqualTo("Overview");
    }

    @Test
    void resolveDspNmFallsBackToBaseNameWhenCdDoesNotExist() {
        given(cdGroupRepository.findByGrpCdIgnoreCase("MNU_NM")).willReturn(Optional.empty());

        LocaleContextHolder.setLocale(Locale.GERMAN);

        String resolved = commonCdService.resolveDspNm("MNU_NM", "MNU_ADMIN_DASH", "Admin Dashboard");

        assertThat(resolved).isEqualTo("Admin Dashboard");
    }
}

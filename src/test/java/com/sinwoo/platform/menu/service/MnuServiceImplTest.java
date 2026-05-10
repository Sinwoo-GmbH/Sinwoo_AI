package com.sinwoo.platform.mnu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

import com.sinwoo.platform.auth.domain.Role;
import com.sinwoo.platform.auth.domain.UsrRole;
import com.sinwoo.platform.auth.repository.RoleRepository;
import com.sinwoo.platform.auth.repository.UsrRoleRepository;
import com.sinwoo.platform.billing.support.BillAccessPolicyService;
import com.sinwoo.platform.code.service.CommonCdService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.platform.mnu.domain.Mnu;
import com.sinwoo.platform.mnu.domain.RoleMnuAuth;
import com.sinwoo.platform.mnu.dto.MnuTreeResponse;
import com.sinwoo.platform.mnu.repository.MnuRepository;
import com.sinwoo.platform.mnu.repository.RoleMnuAuthRepository;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.user.domain.Usr;
import com.sinwoo.platform.user.repository.UsrRepository;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MnuServiceImplTest {

    @Mock
    private MnuRepository mnuRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMnuAuthRepository roleMnuAuthRepository;

    @Mock
    private UsrRoleRepository userRoleRepository;

    @Mock
    private UsrRepository usrRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BillAccessPolicyService billAccessPolicyService;

    @Mock
    private CommonCdService commonCdService;

    @InjectMocks
    private MnuServiceImpl mnuService;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getVisibleMnusForCurrentUsrFallsBackToPersistedUsrRolesWhenPrincipalRoleCdsMissing() {
        AuthenticatedUsr authenticatedUsr = new AuthenticatedUsr(
                1L,
                null,
                null,
                null,
                "CUSTOMER",
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "Sinwoo Admin",
                "CUSTOMER",
                "PASSWORD",
                null
        );

        Usr user = Usr.create(
                100L,
                200L,
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "hashed",
                "Sinwoo Admin",
                "de",
                null,
                "CUSTOMER",
                "PASSWORD",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        Role role = Role.create(
                "ROLE_CUSTOMER_ADMIN_MEMBER",
                "Customer Admin Member",
                "CUSTOMER",
                "CUSTOMER",
                "ADMIN",
                "MEMBER",
                "CUSTOMER",
                "STANDARD"
        );
        ReflectionTestUtils.setField(role, "id", 10L);

        Mnu mnu = Mnu.create(
                "MNU_CUSTOMER_DASH",
                "MNU_CUSTOMER_DASH",
                "Customer Dashboard",
                "CUSTOMER",
                null,
                "/customer/dashboard",
                "grid",
                10,
                "Y",
                null
        );
        ReflectionTestUtils.setField(mnu, "id", 20L);

        RoleMnuAuth roleMnuAuth = RoleMnuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");

        given(usrRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UsrRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(mnuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(mnu));
        given(roleMnuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(roleMnuAuth));
        given(commonCdService.resolveDspNm("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willReturn("Customer Dashboard");

        MnuTreeResponse response = mnuService.getVisibleMnusForCurrentUsr(authenticatedUsr, "CUSTOMER");

        assertThat(response.totCnt()).isEqualTo(1);
        assertThat(response.itemList()).hasSize(1);
        assertThat(response.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(response.itemList().getFirst().mnuNm()).isEqualTo("Customer Dashboard");
    }

    @Test
    void getVisibleMnusForCurrentUsrReturnsUnauthorizedWhenPrincipalCannotBeRecovered() {
        AuthenticatedUsr authenticatedUsr = new AuthenticatedUsr(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> mnuService.getVisibleMnusForCurrentUsr(authenticatedUsr, "CUSTOMER"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(apiException.getCd()).isEqualTo("AUTH_AUTHENTICATION_REQUIRED");
                });
    }

    @Test
    void getVisibleMnusByUsrKeepsMnuNodeWhenLocalizedNameResolvesBlank() {
        Usr user = Usr.create(
                100L,
                200L,
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "hashed",
                "Sinwoo Admin",
                "de",
                null,
                "CUSTOMER",
                "PASSWORD",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        Role role = Role.create(
                "ROLE_CUSTOMER_ADMIN_MEMBER",
                "Customer Admin Member",
                "CUSTOMER",
                "CUSTOMER",
                "ADMIN",
                "MEMBER",
                "CUSTOMER",
                "STANDARD"
        );
        ReflectionTestUtils.setField(role, "id", 10L);

        Mnu mnu = Mnu.create(
                "MNU_CUSTOMER_DASH",
                "MNU_CUSTOMER_DASH",
                "Customer Dashboard",
                "CUSTOMER",
                null,
                "/customer/dashboard",
                "grid",
                10,
                "Y",
                null
        );
        ReflectionTestUtils.setField(mnu, "id", 20L);

        RoleMnuAuth roleMnuAuth = RoleMnuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");

        given(usrRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UsrRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(mnuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(mnu));
        given(roleMnuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(roleMnuAuth));
        given(commonCdService.resolveDspNm("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willReturn("   ");

        MnuTreeResponse response = mnuService.getVisibleMnusByUsr(1L, "CUSTOMER");

        assertThat(response.totCnt()).isEqualTo(1);
        assertThat(response.itemList()).hasSize(1);
        assertThat(response.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(response.itemList().getFirst().mnuNm()).isEqualTo("Customer Dashboard");
    }

    @Test
    void getVisibleMnusByUsrKeepsSameMnuStructureAcrossLocales() {
        Usr user = Usr.create(
                100L,
                200L,
                "GGAMGANG",
                "ggamgang@sinwoo-itc.com",
                "hashed",
                "Sinwoo Admin",
                "de",
                null,
                "CUSTOMER",
                "PASSWORD",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        Role role = Role.create(
                "ROLE_CUSTOMER_ADMIN_MEMBER",
                "Customer Admin Member",
                "CUSTOMER",
                "CUSTOMER",
                "ADMIN",
                "MEMBER",
                "CUSTOMER",
                "STANDARD"
        );
        ReflectionTestUtils.setField(role, "id", 10L);

        Mnu rootMnu = Mnu.create(
                "MNU_CUSTOMER_DASH",
                "MNU_CUSTOMER_DASH",
                "Customer Dashboard",
                "CUSTOMER",
                null,
                "/customer/dashboard",
                "grid",
                10,
                "Y",
                null
        );
        ReflectionTestUtils.setField(rootMnu, "id", 20L);

        Mnu childMnu = Mnu.create(
                "MNU_CUSTOMER_PAY",
                "MNU_CUSTOMER_PAY",
                "Payment Center",
                "CUSTOMER",
                20L,
                "/customer/payments",
                "wallet",
                20,
                "Y",
                null
        );
        ReflectionTestUtils.setField(childMnu, "id", 21L);

        RoleMnuAuth rootAuth = RoleMnuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");
        RoleMnuAuth childAuth = RoleMnuAuth.create(10L, 21L, "Y", "N", "N", "N", "N", "N");

        given(usrRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UsrRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(mnuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(rootMnu, childMnu));
        given(roleMnuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(rootAuth, childAuth));
        given(commonCdService.resolveDspNm("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willAnswer(invocation -> switch (LocaleContextHolder.getLocale().getLanguage()) {
                    case "de" -> "Kunden-Dashboard";
                    case "ko" -> "고객 대시보드";
                    default -> "Customer Dashboard";
                });
        given(commonCdService.resolveDspNm("MNU_NM", "MNU_CUSTOMER_PAY", "Payment Center"))
                .willAnswer(invocation -> switch (LocaleContextHolder.getLocale().getLanguage()) {
                    case "de" -> "Zahlungscenter";
                    case "ko" -> "결제 센터";
                    default -> "Payment Center";
                });

        LocaleContextHolder.setLocale(Locale.KOREAN);
        MnuTreeResponse koResponse = mnuService.getVisibleMnusByUsr(1L, "CUSTOMER");

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        MnuTreeResponse enResponse = mnuService.getVisibleMnusByUsr(1L, "CUSTOMER");

        LocaleContextHolder.setLocale(Locale.GERMAN);
        MnuTreeResponse deResponse = mnuService.getVisibleMnusByUsr(1L, "CUSTOMER");

        assertThat(koResponse.itemList()).hasSize(1);
        assertThat(enResponse.itemList()).hasSize(1);
        assertThat(deResponse.itemList()).hasSize(1);

        assertThat(koResponse.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(enResponse.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(deResponse.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");

        assertThat(koResponse.itemList().getFirst().childList()).hasSize(1);
        assertThat(enResponse.itemList().getFirst().childList()).hasSize(1);
        assertThat(deResponse.itemList().getFirst().childList()).hasSize(1);

        assertThat(koResponse.itemList().getFirst().mnuNm()).isEqualTo("고객 대시보드");
        assertThat(enResponse.itemList().getFirst().mnuNm()).isEqualTo("Customer Dashboard");
        assertThat(deResponse.itemList().getFirst().mnuNm()).isEqualTo("Kunden-Dashboard");
    }
}

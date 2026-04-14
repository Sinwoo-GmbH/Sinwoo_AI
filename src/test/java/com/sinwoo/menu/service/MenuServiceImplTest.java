package com.sinwoo.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.domain.UserRole;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.auth.repository.UserRoleRepository;
import com.sinwoo.billing.support.BillingAccessPolicyService;
import com.sinwoo.code.service.CommonCodeService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.menu.domain.Menu;
import com.sinwoo.menu.domain.RoleMenuAuth;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.repository.MenuRepository;
import com.sinwoo.menu.repository.RoleMenuAuthRepository;
import com.sinwoo.user.domain.User;
import com.sinwoo.user.repository.UserRepository;
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
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMenuAuthRepository roleMenuAuthRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillingAccessPolicyService billingAccessPolicyService;

    @Mock
    private CommonCodeService commonCodeService;

    @InjectMocks
    private MenuServiceImpl menuService;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getVisibleMenusForCurrentUserFallsBackToPersistedUserRolesWhenPrincipalRoleCodesMissing() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                1L,
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

        User user = User.create(
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

        Menu menu = Menu.create(
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
        ReflectionTestUtils.setField(menu, "id", 20L);

        RoleMenuAuth roleMenuAuth = RoleMenuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UserRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(menu));
        given(roleMenuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(roleMenuAuth));
        given(commonCodeService.resolveDisplayName("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willReturn("Customer Dashboard");

        MenuTreeResponse response = menuService.getVisibleMenusForCurrentUser(authenticatedUser, "CUSTOMER");

        assertThat(response.totCnt()).isEqualTo(1);
        assertThat(response.itemList()).hasSize(1);
        assertThat(response.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(response.itemList().getFirst().mnuNm()).isEqualTo("Customer Dashboard");
    }

    @Test
    void getVisibleMenusForCurrentUserReturnsUnauthorizedWhenPrincipalCannotBeRecovered() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
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

        assertThatThrownBy(() -> menuService.getVisibleMenusForCurrentUser(authenticatedUser, "CUSTOMER"))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(apiException.getCode()).isEqualTo("AUTH_AUTHENTICATION_REQUIRED");
                });
    }

    @Test
    void getVisibleMenusByUsrKeepsMenuNodeWhenLocalizedNameResolvesBlank() {
        User user = User.create(
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

        Menu menu = Menu.create(
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
        ReflectionTestUtils.setField(menu, "id", 20L);

        RoleMenuAuth roleMenuAuth = RoleMenuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UserRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(menu));
        given(roleMenuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(roleMenuAuth));
        given(commonCodeService.resolveDisplayName("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willReturn("   ");

        MenuTreeResponse response = menuService.getVisibleMenusByUsr(1L, "CUSTOMER");

        assertThat(response.totCnt()).isEqualTo(1);
        assertThat(response.itemList()).hasSize(1);
        assertThat(response.itemList().getFirst().mnuCd()).isEqualTo("MNU_CUSTOMER_DASH");
        assertThat(response.itemList().getFirst().mnuNm()).isEqualTo("Customer Dashboard");
    }

    @Test
    void getVisibleMenusByUsrKeepsSameMenuStructureAcrossLocales() {
        User user = User.create(
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

        Menu rootMenu = Menu.create(
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
        ReflectionTestUtils.setField(rootMenu, "id", 20L);

        Menu childMenu = Menu.create(
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
        ReflectionTestUtils.setField(childMenu, "id", 21L);

        RoleMenuAuth rootAuth = RoleMenuAuth.create(10L, 20L, "Y", "N", "N", "N", "N", "N");
        RoleMenuAuth childAuth = RoleMenuAuth.create(10L, 21L, "Y", "N", "N", "N", "N", "N");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRoleRepository.findAllByUsrId(1L)).willReturn(List.of(UserRole.create(1L, 10L)));
        given(roleRepository.findAllById(List.of(10L))).willReturn(List.of(role));
        given(menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()).willReturn(List.of(rootMenu, childMenu));
        given(roleMenuAuthRepository.findAllByRoleIdIn(List.of(10L))).willReturn(List.of(rootAuth, childAuth));
        given(commonCodeService.resolveDisplayName("MNU_NM", "MNU_CUSTOMER_DASH", "Customer Dashboard"))
                .willAnswer(invocation -> switch (LocaleContextHolder.getLocale().getLanguage()) {
                    case "de" -> "Kunden-Dashboard";
                    case "ko" -> "고객 대시보드";
                    default -> "Customer Dashboard";
                });
        given(commonCodeService.resolveDisplayName("MNU_NM", "MNU_CUSTOMER_PAY", "Payment Center"))
                .willAnswer(invocation -> switch (LocaleContextHolder.getLocale().getLanguage()) {
                    case "de" -> "Zahlungscenter";
                    case "ko" -> "결제 센터";
                    default -> "Payment Center";
                });

        LocaleContextHolder.setLocale(Locale.KOREAN);
        MenuTreeResponse koResponse = menuService.getVisibleMenusByUsr(1L, "CUSTOMER");

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        MenuTreeResponse enResponse = menuService.getVisibleMenusByUsr(1L, "CUSTOMER");

        LocaleContextHolder.setLocale(Locale.GERMAN);
        MenuTreeResponse deResponse = menuService.getVisibleMenusByUsr(1L, "CUSTOMER");

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

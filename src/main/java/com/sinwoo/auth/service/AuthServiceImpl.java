package com.sinwoo.auth.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.domain.UserOauthIdentity;
import com.sinwoo.auth.domain.UserRole;
import com.sinwoo.auth.dto.AuthProviderListResponse;
import com.sinwoo.auth.dto.AuthProviderResponse;
import com.sinwoo.auth.dto.AuthTokenResponse;
import com.sinwoo.auth.dto.CredentialLoginRequest;
import com.sinwoo.auth.dto.CurrentUserResponse;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.auth.repository.UserOauthIdentityRepository;
import com.sinwoo.auth.repository.UserRoleRepository;
import com.sinwoo.common.security.AuthProperties;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.common.security.JwtTokenService;
import com.sinwoo.common.web.ApiException;
import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.repository.TenantRepository;
import com.sinwoo.user.domain.User;
import com.sinwoo.user.repository.UserRepository;
import com.sinwoo.auth.support.AuthErrorCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserOauthIdentityRepository userOauthIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthProperties authProperties;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    @Override
    public AuthProviderListResponse getOauthProviders() {
        List<AuthProviderResponse> items = new ArrayList<>();
        if (clientRegistrationRepository.isPresent()
                && clientRegistrationRepository.get() instanceof Iterable<?> iterable) {
            for (Object candidate : iterable) {
                if (candidate instanceof ClientRegistration registration
                        && registration.getClientId() != null
                        && !registration.getClientId().isBlank()) {
                    items.add(new AuthProviderResponse(
                            registration.getRegistrationId(),
                            registration.getClientName(),
                            "/api/v1/auth/oauth/authorize/" + registration.getRegistrationId()
                    ));
                }
            }
        }
        return new AuthProviderListResponse(items.size(), items);
    }

    @Override
    public AuthTokenResponse loginWithCredentials(CredentialLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.eml());
        Tenant tenant = resolveTenantByLoginEmail(normalizedEmail);

        User user = userRepository.findByTenantIdAndEmlIgnoreCase(tenant.getId(), normalizedEmail)
                .orElseThrow(() -> authException(AuthErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!"ACTIVE".equalsIgnoreCase(user.getStsCd())) {
            throw authException(AuthErrorCode.AUTH_USER_INACTIVE);
        }

        if (!passwordEncoder.matches(request.pwd(), user.getPwdHash())) {
            throw authException(AuthErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return issueTokens(buildAuthenticatedUser(user));
    }

    @Override
    @Transactional
    public AuthTokenResponse completeOauthLogin(String registrationId, String tenantCd, Map<String, Object> attributes) {
        String providerCd = registrationId.trim().toUpperCase(Locale.ROOT);
        String oauthSub = firstNonBlank(
                stringValue(attributes.get("sub")),
                stringValue(attributes.get("id"))
        );
        String eml = firstNonBlank(
                lowerValue(attributes.get("email")),
                lowerValue(attributes.get("preferred_username"))
        );
        String dspNm = firstNonBlank(
                stringValue(attributes.get("name")),
                stringValue(attributes.get("given_name")),
                deriveDisplayName(eml)
        );
        String emlVrfyYn = resolveEmailVerified(attributes);
        Tenant tenant = resolveTenantForOauthLogin(tenantCd, eml);

        if (oauthSub == null) {
            throw authException(AuthErrorCode.AUTH_OAUTH_SUBJECT_MISSING);
        }

        User user = userOauthIdentityRepository.findByOauthProvCdAndOauthSub(providerCd, oauthSub)
                .map(identity -> syncExistingIdentity(identity, eml, emlVrfyYn))
                .orElseGet(() -> linkOrProvisionUser(tenant, providerCd, oauthSub, eml, emlVrfyYn, dspNm));

        return issueTokens(buildAuthenticatedUser(user), providerCd);
    }

    @Override
    public CurrentUserResponse getCurrentUser(AuthenticatedUser authenticatedUser) {
        return new CurrentUserResponse(
                authenticatedUser.usrId(),
                authenticatedUser.tenantId(),
                authenticatedUser.coId(),
                authenticatedUser.lgnId(),
                authenticatedUser.eml(),
                authenticatedUser.dspNm(),
                authenticatedUser.authGrpCd(),
                authenticatedUser.authLvlCd(),
                authenticatedUser.roleCds()
        );
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {
        List<String> roleCds = resolveRoleCodes(user.getId());
        return new AuthenticatedUser(
                user.getId(),
                user.getTenantId(),
                user.getCoId(),
                user.getLgnId(),
                user.getEml(),
                user.getDspNm(),
                user.getAuthGrpCd(),
                user.getAuthLvlCd(),
                roleCds
        );
    }

    private AuthTokenResponse issueTokens(AuthenticatedUser authenticatedUser) {
        return issueTokens(authenticatedUser, "SINWOO");
    }

    private AuthTokenResponse issueTokens(AuthenticatedUser authenticatedUser, String providerCd) {
        return new AuthTokenResponse(
                jwtTokenService.issueAccessToken(authenticatedUser),
                jwtTokenService.getAccessTokenTtlSeconds(),
                jwtTokenService.issueRefreshToken(authenticatedUser),
                jwtTokenService.getRefreshTokenTtlSeconds(),
                "Bearer",
                providerCd,
                getCurrentUser(authenticatedUser)
        );
    }

    private User syncExistingIdentity(UserOauthIdentity identity, String eml, String emlVrfyYn) {
        User user = userRepository.findById(identity.getUsrId())
                .orElseThrow(() -> authException(AuthErrorCode.AUTH_LINKED_USER_NOT_FOUND));
        identity.markLogin(eml, emlVrfyYn, OffsetDateTime.now(ZoneOffset.UTC));
        return user;
    }

    private User linkOrProvisionUser(
            Tenant tenant,
            String providerCd,
            String oauthSub,
            String eml,
            String emlVrfyYn,
            String dspNm
    ) {
        if (eml == null) {
            throw authException(AuthErrorCode.AUTH_OAUTH_EMAIL_REQUIRED);
        }

        User user = userRepository.findByTenantIdAndEmlIgnoreCase(tenant.getId(), eml)
                .orElseGet(() -> createOauthUser(tenant, eml, dspNm));

        userOauthIdentityRepository.save(UserOauthIdentity.create(
                user.getId(),
                tenant.getId(),
                providerCd,
                oauthSub,
                eml,
                emlVrfyYn,
                OffsetDateTime.now(ZoneOffset.UTC)
        ));
        return user;
    }

    private User createOauthUser(Tenant tenant, String eml, String dspNm) {
        String lgnId = createUniqueLoginId(tenant.getId(), eml);
        User user = User.create(
                tenant.getId(),
                null,
                lgnId,
                eml,
                passwordEncoder.encode("OAUTH2-" + UUID.randomUUID()),
                dspNm,
                normalizeLocale(authProperties.defaultLoclCd()),
                null,
                normalizeAuthGroup(tenant.getTenantTpCd()),
                "OAUTH",
                "ACTIVE"
        );
        User savedUser = userRepository.save(user);
        Role defaultRole = resolveDefaultRole(tenant.getTenantTpCd());
        userRoleRepository.save(UserRole.create(savedUser.getId(), defaultRole.getId()));
        return savedUser;
    }

    private Role resolveDefaultRole(String tenantTpCd) {
        String roleCd = "INTERNAL".equalsIgnoreCase(tenantTpCd)
                ? authProperties.internalDefaultRoleCd()
                : authProperties.customerDefaultRoleCd();
        return roleRepository.findByRoleCd(roleCd)
                .orElseThrow(() -> authException(AuthErrorCode.AUTH_DEFAULT_ROLE_NOT_FOUND));
    }

    private Tenant resolveTenantForOauthLogin(String tenantCd, String eml) {
        if (tenantCd != null && !tenantCd.isBlank()) {
            return tenantRepository.findByTenantCdIgnoreCase(tenantCd.trim())
                    .orElseThrow(() -> authException(AuthErrorCode.AUTH_TENANT_NOT_FOUND));
        }
        return resolveTenantByLoginEmail(eml);
    }

    private List<String> resolveRoleCodes(Long usrId) {
        return userRoleRepository.findAllByUsrId(usrId).stream()
                .map(UserRole::getRoleId)
                .distinct()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> authException(AuthErrorCode.AUTH_ROLE_NOT_FOUND)))
                .map(Role::getRoleCd)
                .toList();
    }

    private Tenant resolveTenantByLoginEmail(String eml) {
        String normalizedEmail = normalizeEmail(eml);
        String emailDomain = extractEmailDomain(normalizedEmail);

        if (emailDomain != null) {
            Optional<Tenant> tenantByDomain = tenantRepository.findByEmlDomnIgnoreCase(emailDomain);
            if (tenantByDomain.isPresent()) {
                return tenantByDomain.get();
            }
        }

        List<User> matchedUsers = userRepository.findAllByEmlIgnoreCase(normalizedEmail);
        if (matchedUsers.size() == 1) {
            return tenantRepository.findById(matchedUsers.get(0).getTenantId())
                    .orElseThrow(() -> authException(AuthErrorCode.AUTH_TENANT_NOT_FOUND));
        }
        if (matchedUsers.size() > 1) {
            throw authException(AuthErrorCode.AUTH_TENANT_AMBIGUOUS);
        }
        throw authException(AuthErrorCode.AUTH_TENANT_UNRESOLVED);
    }

    private String createUniqueLoginId(Long tenantId, String eml) {
        String base = eml.substring(0, eml.indexOf('@'))
                .replaceAll("[^A-Za-z0-9._-]", "")
                .toUpperCase(Locale.ROOT);
        if (base.isBlank()) {
            base = "OAUTH";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByTenantIdAndLgnIdIgnoreCase(tenantId, candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private String normalizeEmail(String eml) {
        if (eml == null || eml.isBlank()) {
            throw authException(AuthErrorCode.AUTH_EMAIL_REQUIRED);
        }
        return eml.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        return locale.trim().toLowerCase(Locale.ROOT);
    }

    private String extractEmailDomain(String eml) {
        int atIndex = eml.indexOf('@');
        if (atIndex < 0 || atIndex == eml.length() - 1) {
            return null;
        }
        return eml.substring(atIndex + 1);
    }

    private String normalizeAuthGroup(String tenantTpCd) {
        return "INTERNAL".equalsIgnoreCase(tenantTpCd) ? "ADMIN" : "CUSTOMER";
    }

    private String resolveEmailVerified(Map<String, Object> attributes) {
        Object value = attributes.get("email_verified");
        if (value instanceof Boolean bool) {
            return bool ? "Y" : "N";
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value)) ? "Y" : "N";
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String lowerValue(Object value) {
        String stringValue = stringValue(value);
        return stringValue == null ? null : stringValue.toLowerCase(Locale.ROOT);
    }

    private String deriveDisplayName(String eml) {
        if (eml == null || !eml.contains("@")) {
            return "OAuth User";
        }
        return eml.substring(0, eml.indexOf('@'));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private ApiException authException(AuthErrorCode errorCode) {
        return new ApiException(errorCode.status(), errorCode.code(), errorCode.message());
    }
}

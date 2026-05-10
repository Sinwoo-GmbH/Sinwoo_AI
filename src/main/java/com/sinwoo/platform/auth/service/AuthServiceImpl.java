package com.sinwoo.platform.auth.service;

import com.sinwoo.platform.auth.domain.Role;
import com.sinwoo.platform.auth.domain.UsrOauthIdentity;
import com.sinwoo.platform.auth.domain.UsrRole;
import com.sinwoo.platform.auth.dto.AuthProviderListResponse;
import com.sinwoo.platform.auth.dto.AuthProviderResponse;
import com.sinwoo.platform.auth.dto.AuthTokenResponse;
import com.sinwoo.platform.auth.dto.CredKeyResponse;
import com.sinwoo.platform.auth.dto.CredLoginRequest;
import com.sinwoo.platform.auth.dto.CurrentUsrResponse;
import com.sinwoo.platform.auth.repository.RoleRepository;
import com.sinwoo.platform.auth.repository.UsrOauthIdentityRepository;
import com.sinwoo.platform.auth.repository.UsrRoleRepository;
import com.sinwoo.platform.auth.support.AuthBizConst;
import com.sinwoo.platform.billing.support.BillAccessPolicyService;
import com.sinwoo.common.util.CommonBizConst;
import com.sinwoo.common.security.AuthProperties;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.security.CredEncryptService;
import com.sinwoo.common.security.JwtTokenService;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.user.domain.Usr;
import com.sinwoo.platform.user.repository.UsrRepository;
import com.sinwoo.platform.auth.support.AuthErrorCd;
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
    private final UsrRepository usrRepository;
    private final RoleRepository roleRepository;
    private final UsrRoleRepository userRoleRepository;
    private final UsrOauthIdentityRepository userOauthIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CredEncryptService credEncryptService;
    private final AuthProperties authProperties;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;
    private final BillAccessPolicyService billAccessPolicyService;

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
    public CredKeyResponse getCredKey() {
        try {
            return credEncryptService.getCredKey();
        } catch (RuntimeException exception) {
            throw authException(AuthErrorCd.AUTH_CREDENTIAL_KEY_UNAVAILABLE);
        }
    }

    @Override
    public AuthTokenResponse loginWithCreds(CredLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.eml());
        Tenant tenant = resolveTenantByLoginEmail(normalizedEmail);
        String rawPassword = resolveRawPassword(request);

        Usr user = usrRepository.findByTenantIdAndEmlIgnoreCase(tenant.getId(), normalizedEmail)
                .orElseThrow(() -> authException(AuthErrorCd.AUTH_INVALID_CREDENTIALS));

        if (!CommonBizConst.STS_CD_ACTIVE.equalsIgnoreCase(user.getStsCd())) {
            throw authException(AuthErrorCd.AUTH_USER_INACTIVE);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPwdHash())) {
            throw authException(AuthErrorCd.AUTH_INVALID_CREDENTIALS);
        }

        return issueTokens(buildAuthenticatedUsr(user));
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
            throw authException(AuthErrorCd.AUTH_OAUTH_SUBJECT_MISSING);
        }

        Usr user = userOauthIdentityRepository.findByOauthProvCdAndOauthSub(providerCd, oauthSub)
                .map(identity -> syncExistingIdentity(identity, eml, emlVrfyYn))
                .orElseGet(() -> linkOrProvisionUsr(tenant, providerCd, oauthSub, eml, emlVrfyYn, dspNm));

        return issueTokens(buildAuthenticatedUsr(user), providerCd);
    }

    @Override
    public CurrentUsrResponse getCurrentUsr(AuthenticatedUsr authenticatedUsr) {
        return new CurrentUsrResponse(
                authenticatedUsr.usrId(),
                authenticatedUsr.tenantId(),
                authenticatedUsr.tenantCd(),
                authenticatedUsr.coId(),
                authenticatedUsr.tenantTpCd(),
                authenticatedUsr.lgnId(),
                authenticatedUsr.eml(),
                authenticatedUsr.dspNm(),
                authenticatedUsr.authGrpCd(),
                authenticatedUsr.authLvlCd(),
                authenticatedUsr.roleCds(),
                billAccessPolicyService.hasPaidAdminAccess(authenticatedUsr.tenantId()) ? "Y" : "N"
        );
    }

    private AuthenticatedUsr buildAuthenticatedUsr(Usr user) {
        List<String> roleCds = resolveRoleCds(user.getId());
        Tenant tenant = resolveTenant(user.getTenantId());
        return new AuthenticatedUsr(
                user.getId(),
                user.getTenantId(),
                tenant.getTenantCd(),
                user.getCoId(),
                tenant.getTenantTpCd(),
                user.getLgnId(),
                user.getEml(),
                user.getDspNm(),
                user.getAuthGrpCd(),
                user.getAuthLvlCd(),
                roleCds
        );
    }

    private AuthTokenResponse issueTokens(AuthenticatedUsr authenticatedUsr) {
        return issueTokens(authenticatedUsr, AuthBizConst.AUTH_PROV_CD_SINWOO);
    }

    private AuthTokenResponse issueTokens(AuthenticatedUsr authenticatedUsr, String providerCd) {
        return new AuthTokenResponse(
                jwtTokenService.issueAccessToken(authenticatedUsr),
                jwtTokenService.getAccessTokenTtlSeconds(),
                jwtTokenService.issueRefreshToken(authenticatedUsr),
                jwtTokenService.getRefreshTokenTtlSeconds(),
                "Bearer",
                providerCd,
                getCurrentUsr(authenticatedUsr)
        );
    }

    private Usr syncExistingIdentity(UsrOauthIdentity identity, String eml, String emlVrfyYn) {
        Usr user = usrRepository.findById(identity.getUsrId())
                .orElseThrow(() -> authException(AuthErrorCd.AUTH_LINKED_USER_NOT_FOUND));
        identity.markLogin(eml, emlVrfyYn, OffsetDateTime.now(ZoneOffset.UTC));
        return user;
    }

    private Usr linkOrProvisionUsr(
            Tenant tenant,
            String providerCd,
            String oauthSub,
            String eml,
            String emlVrfyYn,
            String dspNm
    ) {
        if (eml == null) {
            throw authException(AuthErrorCd.AUTH_OAUTH_EMAIL_REQUIRED);
        }

        Usr user = usrRepository.findByTenantIdAndEmlIgnoreCase(tenant.getId(), eml)
                .orElseGet(() -> createOauthUsr(tenant, eml, dspNm));

        userOauthIdentityRepository.save(UsrOauthIdentity.create(
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

    private Usr createOauthUsr(Tenant tenant, String eml, String dspNm) {
        String lgnId = createUniqueLoginId(tenant.getId(), eml);
        Usr user = Usr.create(
                tenant.getId(),
                null,
                lgnId,
                eml,
                passwordEncoder.encode("OAUTH2-" + UUID.randomUUID()),
                dspNm,
                normalizeLocale(authProperties.defaultLoclCd()),
                null,
                normalizeAuthGroup(tenant.getTenantTpCd()),
                AuthBizConst.AUTH_PROV_CD_OAUTH,
                CommonBizConst.STS_CD_ACTIVE
        );
        Usr savedUsr = usrRepository.save(user);
        Role defaultRole = resolveDefaultRole(tenant.getTenantTpCd());
        userRoleRepository.save(UsrRole.create(savedUsr.getId(), defaultRole.getId()));
        return savedUsr;
    }

    private Role resolveDefaultRole(String tenantTpCd) {
        String roleCd = AuthBizConst.TENANT_TP_CD_INTERNAL.equalsIgnoreCase(tenantTpCd)
                ? authProperties.internalDefaultRoleCd()
                : authProperties.customerDefaultRoleCd();
        return roleRepository.findByRoleCd(roleCd)
                .orElseThrow(() -> authException(AuthErrorCd.AUTH_DEFAULT_ROLE_NOT_FOUND));
    }

    private Tenant resolveTenantForOauthLogin(String tenantCd, String eml) {
        if (tenantCd != null && !tenantCd.isBlank()) {
            return tenantRepository.findByTenantCdIgnoreCase(tenantCd.trim())
                    .orElseThrow(() -> authException(AuthErrorCd.AUTH_TENANT_NOT_FOUND));
        }
        return resolveTenantByLoginEmail(eml);
    }

    private List<String> resolveRoleCds(Long usrId) {
        return userRoleRepository.findAllByUsrId(usrId).stream()
                .map(UsrRole::getRoleId)
                .distinct()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> authException(AuthErrorCd.AUTH_ROLE_NOT_FOUND)))
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

        List<Usr> matchedUsrs = usrRepository.findAllByEmlIgnoreCase(normalizedEmail);
        if (matchedUsrs.size() == 1) {
            return tenantRepository.findById(matchedUsrs.get(0).getTenantId())
                    .orElseThrow(() -> authException(AuthErrorCd.AUTH_TENANT_NOT_FOUND));
        }
        if (matchedUsrs.size() > 1) {
            throw authException(AuthErrorCd.AUTH_TENANT_AMBIGUOUS);
        }
        throw authException(AuthErrorCd.AUTH_TENANT_UNRESOLVED);
    }

    private String createUniqueLoginId(Long tenantId, String eml) {
        String base = eml.substring(0, eml.indexOf('@'))
                .replaceAll("[^A-Za-z0-9._-]", "")
                .toUpperCase(Locale.ROOT);
        if (base.isBlank()) {
            base = AuthBizConst.LGN_ID_OAUTH_BASE;
        }

        String candidate = base;
        int suffix = 1;
        while (usrRepository.existsByTenantIdAndLgnIdIgnoreCase(tenantId, candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private String normalizeEmail(String eml) {
        if (eml == null || eml.isBlank()) {
            throw authException(AuthErrorCd.AUTH_EMAIL_REQUIRED);
        }
        return eml.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveRawPassword(CredLoginRequest request) {
        if (request.pwdEnc() == null || request.pwdEnc().isBlank()) {
            throw authException(AuthErrorCd.AUTH_PASSWORD_PAYLOAD_INVALID);
        }

        try {
            String decrypted = credEncryptService.decryptPassword(request.pwdEnc());
            if (decrypted.isBlank()) {
                throw authException(AuthErrorCd.AUTH_PASSWORD_PAYLOAD_INVALID);
            }
            return decrypted;
        } catch (IllegalArgumentException exception) {
            throw authException(AuthErrorCd.AUTH_PASSWORD_PAYLOAD_INVALID);
        }
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return AuthBizConst.LOCL_CD_EN;
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
        return AuthBizConst.TENANT_TP_CD_INTERNAL.equalsIgnoreCase(tenantTpCd)
                ? AuthBizConst.AUTH_GRP_CD_ADMIN
                : AuthBizConst.AUTH_GRP_CD_CUSTOMER;
    }

    private Tenant resolveTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> authException(AuthErrorCd.AUTH_TENANT_NOT_FOUND));
    }

    private String resolveEmailVerified(Map<String, Object> attributes) {
        Object value = attributes.get("email_verified");
        if (value instanceof Boolean bool) {
            return bool ? CommonBizConst.YN_Y : CommonBizConst.YN_N;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value))
                ? CommonBizConst.YN_Y
                : CommonBizConst.YN_N;
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
            return "OAuth Usr";
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

    private ApiException authException(AuthErrorCd errorCd) {
        return new ApiException(errorCd.status(), errorCd.code(), errorCd.message());
    }
}

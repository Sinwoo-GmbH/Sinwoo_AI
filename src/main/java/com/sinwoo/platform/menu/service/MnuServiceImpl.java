package com.sinwoo.platform.menu.service;

import static com.sinwoo.common.util.StringNormalizer.blankToNull;
import static com.sinwoo.common.util.StringNormalizer.normalizeYn;

import com.sinwoo.platform.auth.domain.Role;
import com.sinwoo.platform.auth.repository.RoleRepository;
import com.sinwoo.platform.auth.repository.UsrRoleRepository;
import com.sinwoo.platform.auth.support.AuthErrorCd;
import com.sinwoo.platform.billing.support.BillAccessPolicyService;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.common.util.CommonBizConst;
import com.sinwoo.common.exception.ApiException;
import com.sinwoo.platform.code.service.CommonCdService;
import com.sinwoo.platform.code.support.CommonCdGroupCd;
import com.sinwoo.platform.menu.domain.Mnu;
import com.sinwoo.platform.menu.domain.RoleMnuAuth;
import com.sinwoo.platform.menu.dto.CreateMnuRequest;
import com.sinwoo.platform.menu.dto.MnuListResponse;
import com.sinwoo.platform.menu.dto.MnuNodeResponse;
import com.sinwoo.platform.menu.dto.MnuResponse;
import com.sinwoo.platform.menu.dto.MnuTreeResponse;
import com.sinwoo.platform.menu.repository.MnuRepository;
import com.sinwoo.platform.menu.repository.RoleMnuAuthRepository;
import com.sinwoo.platform.menu.support.MnuBizConst;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.user.domain.Usr;
import com.sinwoo.platform.user.repository.UsrRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MnuServiceImpl implements MnuService {

    private final MnuRepository mnuRepository;
    private final RoleRepository roleRepository;
    private final RoleMnuAuthRepository roleMnuAuthRepository;
    private final UsrRoleRepository userRoleRepository;
    private final UsrRepository usrRepository;
    private final TenantRepository tenantRepository;
    private final BillAccessPolicyService billAccessPolicyService;
    private final CommonCdService commonCdService;

    @Override
    @Transactional
    public MnuResponse createMnu(CreateMnuRequest request) {
        String normalizedMnuCd = request.mnuCd().trim().toUpperCase();

        if (mnuRepository.existsByMnuCdIgnoreCase(normalizedMnuCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mnu code already exists");
        }

        if (request.upMnuId() != null && !mnuRepository.existsById(request.upMnuId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent mnu not found");
        }

        Mnu mnu = Mnu.create(
                normalizedMnuCd,
                normalizeCdOrDefault(request.mnuNmCd(), normalizedMnuCd),
                request.mnuNm().trim(),
                normalizeScope(request.mnuScopeCd()),
                request.upMnuId(),
                blankToNull(request.pathUri()),
                blankToNull(request.iconNm()),
                request.dspOrd() == null ? 0 : request.dspOrd(),
                normalizeYn(request.useYn(), "Y"),
                blankToNull(normalizeGateCd(request.billGateCd()))
        );

        Mnu savedMnu = mnuRepository.save(mnu);
        commonCdService.ensureCd(CommonCdGroupCd.MNU_NM, savedMnu.getMnuNmCd(), savedMnu.getMnuNm());
        return toMnuResponse(savedMnu);
    }

    @Override
    public MnuListResponse getMnus(String mnuScopeCd) {
        List<MnuResponse> items = (mnuScopeCd == null || mnuScopeCd.isBlank()
                ? mnuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()
                : mnuRepository.findAllByMnuScopeCdOrderByDspOrdAscIdAsc(normalizeScope(mnuScopeCd)))
                .stream()
                .map(this::toMnuResponse)
                .toList();

        return new MnuListResponse(items.size(), items);
    }

    @Override
    public MnuTreeResponse getVisibleMnus(List<String> roleCds, String mnuScopeCd) {
        if (roleCds == null || roleCds.isEmpty()) {
            return new MnuTreeResponse(0, List.of());
        }

        List<String> normalizedRoleCds = roleCds.stream()
                .map(value -> value.trim().toUpperCase())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        List<Role> roles = roleRepository.findByRoleCdIn(normalizedRoleCds);
        return buildVisibleMnus(roles, mnuScopeCd, null);
    }

    @Override
    public MnuTreeResponse getVisibleMnusByUsr(Long usrId, String mnuScopeCd) {
        if (usrId == null) {
            return new MnuTreeResponse(0, List.of());
        }

        Usr user = findUsr(usrId);
        if (user == null) {
            return new MnuTreeResponse(0, List.of());
        }

        List<Role> roles = findRolesByUsrId(usrId);
        return buildVisibleMnus(roles, mnuScopeCd, user.getTenantId());
    }

    @Override
    public MnuTreeResponse getVisibleMnusByLgnId(String tenantCd, String lgnId, String mnuScopeCd) {
        if (tenantCd == null || tenantCd.isBlank() || lgnId == null || lgnId.isBlank()) {
            return new MnuTreeResponse(0, List.of());
        }

        Tenant tenant = tenantRepository.findByTenantCdIgnoreCase(tenantCd.trim())
                .orElse(null);
        if (tenant == null) {
            return new MnuTreeResponse(0, List.of());
        }

        Usr user = usrRepository.findByTenantIdAndLgnIdIgnoreCase(tenant.getId(), lgnId.trim())
                .orElse(null);
        if (user == null) {
            return new MnuTreeResponse(0, List.of());
        }

        List<Role> roles = findRolesByUsrId(user.getId());
        return buildVisibleMnus(roles, mnuScopeCd, user.getTenantId());
    }

    @Override
    public MnuTreeResponse getVisibleMnusForCurrentUsr(AuthenticatedUsr authenticatedUsr, String mnuScopeCd) {
        if (authenticatedUsr == null) {
            throw invalidAuthenticationContext();
        }

        String resolvedScope = mnuScopeCd;
        if (resolvedScope == null || resolvedScope.isBlank()) {
            resolvedScope = MnuBizConst.MNU_SCOPE_CD_ADMIN.equalsIgnoreCase(authenticatedUsr.authGrpCd())
                    ? MnuBizConst.MNU_SCOPE_CD_ADMIN
                    : MnuBizConst.MNU_SCOPE_CD_CUSTOMER;
        }

        if (authenticatedUsr.usrId() != null) {
            Usr persistedUsr = findUsr(authenticatedUsr.usrId());
            if (persistedUsr == null) {
                throw invalidAuthenticationContext();
            }
            return getVisibleMnusByUsr(authenticatedUsr.usrId(), resolvedScope);
        }

        List<Role> roles = findRolesByRoleCds(authenticatedUsr.roleCds());
        if (roles.isEmpty()) {
            throw invalidAuthenticationContext();
        }

        return buildVisibleMnus(
                roles,
                resolvedScope,
                authenticatedUsr.tenantId()
        );
    }

    private Usr findUsr(Long usrId) {
        if (usrId == null) {
            return null;
        }
        return usrRepository.findById(usrId).orElse(null);
    }

    private List<Role> findRolesByUsrId(Long usrId) {
        if (usrId == null) {
            return List.of();
        }

        List<Long> roleIds = userRoleRepository.findAllByUsrId(usrId).stream()
                .map(userRole -> userRole.getRoleId())
                .filter(roleId -> roleId != null)
                .distinct()
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findAllById(roleIds);
    }

    private List<Role> findRolesByRoleCds(Collection<String> roleCds) {
        if (roleCds == null || roleCds.isEmpty()) {
            return List.of();
        }

        List<String> normalizedRoleCds = roleCds.stream()
                .filter(roleCd -> roleCd != null && !roleCd.isBlank())
                .map(roleCd -> roleCd.trim().toUpperCase())
                .distinct()
                .toList();

        if (normalizedRoleCds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findByRoleCdIn(normalizedRoleCds);
    }

    private ApiException invalidAuthenticationContext() {
        return new ApiException(
                AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.status(),
                AuthErrorCd.AUTH_AUTHENTICATION_REQUIRED.code(),
                "Your sign-in session is incomplete. Please sign in again."
        );
    }

    private MnuTreeResponse buildVisibleMnus(List<Role> roles, String mnuScopeCd, Long tenantId) {
        if (roles.isEmpty()) {
            return new MnuTreeResponse(0, List.of());
        }

        List<Long> roleIds = roles.stream()
                .map(Role::getId)
                .filter(roleId -> roleId != null)
                .distinct()
                .toList();

        if (roleIds.isEmpty()) {
            return new MnuTreeResponse(0, List.of());
        }

        List<Mnu> allMnus = mnuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc();
        Map<Long, Mnu> mnuById = new LinkedHashMap<>();
        allMnus.forEach(mnu -> mnuById.put(mnu.getId(), mnu));

        Set<Long> visibleIds = roleMnuAuthRepository.findAllByRoleIdIn(
                        roleIds
                ).stream()
                .filter(auth -> CommonBizConst.YN_Y.equals(auth.getViewYn()))
                .map(RoleMnuAuth::getMnuId)
                .filter(mnuId -> isBillingGateSatisfied(mnuById.get(mnuId), roles, tenantId))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        Set<Long> effectiveIds = new LinkedHashSet<>();
        for (Long visibleId : visibleIds) {
            addAncestors(effectiveIds, mnuById, visibleId);
        }

        List<MnuNodeResponse> tree = buildMnuTree(
                allMnus.stream()
                        .filter(mnu -> effectiveIds.contains(mnu.getId()))
                        .filter(mnu -> matchesScope(mnu, mnuScopeCd))
                        .toList()
        );

        return new MnuTreeResponse(tree.size(), tree);
    }

    private void addAncestors(Set<Long> effectiveIds, Map<Long, Mnu> mnuById, Long mnuId) {
        Long cursor = mnuId;
        while (cursor != null && effectiveIds.add(cursor)) {
            Mnu mnu = mnuById.get(cursor);
            if (mnu == null) {
                return;
            }
            cursor = mnu.getUpMnuId();
        }
    }

    private List<MnuNodeResponse> buildMnuTree(List<Mnu> mnus) {
        Map<Long, MnuNodeResponse> nodeById = new LinkedHashMap<>();
        mnus.forEach(mnu -> nodeById.put(mnu.getId(), toMnuNodeResponse(mnu)));

        List<MnuNodeResponse> roots = new ArrayList<>();
        for (Mnu mnu : mnus) {
            MnuNodeResponse node = nodeById.get(mnu.getId());
            if (mnu.getUpMnuId() == null || !nodeById.containsKey(mnu.getUpMnuId())) {
                roots.add(node);
                continue;
            }
            nodeById.get(mnu.getUpMnuId()).childList().add(node);
        }
        return roots;
    }

    private boolean matchesScope(Mnu mnu, String mnuScopeCd) {
        if (mnuScopeCd == null || mnuScopeCd.isBlank()) {
            return true;
        }
        String normalizedScope = normalizeScope(mnuScopeCd);
        return normalizedScope.equals(mnu.getMnuScopeCd()) || MnuBizConst.MNU_SCOPE_CD_COMMON.equals(mnu.getMnuScopeCd());
    }

    private String normalizeScope(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeCdOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeGateCd(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private boolean isBillingGateSatisfied(Mnu mnu, List<Role> roles, Long tenantId) {
        if (mnu == null || mnu.getBillGateCd() == null || mnu.getBillGateCd().isBlank()) {
            return true;
        }

        // Platform Admin은 모든 BILL_GATE 통과 (시스템 최상위 권한)
        if (hasPlatformAdminRole(roles)) {
            return true;
        }

        if (MnuBizConst.BILL_GATE_CD_PAID_CUSTOMER_ADMIN.equals(mnu.getBillGateCd())) {
            return hasCustomerAdminRole(roles) && billAccessPolicyService.hasPaidAdminAccess(tenantId);
        }

        return true;
    }

    private boolean hasPlatformAdminRole(List<Role> roles) {
        return roles.stream().anyMatch(role ->
                "PLT".equalsIgnoreCase(role.getRoleD1Cd())
                        || "PADM".equalsIgnoreCase(role.getRoleCd())
        );
    }

    private boolean hasCustomerAdminRole(List<Role> roles) {
        return roles.stream().anyMatch(role ->
                MnuBizConst.ROLE_D1_CD_CUSTOMER.equalsIgnoreCase(role.getRoleD1Cd())
                        && role.getRoleD2Cd() != null
                        && !MnuBizConst.ROLE_D2_CD_USER.equalsIgnoreCase(role.getRoleD2Cd())
        );
    }

    private MnuResponse toMnuResponse(Mnu mnu) {
        return MnuResponse.from(mnu, resolveMnuName(mnu));
    }

    private MnuNodeResponse toMnuNodeResponse(Mnu mnu) {
        return MnuNodeResponse.from(mnu, resolveMnuName(mnu));
    }

    private String resolveMnuName(Mnu mnu) {
        return firstNonBlank(
                commonCdService.resolveDspNm(CommonCdGroupCd.MNU_NM, mnu.getMnuNmCd(), resolveBaseMnuName(mnu)),
                resolveBaseMnuName(mnu),
                mnu.getMnuCd()
        );
    }

    private String resolveBaseMnuName(Mnu mnu) {
        return firstNonBlank(mnu.getMnuNm(), mnu.getMnuNmCd(), mnu.getMnuCd());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}

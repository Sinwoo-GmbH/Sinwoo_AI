package com.sinwoo.menu.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.auth.repository.UserRoleRepository;
import com.sinwoo.billing.support.BillingAccessPolicyService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.code.service.CommonCodeService;
import com.sinwoo.menu.domain.Menu;
import com.sinwoo.menu.domain.RoleMenuAuth;
import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuNodeResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.repository.MenuRepository;
import com.sinwoo.menu.repository.RoleMenuAuthRepository;
import com.sinwoo.user.domain.User;
import com.sinwoo.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuAuthRepository roleMenuAuthRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final BillingAccessPolicyService billingAccessPolicyService;
    private final CommonCodeService commonCodeService;

    @Override
    @Transactional
    public MenuResponse createMenu(CreateMenuRequest request) {
        String normalizedMnuCd = request.mnuCd().trim().toUpperCase();

        if (menuRepository.existsByMnuCdIgnoreCase(normalizedMnuCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Menu code already exists");
        }

        if (request.upMnuId() != null && !menuRepository.existsById(request.upMnuId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent menu not found");
        }

        Menu menu = Menu.create(
                normalizedMnuCd,
                normalizeCodeOrDefault(request.mnuNmCd(), normalizedMnuCd),
                request.mnuNm().trim(),
                normalizeScope(request.mnuScopeCd()),
                request.upMnuId(),
                blankToNull(request.pathUri()),
                blankToNull(request.iconNm()),
                request.dspOrd() == null ? 0 : request.dspOrd(),
                normalizeYn(request.useYn(), "Y"),
                blankToNull(normalizeGateCode(request.billGateCd()))
        );

        Menu savedMenu = menuRepository.save(menu);
        commonCodeService.ensureCode("MNU_NM", savedMenu.getMnuNmCd(), savedMenu.getMnuNm());
        return toMenuResponse(savedMenu);
    }

    @Override
    public MenuListResponse getMenus(String mnuScopeCd) {
        List<MenuResponse> items = (mnuScopeCd == null || mnuScopeCd.isBlank()
                ? menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()
                : menuRepository.findAllByMnuScopeCdOrderByDspOrdAscIdAsc(normalizeScope(mnuScopeCd)))
                .stream()
                .map(this::toMenuResponse)
                .toList();

        return new MenuListResponse(items.size(), items);
    }

    @Override
    public MenuTreeResponse getVisibleMenus(List<String> roleCds, String mnuScopeCd) {
        if (roleCds == null || roleCds.isEmpty()) {
            return new MenuTreeResponse(0, List.of());
        }

        List<String> normalizedRoleCds = roleCds.stream()
                .map(value -> value.trim().toUpperCase())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        List<Role> roles = roleRepository.findByRoleCdIn(normalizedRoleCds);
        return buildVisibleMenus(roles, mnuScopeCd, null);
    }

    @Override
    public MenuTreeResponse getVisibleMenusByUsr(Long usrId, String mnuScopeCd) {
        if (usrId == null) {
            return new MenuTreeResponse(0, List.of());
        }

        User user = userRepository.findById(usrId).orElse(null);
        if (user == null) {
            return new MenuTreeResponse(0, List.of());
        }

        List<Long> roleIds = userRoleRepository.findAllByUsrId(usrId).stream()
                .map(userRole -> userRole.getRoleId())
                .distinct()
                .toList();

        if (roleIds.isEmpty()) {
            return new MenuTreeResponse(0, List.of());
        }

        List<Role> roles = roleRepository.findAllById(roleIds);
        return buildVisibleMenus(roles, mnuScopeCd, user.getTenantId());
    }

    @Override
    public MenuTreeResponse getVisibleMenusForCurrentUser(AuthenticatedUser authenticatedUser, String mnuScopeCd) {
        if (authenticatedUser == null) {
            return new MenuTreeResponse(0, List.of());
        }

        String resolvedScope = mnuScopeCd;
        if (resolvedScope == null || resolvedScope.isBlank()) {
            resolvedScope = "ADMIN".equalsIgnoreCase(authenticatedUser.authGrpCd()) ? "ADMIN" : "CUSTOMER";
        }

        return buildVisibleMenus(
                roleRepository.findByRoleCdIn(authenticatedUser.roleCds()),
                resolvedScope,
                authenticatedUser.tenantId()
        );
    }

    private MenuTreeResponse buildVisibleMenus(List<Role> roles, String mnuScopeCd, Long tenantId) {
        if (roles.isEmpty()) {
            return new MenuTreeResponse(0, List.of());
        }

        List<Menu> allMenus = menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc();
        Map<Long, Menu> menuById = new LinkedHashMap<>();
        allMenus.forEach(menu -> menuById.put(menu.getId(), menu));

        Set<Long> visibleIds = roleMenuAuthRepository.findAllByRoleIdIn(
                        roles.stream().map(Role::getId).toList()
                ).stream()
                .filter(auth -> "Y".equals(auth.getViewYn()))
                .map(RoleMenuAuth::getMnuId)
                .filter(menuId -> isBillingGateSatisfied(menuById.get(menuId), roles, tenantId))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        Set<Long> effectiveIds = new LinkedHashSet<>();
        for (Long visibleId : visibleIds) {
            addAncestors(effectiveIds, menuById, visibleId);
        }

        List<MenuNodeResponse> tree = buildMenuTree(
                allMenus.stream()
                        .filter(menu -> effectiveIds.contains(menu.getId()))
                        .filter(menu -> matchesScope(menu, mnuScopeCd))
                        .toList()
        );

        return new MenuTreeResponse(tree.size(), tree);
    }

    private void addAncestors(Set<Long> effectiveIds, Map<Long, Menu> menuById, Long menuId) {
        Long cursor = menuId;
        while (cursor != null && effectiveIds.add(cursor)) {
            Menu menu = menuById.get(cursor);
            if (menu == null) {
                return;
            }
            cursor = menu.getUpMnuId();
        }
    }

    private List<MenuNodeResponse> buildMenuTree(List<Menu> menus) {
        Map<Long, MenuNodeResponse> nodeById = new LinkedHashMap<>();
        menus.forEach(menu -> nodeById.put(menu.getId(), toMenuNodeResponse(menu)));

        List<MenuNodeResponse> roots = new ArrayList<>();
        for (Menu menu : menus) {
            MenuNodeResponse node = nodeById.get(menu.getId());
            if (menu.getUpMnuId() == null || !nodeById.containsKey(menu.getUpMnuId())) {
                roots.add(node);
                continue;
            }
            nodeById.get(menu.getUpMnuId()).childList().add(node);
        }
        return roots;
    }

    private boolean matchesScope(Menu menu, String mnuScopeCd) {
        if (mnuScopeCd == null || mnuScopeCd.isBlank()) {
            return true;
        }
        String normalizedScope = normalizeScope(mnuScopeCd);
        return normalizedScope.equals(menu.getMnuScopeCd()) || "COMMON".equals(menu.getMnuScopeCd());
    }

    private String normalizeScope(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeCodeOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeGateCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private boolean isBillingGateSatisfied(Menu menu, List<Role> roles, Long tenantId) {
        if (menu == null || menu.getBillGateCd() == null || menu.getBillGateCd().isBlank()) {
            return true;
        }

        if ("PAID_CUSTOMER_ADMIN".equals(menu.getBillGateCd())) {
            return hasCustomerAdminRole(roles) && billingAccessPolicyService.hasPaidAdminAccess(tenantId);
        }

        return true;
    }

    private boolean hasCustomerAdminRole(List<Role> roles) {
        return roles.stream().anyMatch(role ->
                "CUSTOMER".equalsIgnoreCase(role.getRoleD1Cd())
                        && role.getRoleD2Cd() != null
                        && !"USER".equalsIgnoreCase(role.getRoleD2Cd())
        );
    }

    private MenuResponse toMenuResponse(Menu menu) {
        return MenuResponse.from(menu, resolveMenuName(menu));
    }

    private MenuNodeResponse toMenuNodeResponse(Menu menu) {
        return MenuNodeResponse.from(menu, resolveMenuName(menu));
    }

    private String resolveMenuName(Menu menu) {
        return commonCodeService.resolveDisplayName("MNU_NM", menu.getMnuNmCd(), menu.getMnuNm());
    }
}

package com.sinwoo.menu.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.auth.repository.UserRoleRepository;
import com.sinwoo.menu.domain.Menu;
import com.sinwoo.menu.domain.RoleMenuAuth;
import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuNodeResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.repository.MenuRepository;
import com.sinwoo.menu.repository.RoleMenuAuthRepository;
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
                request.mnuNm().trim(),
                normalizeScope(request.mnuScopeCd()),
                request.upMnuId(),
                blankToNull(request.pathUri()),
                blankToNull(request.iconNm()),
                request.dspOrd() == null ? 0 : request.dspOrd(),
                normalizeYn(request.useYn(), "Y")
        );

        return MenuResponse.from(menuRepository.save(menu));
    }

    @Override
    public MenuListResponse getMenus(String mnuScopeCd) {
        List<MenuResponse> items = (mnuScopeCd == null || mnuScopeCd.isBlank()
                ? menuRepository.findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc()
                : menuRepository.findAllByMnuScopeCdOrderByDspOrdAscIdAsc(normalizeScope(mnuScopeCd)))
                .stream()
                .map(MenuResponse::from)
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
        return buildVisibleMenus(roles, mnuScopeCd);
    }

    @Override
    public MenuTreeResponse getVisibleMenusByUsr(Long usrId, String mnuScopeCd) {
        if (usrId == null) {
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
        return buildVisibleMenus(roles, mnuScopeCd);
    }

    private MenuTreeResponse buildVisibleMenus(List<Role> roles, String mnuScopeCd) {
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
        menus.forEach(menu -> nodeById.put(menu.getId(), MenuNodeResponse.from(menu)));

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

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

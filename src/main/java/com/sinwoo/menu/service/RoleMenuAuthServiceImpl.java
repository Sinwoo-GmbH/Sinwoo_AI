package com.sinwoo.menu.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.menu.domain.Menu;
import com.sinwoo.menu.domain.RoleMenuAuth;
import com.sinwoo.menu.dto.RoleMenuAuthListResponse;
import com.sinwoo.menu.dto.RoleMenuAuthResponse;
import com.sinwoo.menu.dto.RoleMenuGrantRequest;
import com.sinwoo.menu.dto.UpsertRoleMenuAuthRequest;
import com.sinwoo.menu.repository.MenuRepository;
import com.sinwoo.menu.repository.RoleMenuAuthRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleMenuAuthServiceImpl implements RoleMenuAuthService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final RoleMenuAuthRepository roleMenuAuthRepository;

    @Override
    @Transactional
    public RoleMenuAuthListResponse upsertRoleMenuAuths(UpsertRoleMenuAuthRequest request) {
        Role role = roleRepository.findByRoleCd(request.roleCd().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

        roleMenuAuthRepository.deleteAllByRoleId(role.getId());

        for (RoleMenuGrantRequest item : request.itemList()) {
            Menu menu = menuRepository.findByMnuCd(item.mnuCd().trim().toUpperCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu not found: " + item.mnuCd()));

            roleMenuAuthRepository.save(RoleMenuAuth.create(
                    role.getId(),
                    menu.getId(),
                    normalizeYn(item.viewYn(), "Y"),
                    normalizeYn(item.crtYn(), "N"),
                    normalizeYn(item.updYn(), "N"),
                    normalizeYn(item.delYn(), "N"),
                    normalizeYn(item.aprvYn(), "N"),
                    normalizeYn(item.exprtYn(), "N")
            ));
        }

        return getRoleMenuAuths(role.getRoleCd());
    }

    @Override
    public RoleMenuAuthListResponse getRoleMenuAuths(String roleCd) {
        Role role = roleRepository.findByRoleCd(roleCd.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

        Map<Long, Menu> menuById = menuRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Menu::getId, Function.identity()));

        List<RoleMenuAuthResponse> items = roleMenuAuthRepository.findAllByRoleIdOrderByMnuIdAsc(role.getId()).stream()
                .map(auth -> {
                    Menu menu = menuById.get(auth.getMnuId());
                    return new RoleMenuAuthResponse(
                            role.getRoleCd(),
                            role.getRoleNm(),
                            menu == null ? null : menu.getMnuCd(),
                            menu == null ? null : menu.getMnuNm(),
                            auth.getViewYn(),
                            auth.getCrtYn(),
                            auth.getUpdYn(),
                            auth.getDelYn(),
                            auth.getAprvYn(),
                            auth.getExprtYn()
                    );
                })
                .toList();

        return new RoleMenuAuthListResponse(items.size(), items);
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }
}

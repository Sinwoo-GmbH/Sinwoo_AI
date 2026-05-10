package com.sinwoo.platform.mnu.service;

import com.sinwoo.platform.auth.domain.Role;
import com.sinwoo.platform.auth.repository.RoleRepository;
import com.sinwoo.platform.mnu.domain.Mnu;
import com.sinwoo.platform.mnu.domain.RoleMnuAuth;
import com.sinwoo.platform.mnu.dto.RoleMnuAuthListResponse;
import com.sinwoo.platform.mnu.dto.RoleMnuAuthResponse;
import com.sinwoo.platform.mnu.dto.RoleMnuGrantRequest;
import com.sinwoo.platform.mnu.dto.UpsertRoleMnuAuthRequest;
import com.sinwoo.platform.mnu.repository.MnuRepository;
import com.sinwoo.platform.mnu.repository.RoleMnuAuthRepository;
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
public class RoleMnuAuthServiceImpl implements RoleMnuAuthService {

    private final RoleRepository roleRepository;
    private final MnuRepository mnuRepository;
    private final RoleMnuAuthRepository roleMnuAuthRepository;

    @Override
    @Transactional
    public RoleMnuAuthListResponse upsertRoleMnuAuths(UpsertRoleMnuAuthRequest request) {
        Role role = roleRepository.findByRoleCd(request.roleCd().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

        roleMnuAuthRepository.deleteAllByRoleId(role.getId());

        for (RoleMnuGrantRequest item : request.itemList()) {
            Mnu mnu = mnuRepository.findByMnuCd(item.mnuCd().trim().toUpperCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mnu not found: " + item.mnuCd()));

            roleMnuAuthRepository.save(RoleMnuAuth.create(
                    role.getId(),
                    mnu.getId(),
                    normalizeYn(item.viewYn(), "Y"),
                    normalizeYn(item.crtYn(), "N"),
                    normalizeYn(item.updYn(), "N"),
                    normalizeYn(item.delYn(), "N"),
                    normalizeYn(item.aprvYn(), "N"),
                    normalizeYn(item.exprtYn(), "N")
            ));
        }

        return getRoleMnuAuths(role.getRoleCd());
    }

    @Override
    public RoleMnuAuthListResponse getRoleMnuAuths(String roleCd) {
        Role role = roleRepository.findByRoleCd(roleCd.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

        Map<Long, Mnu> mnuById = mnuRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Mnu::getId, Function.identity()));

        List<RoleMnuAuthResponse> items = roleMnuAuthRepository.findAllByRoleIdOrderByMnuIdAsc(role.getId()).stream()
                .map(auth -> {
                    Mnu mnu = mnuById.get(auth.getMnuId());
                    return new RoleMnuAuthResponse(
                            role.getRoleCd(),
                            role.getRoleNm(),
                            mnu == null ? null : mnu.getMnuCd(),
                            mnu == null ? null : mnu.getMnuNm(),
                            auth.getViewYn(),
                            auth.getCrtYn(),
                            auth.getUpdYn(),
                            auth.getDelYn(),
                            auth.getAprvYn(),
                            auth.getExprtYn()
                    );
                })
                .toList();

        return new RoleMnuAuthListResponse(items.size(), items);
    }

    private String normalizeYn(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "Y".equalsIgnoreCase(value.trim()) ? "Y" : "N";
    }
}

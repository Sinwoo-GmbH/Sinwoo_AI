package com.sinwoo.auth.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.dto.CreateRoleRequest;
import com.sinwoo.auth.dto.RoleListResponse;
import com.sinwoo.auth.dto.RoleResponse;
import com.sinwoo.auth.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedRoleCd = request.roleCd().trim().toUpperCase();

        if (roleRepository.existsByRoleCdIgnoreCase(normalizedRoleCd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role code already exists");
        }

        Role role = Role.create(
                normalizedRoleCd,
                request.roleNm().trim(),
                blankToNullUpper(request.roleScopeCd()),
                blankToNullUpper(request.roleD1Cd()),
                blankToNullUpper(request.roleD2Cd()),
                blankToNullUpper(request.roleD3Cd()),
                blankToNullUpper(request.roleGrpCd()),
                blankToNullUpper(request.roleLvlCd())
        );

        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    public RoleListResponse getRoles() {
        List<RoleResponse> items = roleRepository.findAllByOrderByRoleCdAsc().stream()
                .map(RoleResponse::from)
                .toList();

        return new RoleListResponse(items.size(), items);
    }

    @Override
    public List<RoleResponse> getRolesByCodes(List<String> roleCds) {
        if (roleCds == null || roleCds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findByRoleCdIn(
                        roleCds.stream().map(value -> value.trim().toUpperCase()).distinct().toList()
                ).stream()
                .map(RoleResponse::from)
                .toList();
    }

    private String blankToNullUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}

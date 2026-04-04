package com.sinwoo.user.service;

import com.sinwoo.auth.domain.Role;
import com.sinwoo.auth.domain.UserRole;
import com.sinwoo.auth.repository.RoleRepository;
import com.sinwoo.auth.repository.UserRoleRepository;
import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.tenant.repository.TenantRepository;
import com.sinwoo.user.domain.User;
import com.sinwoo.user.dto.CreateUserRequest;
import com.sinwoo.user.dto.UserListResponse;
import com.sinwoo.user.dto.UserResponse;
import com.sinwoo.user.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateTenant(request.tenantId());
        validateCompany(request.tenantId(), request.coId());

        String normalizedLgnId = request.lgnId().trim().toUpperCase();
        String normalizedEml = request.eml().trim().toLowerCase();

        if (userRepository.existsByTenantIdAndLgnIdIgnoreCase(request.tenantId(), normalizedLgnId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login ID already exists in tenant");
        }

        if (userRepository.existsByTenantIdAndEmlIgnoreCase(request.tenantId(), normalizedEml)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists in tenant");
        }

        List<Role> roles = resolveRoles(request.roleCds());

        User user = User.create(
                request.tenantId(),
                request.coId(),
                normalizedLgnId,
                normalizedEml,
                passwordEncoder.encode(request.pwd()),
                request.dspNm().trim(),
                request.loclCd().trim().toLowerCase(),
                blankToNull(request.telNo()),
                blankToNullUpper(request.authGrpCd()),
                blankToNullUpper(request.authLvlCd()),
                normalizeStatus(request.stsCd())
        );

        User savedUser = userRepository.save(user);

        for (Role role : roles) {
            userRoleRepository.save(UserRole.create(savedUser.getId(), role.getId()));
        }

        return UserResponse.from(savedUser, roles.stream().map(Role::getRoleCd).toList());
    }

    @Override
    public UserListResponse getUsers(Long tenantId, Long coId) {
        validateTenant(tenantId);
        validateCompany(tenantId, coId);

        List<User> users = coId == null
                ? userRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId)
                : userRepository.findAllByTenantIdAndCoIdOrderByCreatedAtDescIdDesc(tenantId, coId);

        Map<Long, String> roleCdById = new LinkedHashMap<>();
        roleRepository.findAll().forEach(role -> roleCdById.put(role.getId(), role.getRoleCd()));

        List<UserResponse> items = users.stream()
                .map(user -> UserResponse.from(
                        user,
                        userRoleRepository.findAllByUsrId(user.getId()).stream()
                                .map(userRole -> roleCdById.get(userRole.getRoleId()))
                                .filter(roleCd -> roleCd != null && !roleCd.isBlank())
                                .distinct()
                                .toList()
                ))
                .toList();

        return new UserListResponse(items.size(), items);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCompany(Long tenantId, Long coId) {
        if (coId == null) {
            return;
        }
        if (companyRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found in tenant");
        }
    }

    private List<Role> resolveRoles(List<String> roleCds) {
        if (roleCds == null || roleCds.isEmpty()) {
            Role defaultRole = roleRepository.findByRoleCd("ROLE_USER")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default role not found"));
            return List.of(defaultRole);
        }

        List<String> normalizedCodes = roleCds.stream()
                .map(value -> value.trim().toUpperCase())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        List<Role> roles = roleRepository.findByRoleCdIn(normalizedCodes);
        if (roles.size() != normalizedCodes.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more roles not found");
        }
        return roles;
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "ACTIVE";
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNullUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}

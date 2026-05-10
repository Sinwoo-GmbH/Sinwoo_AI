package com.sinwoo.platform.user.service;

import com.sinwoo.platform.auth.domain.Role;
import com.sinwoo.platform.auth.domain.UsrRole;
import com.sinwoo.platform.auth.repository.RoleRepository;
import com.sinwoo.platform.auth.repository.UsrRoleRepository;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.tenant.repository.TenantRepository;
import com.sinwoo.platform.user.domain.Usr;
import com.sinwoo.platform.user.dto.CreateUsrRequest;
import com.sinwoo.platform.user.dto.UsrListResponse;
import com.sinwoo.platform.user.dto.UsrResponse;
import com.sinwoo.platform.user.repository.UsrRepository;
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
public class UsrServiceImpl implements UsrService {

    private final UsrRepository usrRepository;
    private final TenantRepository tenantRepository;
    private final CoRepository coRepository;
    private final RoleRepository roleRepository;
    private final UsrRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsrResponse createUsr(CreateUsrRequest request) {
        validateTenant(request.tenantId());
        validateCo(request.tenantId(), request.coId());

        String normalizedLgnId = request.lgnId().trim().toUpperCase();
        String normalizedEml = request.eml().trim().toLowerCase();

        if (usrRepository.existsByTenantIdAndLgnIdIgnoreCase(request.tenantId(), normalizedLgnId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login ID already exists in tenant");
        }

        if (usrRepository.existsByTenantIdAndEmlIgnoreCase(request.tenantId(), normalizedEml)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists in tenant");
        }

        List<Role> roles = resolveRoles(request.roleCds());

        Usr user = Usr.create(
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

        Usr savedUsr = usrRepository.save(user);

        for (Role role : roles) {
            userRoleRepository.save(UsrRole.create(savedUsr.getId(), role.getId()));
        }

        return UsrResponse.from(savedUsr, roles.stream().map(Role::getRoleCd).toList());
    }

    @Override
    public UsrListResponse getUsrs(Long tenantId, Long coId) {
        validateTenant(tenantId);
        validateCo(tenantId, coId);

        List<Usr> usrs = coId == null
                ? usrRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId)
                : usrRepository.findAllByTenantIdAndCoIdOrderByCreatedAtDescIdDesc(tenantId, coId);

        Map<Long, String> roleCdById = new LinkedHashMap<>();
        roleRepository.findAll().forEach(role -> roleCdById.put(role.getId(), role.getRoleCd()));

        List<UsrResponse> items = usrs.stream()
                .map(user -> UsrResponse.from(
                        user,
                        userRoleRepository.findAllByUsrId(user.getId()).stream()
                                .map(userRole -> roleCdById.get(userRole.getRoleId()))
                                .filter(roleCd -> roleCd != null && !roleCd.isBlank())
                                .distinct()
                                .toList()
                ))
                .toList();

        return new UsrListResponse(items.size(), items);
    }

    private void validateTenant(Long tenantId) {
        if (tenantId == null || !tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }
    }

    private void validateCo(Long tenantId, Long coId) {
        if (coId == null) {
            return;
        }
        if (coRepository.findByIdAndTenantId(coId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Co not found in tenant");
        }
    }

    private List<Role> resolveRoles(List<String> roleCds) {
        if (roleCds == null || roleCds.isEmpty()) {
            Role defaultRole = roleRepository.findByRoleCd("ROLE_CUSTOMER_USER_MEMBER")
                    .or(() -> roleRepository.findByRoleCd("ROLE_USER"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default role not found"));
            return List.of(defaultRole);
        }

        List<String> normalizedCds = roleCds.stream()
                .map(value -> value.trim().toUpperCase())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        List<Role> roles = roleRepository.findByRoleCdIn(normalizedCds);
        if (roles.size() != normalizedCds.size()) {
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

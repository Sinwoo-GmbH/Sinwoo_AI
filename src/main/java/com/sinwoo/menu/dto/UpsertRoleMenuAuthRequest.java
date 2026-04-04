package com.sinwoo.menu.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpsertRoleMenuAuthRequest(
        @NotBlank
        @Size(max = 100)
        String roleCd,

        @Valid
        @NotEmpty
        List<RoleMenuGrantRequest> itemList
) {
}

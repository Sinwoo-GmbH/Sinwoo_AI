package com.sinwoo.menu.controller;

import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.service.MenuService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return menuService.createMenu(request);
    }

    @GetMapping
    public MenuListResponse getMenus(@RequestParam(required = false) String mnuScopeCd) {
        return menuService.getMenus(mnuScopeCd);
    }

    @GetMapping("/visible")
    public MenuTreeResponse getVisibleMenus(
            @RequestParam List<String> roleCd,
            @RequestParam(required = false) String mnuScopeCd
    ) {
        return menuService.getVisibleMenus(roleCd, mnuScopeCd);
    }

    @GetMapping("/visible-by-user")
    public MenuTreeResponse getVisibleMenusByUsr(
            @RequestParam Long usrId,
            @RequestParam(required = false) String mnuScopeCd
    ) {
        return menuService.getVisibleMenusByUsr(usrId, mnuScopeCd);
    }
}

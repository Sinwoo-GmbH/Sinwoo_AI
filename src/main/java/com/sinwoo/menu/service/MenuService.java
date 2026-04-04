package com.sinwoo.menu.service;

import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import java.util.List;

public interface MenuService {

    MenuResponse createMenu(CreateMenuRequest request);

    MenuListResponse getMenus(String mnuScopeCd);

    MenuTreeResponse getVisibleMenus(List<String> roleCds, String mnuScopeCd);
}

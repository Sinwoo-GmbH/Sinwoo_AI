package com.sinwoo.menu.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.accesslog.AccessLogService;
import com.sinwoo.common.security.SecurityConfig;
import com.sinwoo.menu.dto.CreateMenuRequest;
import com.sinwoo.menu.dto.MenuListResponse;
import com.sinwoo.menu.dto.MenuNodeResponse;
import com.sinwoo.menu.dto.MenuResponse;
import com.sinwoo.menu.dto.MenuTreeResponse;
import com.sinwoo.menu.service.MenuService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MenuController.class)
@Import(SecurityConfig.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @MockBean
    private AccessLogService accessLogService;

    @Test
    void createMenuReturnsCreatedMenu() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        CreateMenuRequest request = new CreateMenuRequest("MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, "Y");
        MenuResponse response = new MenuResponse(1L, "MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, "Y", now, now);

        given(menuService.createMenu(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/menus")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mnuCd").value("MNU_ADMIN_DASH"))
                .andExpect(jsonPath("$.mnuScopeCd").value("ADMIN"))
                .andExpect(jsonPath("$.useYn").value("Y"));
    }

    @Test
    void getVisibleMenusReturnsMenuTree() throws Exception {
        MenuTreeResponse response = new MenuTreeResponse(
                1,
                List.of(new MenuNodeResponse(1L, "MNU_CUSTOMER_DASH", "Customer Dashboard", "CUSTOMER", null, "/customer/dashboard", "layout-dashboard", 10, List.of()))
        );

        given(menuService.getVisibleMenus(List.of("ROLE_CUSTOMER_USER_MEMBER"), "CUSTOMER")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus/visible")
                        .param("roleCd", "ROLE_CUSTOMER_USER_MEMBER")
                        .param("mnuScopeCd", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_CUSTOMER_DASH"))
                .andExpect(jsonPath("$.itemList[0].mnuScopeCd").value("CUSTOMER"));
    }

    @Test
    void getMenusReturnsFlatList() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-04T00:00:00Z");
        MenuListResponse response = new MenuListResponse(
                1,
                List.of(new MenuResponse(1L, "MNU_ADMIN_DASH", "Admin Dashboard", "ADMIN", null, "/admin/dashboard", "layout-dashboard", 10, "Y", now, now))
        );

        given(menuService.getMenus("ADMIN")).willReturn(response);

        mockMvc.perform(get("/api/v1/menus").param("mnuScopeCd", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totCnt").value(1))
                .andExpect(jsonPath("$.itemList[0].mnuCd").value("MNU_ADMIN_DASH"));
    }
}

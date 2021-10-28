package com.aiden.dev.simplelibrary.modules.main;

import com.aiden.dev.simplelibrary.modules.account.Account;
import com.aiden.dev.simplelibrary.modules.account.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired MockMvc mockMvc;

    @DisplayName("index 페이지 보이는지 테스트 - 비회원")
    @Test
    void home_non_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("index 페이지 보이는지 테스트 - 회원")
    @Test
    void home_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("index"));
    }

    @DisplayName("로그인 페이지 보이는지 테스트")
    @Test
    void login() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}
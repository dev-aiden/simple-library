package com.aiden.dev.simplelibrary.modules.main;

import com.aiden.dev.simplelibrary.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @DisplayName("index 페이지 보이는지 테스트 - 회원")
    @Test
    void home_member() throws Exception {
        Account account = Account.builder()
                .emailVerified(false)
                .build();

        mockMvc.perform(get("/")
                        .flashAttr("account", account))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("index"));
    }
}
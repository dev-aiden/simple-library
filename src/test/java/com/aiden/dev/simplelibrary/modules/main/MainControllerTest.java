package com.aiden.dev.simplelibrary.modules.main;

import com.aiden.dev.simplelibrary.modules.account.AccountService;
import com.aiden.dev.simplelibrary.modules.account.WithAccount;
import com.aiden.dev.simplelibrary.modules.book.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean BookService bookService;
    @MockBean DataSource dataSource;

    @DisplayName("index 페이지 보이는지 테스트 - 비회원")
    @Test
    void home_non_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("categoryName"))
                .andExpect(model().attributeExists("bookList"))
                .andExpect(view().name("index"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("index 페이지 보이는지 테스트 - 회원")
    @Test
    void home_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("categoryName"))
                .andExpect(model().attributeExists("bookList"))
                .andExpect(view().name("index"));
    }

    @DisplayName("로그인 페이지 보이는지 테스트")
    @Test
    void login() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}
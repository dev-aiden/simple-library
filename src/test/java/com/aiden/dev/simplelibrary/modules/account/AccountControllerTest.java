package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.modules.account.validator.SignUpFormValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SignUpFormValidator signUpFormValidator;
    @MockBean AccountService accountService;
    @MockBean AccountRepository accountRepository;
    
    @DisplayName("회원가입 페이지 보이는지 확인")
    @Test
    void signUpForm() throws Exception {
        given(signUpFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원가입 테스트 - 잘못된 입력값")
    @Test
    void signUp_wrong_value() throws Exception {
        given(signUpFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/sign-up")
                        .param("loginId", "test")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test1234")
                        .param("nickname", "")
                        .param("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    }

    @DisplayName("회원가입 테스트")
    @Test
    void signUp() throws Exception {
        given(signUpFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/sign-up")
                        .param("loginId", "test")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test1234")
                        .param("nickname", "test")
                        .param("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        then(accountService).should().createAccount(any());
        then(accountService).should().login(any());
    }

    @DisplayName("이메일 인증 테스트 - 잘못된 이메일")
    @Test
    void checkEmailToken_wrong_email() throws Exception {
        given(accountService.findAccountByEmail(any())).willReturn(null);

        mockMvc.perform(get("/check-email-token")
                        .param("token", "testToken")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.email"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("이메일 인증 테스트 - 잘못된 토큰")
    @Test
    void checkEmailToken_wrong_token() throws Exception {
        given(accountService.findAccountByEmail(any())).willReturn(mock(Account.class));
        given(accountService.findAccountByEmail(any()).isValidToken(any())).willReturn(false);

        mockMvc.perform(get("/check-email-token")
                        .param("token", "testToken")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.token"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("이메일 인증 테스트")
    @Test
    void checkEmailToken() throws Exception {
        given(accountService.findAccountByEmail(any())).willReturn(mock(Account.class));
        given(accountService.findAccountByEmail(any()).isValidToken(any())).willReturn(true);

        mockMvc.perform(get("/check-email-token")
                        .param("token", "testToken")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"));

        then(accountService).should().login(any());
    }
}
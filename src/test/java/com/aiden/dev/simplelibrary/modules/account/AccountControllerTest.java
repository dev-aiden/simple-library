package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.modules.account.validator.SignUpFormValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase
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

        then(accountService).should().completeSignUp(any());
    }

    @DisplayName("인증 메일 페이지 테스트 - 비회원")
    @Test
    void checkEmail_non_member() throws Exception {
        mockMvc.perform(get("/check-email"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("인증 메일 페이지 테스트 - 회원")
    @Test
    void checkEmail_member() throws Exception {
        mockMvc.perform(get("/check-email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("email"))
                .andExpect(view().name("account/check-email"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("인증 메일 재발송 확인 - 1시간 이내 재발송")
    @Test
    void resendConfirmEmail_before_1_hour() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"))
                .andExpect(view().name("account/check-email"));
    }

    @WithAccount(loginId = "aiden", minusHoursForEmailCheckToken = 2L)
    @DisplayName("인증 메일 재발송 확인 - 1시간 이후 재발송")
    @Test
    void resendConfirmEmail_after_1_hour() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeDoesNotExist("email"))
                .andExpect(view().name("redirect:/"));

        then(accountService).should().sendSignUpConfirmEmail(any());
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 다른 사용자 아이디")
    @Test
    void viewProfileAccountOwner_other_account() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/id/aiden2")))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 본인 아이디")
    @Test
    void viewProfileAccountOwner() throws Exception {
        mockMvc.perform(get("/profile/id/aiden"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/aiden"));
    }

    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하지 않는 사용자")
    @Test
    void viewProfile_not_exist_user() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/aiden")))
                .hasCause(new IllegalArgumentException("aiden에 해당하는 사용자가 존재하지 않습니다."));
    }

    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하는 사용자")
    @Test
    void viewProfile_exist_user() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .emailVerified(false)
                .build();

        when(accountService.findAccountByNickname(any())).thenReturn(account);

        mockMvc.perform(get("/profile/aiden"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(view().name("account/profile"));
    }
}
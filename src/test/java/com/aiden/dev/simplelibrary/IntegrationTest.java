package com.aiden.dev.simplelibrary;

import com.aiden.dev.simplelibrary.modules.account.Account;
import com.aiden.dev.simplelibrary.modules.account.AccountRepository;
import com.aiden.dev.simplelibrary.modules.account.AccountService;
import com.aiden.dev.simplelibrary.modules.account.WithAccount;
import com.aiden.dev.simplelibrary.modules.account.form.SignUpForm;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("통합 테스트")
public class IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("aiden");
        signUpForm.setNickname("aiden");
        signUpForm.setEmail("aiden@email.com");
        signUpForm.setPassword("12345678");
        accountService.createAccount(signUpForm);
    }

    @DisplayName("index 페이지 테스트 - 비회원")
    @Test
    void home_non_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("index 페이지 테스트 - 회원")
    @Test
    void home_member() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("index"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("회원 가입 폼 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 테스트 - 잘못된 입력값")
    @Test
    void signUp_wrong_value() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("loginId", "test")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test5678")
                        .param("nickname", "test")
                        .param("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.findByLoginId("test")).isEmpty();
    }

    @DisplayName("회원 가입 테스트")
    @Test
    void signUp() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("loginId", "test")
                        .param("password", "test1234")
                        .param("passwordConfirm", "test1234")
                        .param("nickname", "test")
                        .param("email", "test@email.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test"));

        Account account = accountRepository.findByLoginId("test").orElseThrow();
        assertThat(account.getPassword()).isNotEqualTo("test1234");
        assertThat(account.getNickname()).isEqualTo("test");
        assertThat(account.getEmail()).isEqualTo("test@email.com");
        assertThat(account.getEmailCheckToken()).isNotNull();
    }

    @DisplayName("이메일 인증 테스트 - 잘못된 이메일")
    @Test
    void checkEmailToken_wrong_email() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "aidenToken")
                        .param("email", "aiden2@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.email"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("이메일 인증 테스트 - 잘못된 토큰")
    @Test
    void checkEmailToken_wrong_token() throws Exception {

        mockMvc.perform(get("/check-email-token")
                        .param("token", "aidenToken2")
                        .param("email", "aiden@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.token"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("이메일 인증 테스트")
    @Test
    void checkEmailToken() throws Exception {
        Account account = accountRepository.findByLoginId("aiden").orElseThrow();

        mockMvc.perform(get("/check-email-token")
                        .param("token", account.getEmailCheckToken())
                        .param("email", "aiden@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(accountRepository.findByEmail("aiden@email.com").get().getEmailVerified()).isTrue();
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("인증 메일 재발송 확인 - 1시간 이내 재발송")
    @Test
    void resendConfirmEmail_before_1_hour() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("인증 메일 재발송 확인 - 1시간 이후 재발송")
    @Test
    void resendConfirmEmail_after_1_hour() throws Exception {
        Account account = accountRepository.findByLoginId("aiden").orElseThrow();
        account.setEmailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(2L));

        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeDoesNotExist("email"))
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("로그인 테스트 - 로그인 실패")
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "aiden2")
                        .param("password", "11111111")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("로그인 테스트 - 로그인 성공")
    @Test
    void login_success() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "aiden")
                        .param("password", "12345678")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}

package com.aiden.dev.simplelibrary;

import com.aiden.dev.simplelibrary.modules.account.Account;
import com.aiden.dev.simplelibrary.modules.account.AccountRepository;
import com.aiden.dev.simplelibrary.modules.account.AccountService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 다른 사용자 아이디")
    @Test
    void viewProfileAccountOwner_other_account() {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/id/aiden2")))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 본인 아이디")
    @Test
    void viewProfileAccountOwner() throws Exception {
        mockMvc.perform(get("/profile/id/aiden"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/aiden"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하지 않는 사용자")
    @Test
    void viewProfile_not_exist_user() {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/aiden2")))
                .hasCause(new IllegalArgumentException("aiden2에 해당하는 사용자가 존재하지 않습니다."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하는 사용자")
    @Test
    void viewProfile_exist_user() throws Exception {
        mockMvc.perform(get("/profile/aiden"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("isOwner"))
                .andExpect(view().name("account/profile"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("프로필 변경 폼 테스트 - 비로그인 사용자")
    @Test
    void updateProfileForm_not_login_user() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 변경 폼 테스트 - 로그인 사용자")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(view().name("settings/profile"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 변경 테스트 - 입력값 에러")
    @Test
    void updateProfile_wrong_value() throws Exception {
        mockMvc.perform(post("/settings/profile")
                        .param("nickname", "")
                        .param("profileImage", "aidenProfileImage")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("settings/profile"))
                .andExpect(authenticated().withUsername("aiden"));

        Optional<Account> accountOptional = accountRepository.findByLoginId("aiden");
        assertThat(accountOptional).isNotNull();
        assertThat(accountOptional.get().getNickname()).isEqualTo("aiden");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 변경 테스트 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        mockMvc.perform(post("/settings/profile")
                        .param("nickname", "aiden2")
                        .param("profileImage", "aidenProfileImage")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(authenticated().withUsername("aiden"));

        Optional<Account> accountOptional = accountRepository.findByLoginId("aiden");
        assertThat(accountOptional).isNotNull();
        assertThat(accountOptional.get().getNickname()).isEqualTo("aiden2");
    }

    @DisplayName("비밀번호 변경 폼 테스트 - 비로그인 사용자")
    @Test
    void updatePasswordForm_not_login_user() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 변경 폼 테스트 - 로그인 사용자")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 변경 테스트 - 입력값 에러")
    @Test
    void updatePassword_wrong_value() throws Exception {
        Account account = accountRepository.findByLoginId("aiden").orElse(null);
        String originPassword = account.getPassword();

        mockMvc.perform(post("/settings/password")
                        .param("newPassword", "test1234")
                        .param("newPasswordConfirm", "test5678")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("settings/password"))
                .andExpect(authenticated().withUsername("aiden"));

        Optional<Account> accountOptional = accountRepository.findByLoginId("aiden");
        assertThat(accountOptional).isNotNull();
        assertThat(accountOptional.get().getPassword()).isEqualTo(originPassword);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("비밀번호 변경 테스트 - 입력값 정상")
    @Test
    void updatePassword() throws Exception {
        Account account = accountRepository.findByLoginId("aiden").orElse(null);
        String originPassword = account.getPassword();

        mockMvc.perform(post("/settings/password")
                        .param("newPassword", "test5678")
                        .param("newPasswordConfirm", "test5678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(authenticated().withUsername("aiden"));

        Optional<Account> accountOptional = accountRepository.findByLoginId("aiden");
        assertThat(accountOptional).isNotNull();
        assertThat(accountOptional.get().getNickname()).isNotEqualTo(originPassword);
    }

    @DisplayName("알림 변경 폼 테스트 - 비로그인 사용자")
    @Test
    void updateNotificationForm_not_login_user() throws Exception {
        mockMvc.perform(get("/settings/notification"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 변경 폼 테스트 - 로그인 사용자")
    @Test
    void updateNotificationForm() throws Exception {
        mockMvc.perform(get("/settings/notification"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"))
                .andExpect(view().name("settings/notification"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 변경 테스트")
    @Test
    void updateNotification() throws Exception {
        mockMvc.perform(post("/settings/notification")
                        .param("bookRentalNotificationByEmail", "true")
                        .param("bookRentalNotificationByWeb", "false")
                        .param("bookReturnNotificationByEmail", "true")
                        .param("bookReturnNotificationByWeb", "false")
                        .param("bookRentalAvailabilityNotificationByEmail", "true")
                        .param("bookRentalAvailabilityNotificationByWeb", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/notification"))
                .andExpect(authenticated().withUsername("aiden"));

        Account account = accountRepository.findByLoginId("aiden").orElse(null);
        assertThat(account).isNotNull();
        assertThat(account.getBookRentalNotificationByEmail()).isTrue();
        assertThat(account.getBookRentalNotificationByWeb()).isFalse();
        assertThat(account.getBookReturnNotificationByEmail()).isTrue();
        assertThat(account.getBookReturnNotificationByWeb()).isFalse();
        assertThat(account.getBookRentalAvailabilityNotificationByEmail()).isTrue();
        assertThat(account.getBookRentalAvailabilityNotificationByWeb()).isTrue();
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정 삭제 테스트")
    @Test
    void deleteAccount() throws Exception {
        mockMvc.perform(delete("/settings/account")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.findByNickname("aiden")).isEmpty();
    }
}

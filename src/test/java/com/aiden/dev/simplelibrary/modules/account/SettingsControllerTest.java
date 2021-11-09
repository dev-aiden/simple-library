package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.modules.account.form.NotificationForm;
import com.aiden.dev.simplelibrary.modules.account.validator.PasswordFormValidator;
import com.aiden.dev.simplelibrary.modules.account.validator.ProfileFormValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean ProfileFormValidator profileFormValidator;
    @MockBean PasswordFormValidator passwordFormValidator;
    @MockBean DataSource dataSource;
    @MockBean ModelMapper modelMapper;

    @DisplayName("프로필 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateProfileForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateProfileForm_after_login() throws Exception {
        when(modelMapper.map(any(), any())).thenReturn(new ProfileForm());
        when(profileFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(view().name("settings/profile"));
    }

    @DisplayName("프로필 변경 테스트 - 로그인 이전")
    @Test
    void updateProfile_before_login() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 변경 테스트 - 로그인 이후")
    @Test
    void updateProfile_after_login() throws Exception {
        given(profileFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/settings/profile")
                        .param("nickname", "aiden2")
                        .param("profileImage", "aiden2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/profile"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 변경 테스트 - 입력값 에러")
    @Test
    void updateProfile_wrong_value() throws Exception {
        given(profileFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/settings/profile")
                        .param("nickname", "")
                        .param("profileImage", "aiden2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("settings/profile"));
    }

    @DisplayName("비밀번호 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updatePasswordForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updatePasswordForm_after_login() throws Exception {
        when(passwordFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"));
    }

    @DisplayName("프로필 변경 테스트 - 로그인 이전")
    @Test
    void updatePassword_before_login() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 테스트 - 로그인 이후")
    @Test
    void updatePassword_after_login() throws Exception {
        given(passwordFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/settings/password")
                        .param("newPassword", "test1234")
                        .param("newPasswordConfirm", "test1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/password"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 테스트 - 입력값 에러")
    @Test
    void updatePassword_wrong_value() throws Exception {
        given(passwordFormValidator.supports(any())).willReturn(true);

        mockMvc.perform(post("/settings/password")
                        .param("nickname", "test1234")
                        .param("profileImage", "test5678")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(view().name("settings/password"));
    }

    @DisplayName("알림 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateNotificationForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/notification"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("알림 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateNotificationForm_after_login() throws Exception {
        when(modelMapper.map(any(), any())).thenReturn(new NotificationForm());
        
        mockMvc.perform(get("/settings/notification"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"))
                .andExpect(view().name("settings/notification"));
    }

    @DisplayName("알림 변경 테스트 - 로그인 이전")
    @Test
    void updateNotification_before_login() throws Exception {
        mockMvc.perform(post("/settings/notification")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("알림 변경 테스트 - 로그인 이후")
    @Test
    void updateNotification_after_login() throws Exception {
        mockMvc.perform(post("/settings/notification")
                        .param("bookRentalNotificationByEmail", "true")
                        .param("bookRentalNotificationByWeb", "true")
                        .param("bookReturnNotificationByEmail", "true")
                        .param("bookReturnNotificationByWeb", "true")
                        .param("bookRentalAvailabilityNotificationByEmail", "true")
                        .param("bookRentalAvailabilityNotificationByWeb", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(redirectedUrl("/settings/notification"));
    }
}
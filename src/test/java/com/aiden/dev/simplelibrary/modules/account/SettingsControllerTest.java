package com.aiden.dev.simplelibrary.modules.account;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean ProfileFormValidator profileFormValidator;
    @MockBean DataSource dataSource;
    @MockBean ModelMapper modelMapper;

    @DisplayName("프로필 수정 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateProfileForm_not_current_account() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 수정 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateProfileForm_current_account() throws Exception {
        when(modelMapper.map(any(), any())).thenReturn(new ProfileForm());
        when(profileFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(view().name("settings/profile"));
    }
}
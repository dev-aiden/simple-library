package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.infra.mail.EmailService;
import com.aiden.dev.simplelibrary.modules.account.form.SignUpForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks AccountService accountService;
    @Mock EmailService emailService;
    @Mock AccountRepository accountRepository;
    @Mock ModelMapper modelMapper;
    @Spy PasswordEncoder passwordEncoder;


    @DisplayName("계정 생성 테스트")
    @Test
    void createAccount() {
        // given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("test1234");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");

        Account account = Account.builder()
                .loginId("test")
                .nickname("test")
                .email("test@email.com")
                .build();

        given(passwordEncoder.encode(any())).willReturn("encryptPassword");
        given(accountRepository.save(any())).willReturn(account);
        given(modelMapper.map(any(), any())).willReturn(account);

        // when
        Account savedAccount = accountService.createAccount(signUpForm);

        // then
        assertThat(savedAccount.getLoginId()).isEqualTo(signUpForm.getLoginId());
        assertThat(savedAccount.getPassword()).isNotEqualTo(signUpForm.getPassword());
        assertThat(savedAccount.getNickname()).isEqualTo(signUpForm.getNickname());
        assertThat(savedAccount.getEmail()).isEqualTo(signUpForm.getEmail());
        then(emailService).should().sendEmail(any());
    }

    @DisplayName("이메일로 계정 조회 쿼리 테스트")
    @Test
    void findAccountByEmail() {
        // when
        accountService.findAccountByEmail("test@email.com");

        // then
        then(accountRepository).should().findByEmail("test@email.com");
    }
}
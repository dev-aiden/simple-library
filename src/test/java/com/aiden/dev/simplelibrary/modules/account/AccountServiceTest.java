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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        then(emailService).should().sendEmail(any());
        assertThat(savedAccount.getLoginId()).isEqualTo(signUpForm.getLoginId());
        assertThat(savedAccount.getPassword()).isNotEqualTo(signUpForm.getPassword());
        assertThat(savedAccount.getNickname()).isEqualTo(signUpForm.getNickname());
        assertThat(savedAccount.getEmail()).isEqualTo(signUpForm.getEmail());
    }

    @DisplayName("이메일로 계정 조회 쿼리 테스트")
    @Test
    void findAccountByEmail() {
        // when
        accountService.findAccountByEmail("test@email.com");

        // then
        then(accountRepository).should().findByEmail("test@email.com");
    }

    @DisplayName("회원가입 인증 메일 발송 테스트")
    @Test
    void sendSignUpConfirmEmail() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("test1234")
                .build();

        // When
        accountService.sendSignUpConfirmEmail(account);

        // Then
        then(emailService).should().sendEmail(any());
    }

    @DisplayName("로그인 테스트")
    @Test
    void login() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("test1234")
                .build();

        // When
        accountService.login(account);
        UserAccount authenticationAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Then
        assertThat(authenticationAccount.getUsername()).isEqualTo("test");
        assertThat(authenticationAccount.getPassword()).isEqualTo("test1234");
    }

    @DisplayName("유저 정보 조회 테스트 - 사용자 아이디 미존재")
    @Test
    void loadUserByUsername_not_exist_login_id() {
        // When, Then
        assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername("test"));
    }

    @DisplayName("유저 정보 조회 테스트 - 사용자 아이디 존재")
    @Test
    void loadUserByUsername_exist_login_id() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("test1234")
                .nickname("test")
                .email("test@email.com")
                .build();

        given(accountRepository.findByLoginId(any())).willReturn(Optional.of(account));

        // When
        UserDetails userDetails = accountService.loadUserByUsername("test");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("test");
    }
}
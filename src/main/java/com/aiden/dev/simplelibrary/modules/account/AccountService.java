package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.infra.mail.EmailMessage;
import com.aiden.dev.simplelibrary.infra.mail.EmailService;
import com.aiden.dev.simplelibrary.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    public Account createAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = saveAccount(signUpForm);
        sendSignUpConfirmEmail(account);
        return account;
    }

    private Account saveAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .loginId(signUpForm.getLoginId())
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(signUpForm.getPassword())
                .build();
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account account) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("Simple Library 회원가입 인증")
                .message("/check-email-token?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Account account = accountRepository.findByLoginId(loginId).orElseThrow(() ->
                new UsernameNotFoundException(loginId));
        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public Account findAccountByNickname(String nickname) {
        return accountRepository.findByNickname(nickname).orElse(null);
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}

package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.modules.account.form.NotificationForm;
import com.aiden.dev.simplelibrary.modules.account.form.PasswordForm;
import com.aiden.dev.simplelibrary.modules.account.validator.CurrentAccount;
import com.aiden.dev.simplelibrary.modules.account.validator.PasswordFormValidator;
import com.aiden.dev.simplelibrary.modules.account.validator.ProfileFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ProfileFormValidator profileFormValidator;
    private final PasswordFormValidator passwordFormValidator;
    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @InitBinder("profileForm")
    public void profileFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormValidator);
    }

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentAccount Account account, @Validated ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        redirectAttributes.addFlashAttribute("message","프로필이 수정되었습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@CurrentAccount Account account, @Validated PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes redirectAttributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/notification")
    public String updateNotificationForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationForm.class));
        return "settings/notification";
    }

    @PostMapping("/notification")
    public String updateNotification(@CurrentAccount Account account, @Validated NotificationForm notificationForm, Errors errors,
                                     Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notification";
        }
        notificationForm.setBookRentalAvailabilityNotificationByWeb(true);
        accountService.updateNotification(account, notificationForm);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/settings/notification";
    }

    @GetMapping("/account")
    public String deleteAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        return "settings/account";
    }

    @DeleteMapping("/account")
    public String deleteAccount(@CurrentAccount Account account, RedirectAttributes redirectAttributes) {
        accountService.deleteAccount(account);
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("message", "계정이 삭제되었습니다.");
        return "redirect:/";
    }
}

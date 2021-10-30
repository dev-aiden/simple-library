package com.aiden.dev.simplelibrary.modules.account;

import com.aiden.dev.simplelibrary.modules.account.validator.CurrentAccount;
import com.aiden.dev.simplelibrary.modules.account.validator.ProfileFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ProfileFormValidator profileFormValidator;
    private final ModelMapper modelMapper;

    @InitBinder("profileForm")
    public void profileFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormValidator);
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }
}

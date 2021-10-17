package com.aiden.dev.simplelibrary.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SignUpForm {

    @NotBlank
    @Length(min = 3, max = 10)
    private String loginId;

    @NotBlank
    @Length(min = 8, max = 20)
    private String password;

    @NotBlank
    @Length(min = 8, max = 20)
    private String passwordConfirm;

    @NotBlank
    @Length(min = 1, max = 10)
    private String nickname;

    @NotBlank
    @Email
    private String email;
}

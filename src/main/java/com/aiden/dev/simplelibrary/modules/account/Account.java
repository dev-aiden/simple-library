package com.aiden.dev.simplelibrary.modules.account;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    @Lob
    private String profileImage;

    private Boolean bookRentalNotificationByEmail;

    private Boolean bookRentalNotificationByWeb;

    private Boolean bookReturnNotificationByEmail;

    private Boolean bookReturnNotificationByWeb;

    private Boolean bookRentalAvailabilityNotificationByEmail;

    private Boolean bookRentalAvailabilityNotificationByWeb;

    public void initNotificationSettings() {
        bookRentalNotificationByEmail = false;
        bookRentalNotificationByWeb = true;
        bookReturnNotificationByEmail = false;
        bookReturnNotificationByWeb = true;
        bookRentalAvailabilityNotificationByEmail = false;
        bookRentalAvailabilityNotificationByWeb = true;
    }

    public void generateEmailCheckToken() {
        this.emailVerified = false;
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isPossibleSendConfirmEmail() {
        return this.getEmailCheckTokenGeneratedAt().isBefore(LocalDateTime.now().minusHours(1));
    }
}

package com.aiden.dev.simplelibrary.modules.account;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
@DynamicInsert
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

    @Column(columnDefinition="tinyint(1) default 0")
    private Boolean bookRentalNotificationByEmail;

    @Column(columnDefinition="tinyint(1) default 1")
    private Boolean bookRentalNotificationByWeb;

    @Column(columnDefinition="tinyint(1) default 0")
    private Boolean bookReturnNotificationByEmail;

    @Column(columnDefinition="tinyint(1) default 1")
    private Boolean bookReturnNotificationByWeb;

    @Column(columnDefinition="tinyint(1) default 0")
    private Boolean bookRentalAvailabilityNotificationByEmail;

    @Column(columnDefinition="tinyint(1) default 1")
    private Boolean bookRentalAvailabilityNotificationByWeb;

    public void generateEmailCheckToken() {
        this.emailVerified = false;
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean isValidEmailCheckToken(String token) {
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

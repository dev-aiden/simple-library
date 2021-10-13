package com.aiden.dev.simplelibrary.modules.account;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private Boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    @Lob
    private String profileImage;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean notificationEmail;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean notificationWeb;
}

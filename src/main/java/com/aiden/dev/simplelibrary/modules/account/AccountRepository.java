package com.aiden.dev.simplelibrary.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Boolean existsByLoginId(String loginId);

    Boolean existsByNickname(String nickname);

    Boolean existsByEmail(String email);
}

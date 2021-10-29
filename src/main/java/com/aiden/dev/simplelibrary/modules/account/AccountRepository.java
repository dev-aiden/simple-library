package com.aiden.dev.simplelibrary.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {

    Boolean existsByLoginId(String loginId);

    Boolean existsByNickname(String nickname);

    Boolean existsByEmail(String email);

    Optional<Account> findByLoginId(String loginId);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByNickname(String nickname);
}

package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

// readOnly를 써서 write Lock 방지 토록 한다. (성능)
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String s);
}

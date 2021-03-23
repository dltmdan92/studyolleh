package com.seungmoo.studyolleh.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

// readOnly를 써서 write Lock 방지 토록 한다. (성능)
// 주의!!!!  아래 우리가 만든 method도 transaction 처리할려면 @Transactional 꼭! 달아주도록
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account>, AccountRepositoryExtension {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String s);

    Account findByNickname(String emailOrNickname);
}

package com.seungmoo.studyolleh.modules.notification;

import com.seungmoo.studyolleh.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByAccountAndChecked(Account account, boolean b);

    // 이거는 readOnly가 아니어야 한다.
    // 왜냐하면 List로 받은 notification들을 checked를 바꿀 것이기 때문에!!!
    @Transactional
    List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean b);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean b);
}

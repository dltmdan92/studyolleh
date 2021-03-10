package com.seungmoo.studyolleh.enrollment;

import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.Enrollment;
import com.seungmoo.studyolleh.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}

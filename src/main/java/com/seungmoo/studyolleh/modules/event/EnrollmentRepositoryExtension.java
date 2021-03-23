package com.seungmoo.studyolleh.modules.event;

import com.seungmoo.studyolleh.modules.account.Account;

import java.util.List;

public interface EnrollmentRepositoryExtension {
    List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, Boolean accepted);
}

package com.seungmoo.studyolleh.modules.event;

import com.querydsl.jpa.JPQLQuery;
import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.study.QStudy;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class EnrollmentRepositoryExtensionImpl extends QuerydslRepositorySupport implements EnrollmentRepositoryExtension {

    public EnrollmentRepositoryExtensionImpl() {
        super(Enrollment.class);
    }

    @Override
    public List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, Boolean accepted) {
        QEnrollment enrollment = QEnrollment.enrollment;
        QEvent event = QEvent.event;
        QStudy study = QStudy.study;

        JPQLQuery<Enrollment> enrollmentJPQLQuery = from(enrollment)
                .where(enrollment.account.eq(account).and(enrollment.accepted.eq(accepted)))
                .leftJoin(enrollment.event, event).fetchJoin()
                // JPA 엔티티에서 SubGraph 형식으로도 구현할 수 있다. (in EntityGraph)
                .leftJoin(event.study, study).fetchJoin();

        return enrollmentJPQLQuery.fetch();
    }
}

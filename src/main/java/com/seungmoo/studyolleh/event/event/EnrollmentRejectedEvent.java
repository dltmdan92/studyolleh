package com.seungmoo.studyolleh.event.event;

import com.seungmoo.studyolleh.domain.Enrollment;
import org.springframework.context.ApplicationEvent;

public class EnrollmentRejectedEvent extends EnrollmentEvent {
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 거절했습니다.");
    }
}

package com.seungmoo.studyolleh.modules.event;

import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.event.event.EnrollmentAcceptedEvent;
import com.seungmoo.studyolleh.modules.event.event.EnrollmentRejectedEvent;
import com.seungmoo.studyolleh.modules.study.Study;
import com.seungmoo.studyolleh.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
        // TODO 모집 인원을 늘린 선착순 모임의 경우, 자동으로 추가 인원의 참가 신청을 확정 상태로 변경해야 한다.
        //eventRepository.save(event); --> 이거 할 필요가 없다. event 객체는 영속성 컨텍스트에서 persist 상태임.
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임 정보를 수정했으니 확인하세요."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                "'" + event.getTitle() + "' 모임을 취소했습니다."));
    }

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            // 우선 event에서 enrollment와의 관계를 끊고
            event.removeEnrollment(enrollment);
            // enrollment를 삭제한다.
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}

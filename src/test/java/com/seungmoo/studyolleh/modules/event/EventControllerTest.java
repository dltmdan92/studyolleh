package com.seungmoo.studyolleh.modules.event;

import com.seungmoo.studyolleh.infra.AbstractContainerBaseTest;
import com.seungmoo.studyolleh.infra.MockMvcTest;
import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.account.AccountFactory;
import com.seungmoo.studyolleh.modules.account.AccountRepository;
import com.seungmoo.studyolleh.modules.account.WithAccount;
import com.seungmoo.studyolleh.modules.study.Study;
import com.seungmoo.studyolleh.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("seungmoo")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account dltmdan92 = accountFactory.createAccount("dltmdan92");
        Study study = studyFactory.createStudy("test-study", dltmdan92);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, dltmdan92);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        isAccepted(seungmoo, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithAccount("seungmoo")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account dltmdan92 = accountFactory.createAccount("dltmdan92");
        Study study = studyFactory.createStudy("test-study", dltmdan92);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, dltmdan92);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        isNotAccepted(seungmoo, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("seungmoo")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Account dltmdan92 = accountFactory.createAccount("dltmdan92");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", dltmdan92);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, dltmdan92);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, seungmoo);
        eventService.newEnrollment(event, dltmdan92);

        isAccepted(may, event);
        isAccepted(seungmoo, event);
        isNotAccepted(dltmdan92, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(dltmdan92, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, seungmoo));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("seungmoo")
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Account dltmdan92 = accountFactory.createAccount("dltmdan92");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", dltmdan92);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, dltmdan92);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, dltmdan92);
        eventService.newEnrollment(event, seungmoo);

        isAccepted(may, event);
        isAccepted(dltmdan92, event);
        isNotAccepted(seungmoo, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(dltmdan92, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, seungmoo));
    }

    private void isNotAccepted(Account dltmdan92, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, dltmdan92).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("seungmoo")
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account dltmdan92 = accountFactory.createAccount("dltmdan92");
        Study study = studyFactory.createStudy("test-study", dltmdan92);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, dltmdan92);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        isNotAccepted(seungmoo, event);
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }

}
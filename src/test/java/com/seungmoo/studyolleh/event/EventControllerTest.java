package com.seungmoo.studyolleh.event;

import com.seungmoo.studyolleh.domain.Event;
import com.seungmoo.studyolleh.domain.Study;
import com.seungmoo.studyolleh.study.StudyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class EventControllerTest {

    @Autowired
    private StudyService studyService;

    @Autowired
    private EventRepository eventRepository;

    //@Test
    void getEvents() {
        Study study = studyService.getStudy("봄싹");

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);

        events.forEach(event -> log.info(event.toString()));

    }

}
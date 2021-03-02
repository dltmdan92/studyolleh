package com.seungmoo.studyolleh.event;

import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.Event;
import com.seungmoo.studyolleh.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreateBy(account);
        event.setCreateDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }
}

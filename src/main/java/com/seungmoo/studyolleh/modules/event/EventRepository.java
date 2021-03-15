package com.seungmoo.studyolleh.modules.event;

import com.seungmoo.studyolleh.modules.study.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    // N+1 쿼리를 추가로 또 호출하는 문제를 해결 하기 위함!!
    @EntityGraph(value = "Event.withEnrollments", type = EntityGraph.EntityGraphType.FETCH)
    List<Event> findByStudyOrderByStartDateTime(Study study);
}

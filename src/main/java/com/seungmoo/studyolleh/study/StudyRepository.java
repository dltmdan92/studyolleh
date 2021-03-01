package com.seungmoo.studyolleh.study;

import com.seungmoo.studyolleh.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    // LOAD 타입 : 우리가 명시한 Entity 데이터들은 EAGER 모드로 갖고오고, 나머지는 기본 FetchType에 따른다.
    // ex) One으로 끝나는건 Eager, Many로 끝나는 건 Lazy fetch가 기본이다.
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
}

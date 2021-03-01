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

    // FETCH 타입 : 우리가 선언한거는 EAGER, 나머지는 LAZY로 갖고 온다. (이름 좀 헷갈리게 지은듯)
    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    // jpa가 WithTags 이런 부분은 모른다(무시함), 근데 우리가 EntityGraph 구별 줄라고 메서드 셋팅한거임
    Study findAccountWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    // jpa가 WithZones 이런 부분은 모른다(무시함), 근데 우리가 EntityGraph 구별 줄라고 메서드 셋팅한거임
    Study findAccountWithZonesByPath(String path);
}

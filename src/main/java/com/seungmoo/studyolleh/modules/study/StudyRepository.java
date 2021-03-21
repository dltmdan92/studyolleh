package com.seungmoo.studyolleh.modules.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    // LOAD 타입 : 우리가 명시한 Entity 데이터들은 EAGER 모드로 갖고오고, 나머지는 기본 FetchType에 따른다.
    // ex) One으로 끝나는건 Eager, Many로 끝나는 건 Lazy fetch가 기본이다.
    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    // FETCH 타입 : 우리가 선언한거는 EAGER, 나머지는 LAZY로 갖고 온다. (이름 좀 헷갈리게 지은듯)
    @EntityGraph(attributePaths = {"tags", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    // jpa가 WithTags 이런 부분은 모른다(무시함), 근데 우리가 EntityGraph 구별 줄라고 메서드 셋팅한거임
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = "members")
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    // EntityGrap의 기본 타입 전략이 FETCH 이므로 생략 가능하다.
    //@EntityGraph(attributePaths = {"tags", "zones"}, type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"tags", "zones"})
    Study findStudyWithTagsAndZonesById(Long id);

    //@EntityGraph(attributePaths = {"managers", "members"}, type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"managers", "members"})
    Study findStudyWithManagersAndMembersById(Long id);
}

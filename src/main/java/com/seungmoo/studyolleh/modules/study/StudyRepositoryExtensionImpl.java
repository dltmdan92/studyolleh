package com.seungmoo.studyolleh.modules.study;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

/**
 * JPA에서 interface의 구현체를 만드는 경우
 * 반드시 !!! postfix로 Impl을 달아줘야 한다.
 */
public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    /**
     * 하위 클래스 인스턴스를 생성할 때,
     * 자바에서는 반드시 상위 클래스를 생성할려고 한다. (상위 클래스의 기본 생성자를 통해서)
     * 근데 상위클래스에는 기본 생성자가 없다.
     * 그러므로 아래 생성자를 호출해줘야 한다. (super(domainClass))
     *
     */
    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    /**
     * 기존에는 Fetch 할 엔티티들을 EntityGraph를 통해서 선언했다.
     * 하지만 QueryDsl에서는 그 방법이 조금 다름!!
     *
     * @param keyword
     * @return
     */
    @Override
    public List<Study> findByKeyword(String keyword) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)));

        // fetch를 쓰면 데이터를 가져올 수 있다.
        //query.fetchResults() --> 요거는 페이징 처리할 때 주로 씀
        return query.fetch();
    }
}

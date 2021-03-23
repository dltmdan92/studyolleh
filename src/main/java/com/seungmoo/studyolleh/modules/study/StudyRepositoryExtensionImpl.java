package com.seungmoo.studyolleh.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.account.QAccount;
import com.seungmoo.studyolleh.modules.tag.QTag;
import com.seungmoo.studyolleh.modules.tag.Tag;
import com.seungmoo.studyolleh.modules.zone.QZone;
import com.seungmoo.studyolleh.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

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
     * @param pageable
     * @return
     */
    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;

        // tag, zone 등 을 leftJoin해서 안 갖고 오면,
        // study entity에서 tag, zone을 호출할 때 쿼리를 또 호출 하게 된다 --> N + 1 Select 문제 발생!!!
        // QueryDsl에서는 leftJoin, fetchJoin을 통해서 Fetching을 한다. --> N + 1 Select 문제 해결
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                // 여기서 leftJoin만 해주고(left outer join으로 먹힘), fetchJoin()을 호출 안해주면
                // left outer join은 하되, 데이터는 select 절에서 안 갖고 온다. --> 그러므로 fetchJoin() 호출 해주자.
                .leftJoin(study.tags, QTag.tag).fetchJoin() // fetchJoin : leftJoin에 엮이는 오른쪽 테이블의 데이터 또한 가져 온다.
                .leftJoin(study.zones, QZone.zone).fetchJoin() // fetchJoin : leftJoin에 엮이는 오른쪽 테이블의 데이터 또한 가져 온다.
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct(); // distinct를 통해 중복되는 데이터는 결과 중에서 없앤다.
        // distinct 를 해도 원래는 query의 결과는 여러개 이다. --> distinct를 통해서 전체 데이터에 대한 transform을 해줄 뿐이다. (전체 데이터는 원래대로 뽑힌다.)
        // distinct 말고 projection 등의 튜닝 방법이 있긴 하다. but 좀 어렵긴 하다.

        // QueryDsl에서 pageable을 쓸 수 있는 방식이다. --> 페이징 정보를 포함해서 result 쓰려면 fetchResults()
        JPQLQuery<Study> studyJPQLQuery = getQuerydsl().applyPagination(pageable, query);

        // fetch를 쓰면 데이터를 가져올 수 있다.
        //query.fetchResults() --> 요거는 페이징 처리할 때 주로 씀
        //return query.fetch();
        QueryResults<Study> studyQueryResults = studyJPQLQuery.fetchResults();// fetchResults() 페이징 정보까지 갖고 온다.
        return new PageImpl<>(studyQueryResults.getResults(), pageable, studyQueryResults.getTotal());
    }

    @Override
    public List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, Boolean closed) {
        QStudy study = QStudy.study;

        JPQLQuery<Study> studyJPQLQuery = from(study)
                .where(study.managers.any().eq(account).and(study.closed.eq(closed)))
                .offset(0).limit(5);

        return studyJPQLQuery.fetch();
    }

    @Override
    public List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed) {
        QStudy study = QStudy.study;

        JPQLQuery<Study> studyJPQLQuery = from(study)
                .where(study.members.any().eq(account).and(study.closed.eq(closed)))
                .offset(0).limit(5);

        return studyJPQLQuery.fetch();
    }

    @Override
    public List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed) {
        QStudy study = QStudy.study;

        JPQLQuery<Study> studyJPQLQuery = from(study)
                .where(study.published.eq(published).and(study.closed.eq(closed)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .offset(0).limit(9);

        return studyJPQLQuery.fetch();
    }

    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;

        JPQLQuery<Study> studyJPQLQuery = from(study)
                .where(study.published.isTrue()
                        .and(study.closed.isFalse())
                        .and(study.zones.any().in(zones))
                        .and(study.tags.any().in(tags)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                // left Join해서 fetch해올 때는 중복데이터가 생길 수 있음! --> distinct 처리 해주자.
                .distinct()
                .limit(9);

        return studyJPQLQuery.fetch();
    }
}

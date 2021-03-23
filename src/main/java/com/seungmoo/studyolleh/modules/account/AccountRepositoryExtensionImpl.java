package com.seungmoo.studyolleh.modules.account;

import com.querydsl.jpa.JPQLQuery;
import com.seungmoo.studyolleh.modules.tag.QTag;
import com.seungmoo.studyolleh.modules.zone.QZone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class AccountRepositoryExtensionImpl extends QuerydslRepositorySupport implements AccountRepositoryExtension {

    public AccountRepositoryExtensionImpl() {
        super(Account.class);
    }

    @Override
    public Account findAccountWithTagsAndZonesById(Long id) {
        QAccount account = QAccount.account;

        JPQLQuery<Account> accountJPQLQuery = from(account)
                .where(account.id.eq(id))
                .leftJoin(account.tags, QTag.tag).fetchJoin()
                .leftJoin(account.zones, QZone.zone).fetchJoin();

        return accountJPQLQuery.fetchOne();
    }

}

package com.seungmoo.studyolleh.modules.account;

import com.querydsl.core.types.Predicate;
import com.seungmoo.studyolleh.modules.tag.Tag;
import com.seungmoo.studyolleh.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {

        QAccount account = QAccount.account;

        return account.zones.any().in(zones) // account중에 아무거나 zones에 매칭되고
                .and(account.tags.any().in(tags));
    }

}

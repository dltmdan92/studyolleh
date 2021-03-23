package com.seungmoo.studyolleh.modules.account;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepositoryExtension {
    Account findAccountWithTagsAndZonesById(Long id);
}

package com.seungmoo.studyolleh.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ObjectMother class
 *
 * 테스트 시,  Factory class에 중복되는 로직을 위임하였다. (상속 없이)
 *
 * 특히나 JUnit은 상속하면 안된다. (상위클래스도 다같이 실행된다.)
 * 테스트에서 상속 관계는 맺어주지 않는게 좋다.
 */
@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account whiteship = new Account();
        whiteship.setNickname(nickname);
        whiteship.setEmail(nickname + "@email.com");
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            return accountRepository.save(whiteship);
        }
        return account;
    }

}

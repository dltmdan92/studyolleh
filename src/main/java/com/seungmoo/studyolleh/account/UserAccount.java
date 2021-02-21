package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * spring-security의 User Type인 Principal 객체
 * @CurrentUser 애노테이션에서 @AuthenticationPrincipal 애노테이션의 SpEL을 사용하기 위해, Principal을 셋팅
 */
@Getter
public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}

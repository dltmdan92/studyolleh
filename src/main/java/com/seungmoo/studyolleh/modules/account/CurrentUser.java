package com.seungmoo.studyolleh.modules.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
// 애노테이션 참조하는 Principal이 anonymousUser 면 null
// 익명 사용자일 경우, Principal은 "anonymousUser" 라는 문자열이다.
// 익명 사용자가 아니라면, Principal은 우리가 설정한 UserAccount가 될 것이며, SpEL로 인해 account라는 인스턴스로 대체될 것.
// 여기서 UserAccount라는 Principal 객체의 getAccount() 메서드를 사용해서 account 프로퍼티를 꺼낼 것임.
// 참고로 Principal은 AccountService.class의 login() 메서드에서 principal 셋팅 해주고 있음
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {
}

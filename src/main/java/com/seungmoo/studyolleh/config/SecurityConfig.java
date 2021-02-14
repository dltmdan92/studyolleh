package com.seungmoo.studyolleh.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // spring security 설정을 내가 하겠다 라고 설정
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll() // 다 허용
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // 얘는 GET 만 허용
                .anyRequest().authenticated() // 나머지 요청들은 로그인 해야 만 접근 가능
                ;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // static resource는 Spring Security Filter 적용하지 말 것.
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}

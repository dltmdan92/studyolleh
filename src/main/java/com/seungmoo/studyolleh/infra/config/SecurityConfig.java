package com.seungmoo.studyolleh.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity // spring security 설정을 내가 하겠다 라고 설정
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService accountService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link", "/search/study").permitAll() // 다 허용
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // 얘는 GET 만 허용
                .anyRequest().authenticated() // 나머지 요청들은 로그인 해야 만 접근 가능
                ;

        // form 로그인 기능 활성화
        http.formLogin()
                // loginPage 셋팅안하면, 그냥 스프링 시큐리티의 기본적인 페이지로 제공한다.
                // loginPage : 우리가 커스텀하게 셋팅한 로그인페이지
                .loginPage("/login").permitAll();

        http.logout()
                // 로그아웃 했을 때 어디로 갈지
                .logoutSuccessUrl("/");

        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());
    }

    // DB에 username, series(불변 해쉬값), token(가변 해쉬값)을 저장함으로써
    // 영속적인 로그인 rememberMe를 설정한다.
    @Bean
    public PersistentTokenRepository tokenRepository() {
        // Jdbc 기반의 Token Repository 구현체
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // static resource는 Spring Security Filter 적용하지 말 것.
        web.ignoring()
                // node_modules에 있는 것들도 Spring Securty에 걸린다.
                // atCommonLocations에 보면 /node_modules는 없음, 별도 설정해준다.
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}

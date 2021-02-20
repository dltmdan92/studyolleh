package com.seungmoo.studyolleh.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        // 403 error 발생, spring-security는 기본적으로 CSRF 공격 방지 기능을 지원한다. (spring-security 강의 참고)
        // 일반적으로 웹서버 톰캣 돌려서 실행할 경우 sign-up 페이지에 csrf 토큰 값이 input tag로 박혀져 있을 거임
        // 근데 그냥 JUnit 테스트를 돌릴 경우, CSRF token이 없어서 403이 발생하게 된다.
        mockMvc.perform(post("/sign-up")
                .param("nickname", "seungmoo")
                .param("email", "email..")
                .param("password", "12345")
                .with(csrf())) // 이렇게 junit에서도 csrf token를 넣어서 테스트 가능하다!
                .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(view().name("account/sign-up"));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        // 403 error 발생, spring-security는 기본적으로 CSRF 공격 방지 기능을 지원한다. (spring-security 강의 참고)
        // 일반적으로 웹서버 톰캣 돌려서 실행할 경우 sign-up 페이지에 csrf 토큰 값이 input tag로 박혀져 있을 거임
        // 근데 그냥 JUnit 테스트를 돌릴 경우, CSRF token이 없어서 403이 발생하게 된다.
        mockMvc.perform(post("/sign-up")
                .param("nickname", "seungmoo")
                .param("email", "seungmoo@email.com")
                .param("password", "12345678")
                .with(csrf())) // 이렇게 junit에서도 csrf token를 넣어서 테스트 가능하다!
                .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"));

        assertTrue(accountRepository.existsByEmail("seungmoo@email.com"));

        // SimpleMailMessage --> 이거 Type의 Instance를 가지고 send()메서드가 호출이 되었는가를 검증
        // 메일 보냈다를 검증
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}
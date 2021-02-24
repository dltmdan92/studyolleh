package com.seungmoo.studyolleh.settings;

import com.seungmoo.studyolleh.WithAccount;
import com.seungmoo.studyolleh.account.AccountRepository;
import com.seungmoo.studyolleh.account.AccountService;
import com.seungmoo.studyolleh.account.SignUpForm;
import com.seungmoo.studyolleh.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    //@BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("seungmoo");
        signUpForm.setEmail("seungmoo@naver.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    // @WithUserDetails 이게 @BeforeEach 코드보다 먼저 실행돼서 UserDetailsService에서 loadUser 시, 실패 발생!!@!
    // setupBefore = TestExecutionEvent.TEST_EXECUTION --> 이것도 버그가 있어서 @WithUserDetails가 먼저 실행된다고 하나,
    // 현 스프링부트 2.4.2 버전에서는 setupBefore = TestExecutionEvent.TEST_EXECUTION 정상 적용 되고 있음
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        assertEquals(bio, seungmoo.getBio());
    }

    /**
     * 이거 실행할 때는 위의 @BeforeEach 꺼놓을 것!!!
     * @throws Exception
     */
    @WithAccount("seungmoo")
    @DisplayName("프로필 수정하기 - 입력값 정상, 커스텀 애노테이션 사용(WithAccountSecurityContextFactory 활용)")
    @Test
    void updateProfileCustomAnno() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        assertEquals(bio, seungmoo.getBio());
    }

    /**
     * 이거 실행할 때는 위의 @BeforeEach 꺼놓을 것!!!
     * @throws Exception
     */
    @WithAccount("seungmoo")
    @DisplayName("프로필 수정하기 - 입력값 에러 (커스텀 애노테이션)")
    @Test
    void updateProfileCustomAnno_ERROR() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.짧은 소개를 수정하는 경우.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        assertNotNull(seungmoo);
    }

}
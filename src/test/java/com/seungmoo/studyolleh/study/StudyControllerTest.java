package com.seungmoo.studyolleh.study;

import com.seungmoo.studyolleh.WithAccount;
import com.seungmoo.studyolleh.account.AccountRepository;
import com.seungmoo.studyolleh.account.AccountService;
import com.seungmoo.studyolleh.account.SignUpForm;
import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.Study;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

    @BeforeEach
    void beforeEach() {
        if (accountRepository.findByNickname("seungmoo") == null) {
            SignUpForm signUpForm = new SignUpForm();
            signUpForm.setNickname("seungmoo");
            signUpForm.setEmail("seungmoo@naver.com");
            signUpForm.setPassword("12345678");
            accountService.processNewAccount(signUpForm);
        }
    }

    @AfterEach
    void afterEach() {
        //accountRepository.deleteAll();
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 완료")
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("seungmoo");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath("test-path");
        assertNull(study);
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        studyService.createNewStudy(study, seungmoo);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    private Account createAccount(String nickname) {
        Account whiteship = new Account();
        whiteship.setNickname(nickname);
        whiteship.setEmail(nickname + "@email.com");
        accountRepository.save(whiteship);
        return whiteship;
    }

    private Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 가입")
    void joinStudy() throws Exception {
        Account whiteship = createAccount("whiteship");
        Study study = createStudy("test-study", whiteship);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account keesun = accountRepository.findByNickname("seungmoo");
        assertTrue(study.getMembers().contains(keesun));
    }

    @Test
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account whiteship = createAccount("whiteship");
        Study study = createStudy("test-study", whiteship);
        Account keesun = accountRepository.findByNickname("seungmoo");
        studyService.addMember(study, keesun);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(keesun));
    }

    @Test
    @WithAccount("seungmoo")
    @DisplayName("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account whiteship = createAccount("whiteship");
        Study study = createStudy("test-study", whiteship);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @WithAccount("seungmoo")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Study study = createStudy("test-study", seungmoo);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("seungmoo")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Study study = createStudy("test-study", seungmoo);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "short description")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("seungmoo")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Study study = createStudy("test-study", seungmoo);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }
}
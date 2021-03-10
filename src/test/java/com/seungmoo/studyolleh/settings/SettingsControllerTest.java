package com.seungmoo.studyolleh.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seungmoo.studyolleh.WithAccount;
import com.seungmoo.studyolleh.account.AccountRepository;
import com.seungmoo.studyolleh.account.AccountService;
import com.seungmoo.studyolleh.account.SignUpForm;
import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.Tag;
import com.seungmoo.studyolleh.domain.Zone;
import com.seungmoo.studyolleh.settings.form.TagForm;
import com.seungmoo.studyolleh.settings.form.ZoneForm;
import com.seungmoo.studyolleh.tag.TagRepository;
import com.seungmoo.studyolleh.zone.ZoneRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();


    @BeforeEach
    void beforeEach() {
        if (accountRepository.findByNickname("seungmoo") == null) {
            SignUpForm signUpForm = new SignUpForm();
            signUpForm.setNickname("seungmoo");
            signUpForm.setEmail("seungmoo@naver.com");
            signUpForm.setPassword("12345678");
            accountService.processNewAccount(signUpForm);
        }

        if (zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince()) == null) {
            zoneRepository.save(testZone);
        }
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
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
     * @WithAccount 이거 실행할 때는 위의 @BeforeEach 꺼놓을 것!!!
     * @throws Exception
     */
    //@WithAccount("seungmoo")
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    //@WithAccount("seungmoo")
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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

    //@WithAccount("seungmoo")
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSOWRD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    //@WithAccount("seungmoo")
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSOWRD_URL)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSOWRD_URL))
                .andExpect(flash().attributeExists("message"));
    }

    //@WithAccount("seungmoo")
    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("패스워드 수정 - 입력값 오류")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSOWRD_URL)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "12345678923")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSOWRD_VIEW_NAME));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정에 태그 추가")
    @Test
    @Transactional // 아래 소스를 테스트하기 위해 Transaction 선언해준다.
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        // 요청 보낼 때 트랜잭션 시작
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        // 여기까지 트랜잭션 유지

        // 그럼 아래는???
        // 트랜잭션 유지가 안된다!

        // 이런애들은 Repository가 실행할때면 Transaction이다.
        // 아래 애들은 Detached 상태!!
        Optional<Tag> newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag.get());
        assertTrue(accountRepository.findByNickname("seungmoo").getTags().contains(newTag.get()));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정에 태그 삭제")
    @Test
    @Transactional // 아래 소스를 테스트하기 위해 Transaction 선언해준다.
    void removeTag() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(seungmoo, newTag);

        assertTrue(seungmoo.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        // 요청 보낼 때 트랜잭션 시작
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        // 여기까지 트랜잭션 유지

        assertFalse(seungmoo.getTags().contains(newTag));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정의 지역 정보 추가")
    @Test
    @Transactional
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        // failed to lazily initialize a collection of role 발생!!
        // seungmoo 객체는 이미 영속성 컨텍스트가 종료되었다! @Transactional 선언 잊지 말자 ㅠ
        assertTrue(seungmoo.getZones().contains(zone));
    }

    @WithUserDetails(value = "seungmoo", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("계정의 지역 정보 제거")
    @Test
    @Transactional
    void removeZone() throws Exception {
        Account seungmoo = accountRepository.findByNickname("seungmoo");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(seungmoo, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        // failed to lazily initialize a collection of role 발생!!
        // seungmoo 객체는 이미 영속성 컨텍스트가 종료되었다! @Transactional 선언 잊지 말자 ㅠ
        assertFalse(seungmoo.getZones().contains(zone));
    }

}
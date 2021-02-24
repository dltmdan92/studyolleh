package com.seungmoo.studyolleh.settings;

import com.seungmoo.studyolleh.account.AccountService;
import com.seungmoo.studyolleh.account.CurrentUser;
import com.seungmoo.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";

    static final String SETTINGS_PASSOWRD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSOWRD_URL = "/settings/password";

    private final AccountService accountService;
    private final PasswordFormValidator passwordFormValidator;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    // @ModelAttribute -> 생략 가능
    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            // 기본적으로 Model parameter에는 Form을 채웠던 데이터(@ModelAttribute Profile)는 자동으로 들어가있다.
            // Errors 정보 또한 model에 들어감
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        // 주의!!! account --> 얘는 영속성 컨텍스트에서 관리되는 객체가 아니다!!
        // account --> 얘는 우리가 AuthencationPrincipal통해 가져온 Principal 객체 인 것이다.
        // account -> 얘를 읽어서 여기 Controller로 왔을 때는 이미 Transaction이 끝난 상태이다!!
        // account 얘의 상태는 (Detached 상태의 객체이다. -> 한번이라도 Jpa가 관리했던 객체이다. Transient는 새로 만든 객체)
        // Detached는 아무리 변경해도 관리하지 않는다. 이거를 다시 Jpa에서 영속성 관리를 해줄려면?? --> accountRepository.save(account)
        // save 구현체에서 Id값을 보고 Id값이 있으면 Merge를 시킨다. 즉, 기존 데이터에 update를 시킨다.
        accountService.updateProfile(account, profile);
        // 리다이렉트 하는 핸들러에도 attr을 유지 --> FlashAttribute
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    @GetMapping(SETTINGS_PASSOWRD_URL)
    public String passwordUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSOWRD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSOWRD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSOWRD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + SETTINGS_PASSOWRD_URL;
    }

}

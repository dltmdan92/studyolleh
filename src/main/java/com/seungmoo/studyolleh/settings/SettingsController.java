package com.seungmoo.studyolleh.settings;

import com.seungmoo.studyolleh.account.AccountService;
import com.seungmoo.studyolleh.account.CurrentUser;
import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.settings.form.NicknameForm;
import com.seungmoo.studyolleh.settings.form.Notifications;
import com.seungmoo.studyolleh.settings.form.PasswordForm;
import com.seungmoo.studyolleh.settings.form.Profile;
import com.seungmoo.studyolleh.settings.validator.NicknameValidator;
import com.seungmoo.studyolleh.settings.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    static final String SETTINGS_PROFILE_URL = "/"+SETTINGS_PROFILE_VIEW_NAME;
    static final String SETTINGS_PASSOWRD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSOWRD_URL = "/"+SETTINGS_PASSOWRD_VIEW_NAME;
    static final String SETTINGS_NOTIFICATIONS_URL = "settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "/"+SETTINGS_NOTIFICATIONS_URL;
    static final String SETTINGS_ACCOUNT_URL = "settings/account";
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "/"+SETTINGS_ACCOUNT_URL;

    private final AccountService accountService;
    private final PasswordFormValidator passwordFormValidator;
    private final NicknameValidator nicknameValidator;
    private final ModelMapper modelMapper;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        //model.addAttribute(new Profile(account));
        // account -> profile
        model.addAttribute(modelMapper.map(account, Profile.class));
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

    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        //model.addAttribute(new Notifications(account));
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS_NOTIFICATIONS_URL;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm,
                                Model model, Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }
        accountService.updateNickname(account ,nicknameForm.getNickname());
        redirectAttributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS_ACCOUNT_URL;
    }

}

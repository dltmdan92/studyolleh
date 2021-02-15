package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.ConsoleMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    /**
     * API로 signUpForm 데이터 받을 때, Binder 를 설정할 수 있다.
     * 그리고 이 Binder에 validator를 추가할 수 있다.
     * signUpForm 객체(객체 타입명 camelCase로 follow 한다.)를 받을 때 (ex POST)
     * binder가 적용된다.
     * @param webDataBinder
     */
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        /**
         * 그냥 뷰 띄워줄 경우, 스프링 시큐리티에 의해서 막힘 (login 화면으로 넘어가게 됨)
         * spring security config를 별도로 해줘야 한다.
         */
        //model.addAttribute("signUpForm", new SignUpForm());
        // 위의 코드를 아래와 같이 축약 가능하다. (해당하는 객체의 camelCase로 attribute가 생성된다.)
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    /**
     *
     * @param signUpForm  @ModelAttribute annotation은 생략 가능
     * @return
     */
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                // TODO encoding 반드시 해야함
                .password(signUpForm.getPassword())
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        // NEW 회원 정보 저장
        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();

        // 이메일을 보내 봅시다.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 받는 사람
        mailMessage.setTo(newAccount.getEmail());
        // 제목
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        // 본문
        mailMessage.setText("/check-email-token" +
                "?token="+newAccount.getEmailCheckToken()
                +"&email="+newAccount.getEmail());
        javaMailSender.send(mailMessage);
        return "redirect:/";
    }
}

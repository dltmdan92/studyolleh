package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

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
     * @return 정상 : redirect, error : account/sign-up 페이지
     */
    @PostMapping("/sign-up")
    public ModelAndView signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        ModelAndView mv = new ModelAndView();
        if (errors.hasErrors()) {
            mv.setViewName("account/sign-up");
            mv.setStatus(HttpStatus.BAD_REQUEST);
            return mv;
        }
        accountService.processNewAccount(signUpForm);
        mv.setViewName("redirect:/");
        return mv;
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        try {
            // Null 체크 통과 한 account
            Account nonNullAccount = Optional.ofNullable(account)
                    .orElseThrow(AccountNotFoundException::new);

            if (!nonNullAccount.getEmailCheckToken().equals(token)) {
                model.addAttribute("error", "wrong.token");
            }

            nonNullAccount.setEmailVerified(true);
            nonNullAccount.setJoinedAt(LocalDateTime.now());
            // JpaResitory가 기본 제공하는 count() 메소드를 활용 가능하다.
            model.addAttribute("numberOfUser", accountRepository.count());
            model.addAttribute("nickname", nonNullAccount.getNickname());
            return view;
        } catch (AccountNotFoundException e) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
    }
}

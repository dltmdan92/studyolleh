package com.seungmoo.studyolleh.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;
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
        // 위의 코드를 아래와 같이 축약 가능하다. (해당하는 객체 Type의 camelCase로 attribute가 생성된다.)
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
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        mv.setViewName("redirect:/");
        return mv;
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account;
        String view = "account/checked-email";
        try {
            // Null 체크 통과 한 account
            // 이 녀석은 Persistent한 객체 이다. 영속성 컨텍스트에서 관리가 되는 객체임.
            account = Optional.ofNullable(accountRepository.findByEmail(email))
                    .orElseThrow(AccountNotFoundException::new);
            if (!account.isValidToken(token)) {
                model.addAttribute("error", "wrong.token");
            }

            // completeSignUp을 통해 발생한 객체의 변경사항이 DB에 반영안된다.
            // EntityManager가 영속성 컨텍스트에서 Persistent 객체들을 관리함. 트랜잭션이 종료될 떄 DB에 반영해줌 (update query)
            // 트랜잭션안에서 일어난 사항만 변경사항을 관리한다. (트랜잭션 셋팅을 안해주면, 변경 반영이 없다..)
            // 아래 두개는 AccountService로 옮기고 transaction 관리를 해주도록 하자.
            // 그리고 뷰 랜더링 할 떄까지 영속성 컨텍스트를 유지함
            /*account.completeSignUp();
            accountService.login(account);*/
            accountService.completeSignUp(account);
            // JpaResitory가 기본 제공하는 count() 메소드를 활용 가능하다.
            model.addAttribute("numberOfUser", accountRepository.count());
            model.addAttribute("nickname", account.getNickname());
        } catch (AccountNotFoundException e) {
            model.addAttribute("error", "wrong.email");
        }
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        // /resend-confirm-email 를 계속 리로딩 했을 경우, 이메일 계속 호출 또는 로직 실행될 수 있음.
        // redirect 시켜서 이런 걸 방지하도록 하자.
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {

        Account accountToView = accountService.getAccount(nickname);

        // model.addAttribute("account", byNickname);
        // 이 attribute는 위와 똑같이 "account"로 naming 된다.
        // attr의 이름을 주지 않으면, 해당 객체 타입의 CamelCase로 naming
        model.addAttribute(accountToView);
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

}

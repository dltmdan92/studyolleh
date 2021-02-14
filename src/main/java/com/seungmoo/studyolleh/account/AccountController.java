package com.seungmoo.studyolleh.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

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
}

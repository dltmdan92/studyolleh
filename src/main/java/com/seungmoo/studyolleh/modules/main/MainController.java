package com.seungmoo.studyolleh.modules.main;

import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.account.CurrentUser;
import com.seungmoo.studyolleh.modules.notification.NotificationRepository;
import com.seungmoo.studyolleh.modules.study.Study;
import com.seungmoo.studyolleh.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NotificationRepository notificationRepository;
    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        // 이걸 모든 request에 담아내는 방법은?? --> Spring MVC handler Interceptor를 사용하자
        // spring MVC handler Interceptor가 제공하는 callback 메서드
        /*
        long count = notificationRepository.countByAccountAndChecked(account, false);
        model.addAttribute("hasNotification", count > 0);
        */
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model) {
        List<Study> studyList = studyRepository.findByKeyword(keyword);
        model.addAttribute(studyList);
        model.addAttribute("keyword", keyword);
        return "search";
    }
}

package com.seungmoo.studyolleh.modules.main;

import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.account.CurrentUser;
import com.seungmoo.studyolleh.modules.notification.NotificationRepository;
import com.seungmoo.studyolleh.modules.study.Study;
import com.seungmoo.studyolleh.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    // Pageable  : size, page, sort를 받을 수 있다.
    // @PageableDefault --> 이 애노테이션 붙이면 size 기본값은 10, 안붙이면 size 기본값은 20,
    @GetMapping("/search/study")
    public String searchStudy(@PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                              String keyword, Model model) {
        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage); // Collection의 경우는 이름을 주고 넘기자.. Collection의 attr 이름 없으면 무시한다.
        model.addAttribute("keyword", keyword);
        return "search";
    }
}

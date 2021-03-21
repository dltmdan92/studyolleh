package com.seungmoo.studyolleh.infra.config;

import com.seungmoo.studyolleh.modules.notification.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
//@EnableWebMvc --> 이거 쓰면 스프링부트가 제공하는 자동설정을 사용하지 않겠다는 것이다.
// WebMvcConfigurer 이거만 구현해주면, 스프링부트가 제공하는 자동설정을 사용하면서, Config에서의 추가설정도 같이 사용한다.
public class WebConfig implements WebMvcConfigurer {

    // 추가할 Interceptor를 주입 받는다.
    private final NotificationInterceptor notificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> staticResourcePath = Arrays.stream(StaticResourceLocation.values())
                .flatMap(StaticResourceLocation::getPatterns) // flatMap을 활용해서 여러개의 array들을 하나의 List로 flat하게
                .collect(Collectors.toList());
        staticResourcePath.add("/node_modules/**");

        registry.addInterceptor(notificationInterceptor)
                // 아래 exclude 안해주면 static 리소스에 대한 요청까지도 Interceptor가 적용된다.
                .excludePathPatterns(staticResourcePath);
    }
}

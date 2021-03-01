package com.seungmoo.studyolleh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    // app.host 값이 여기로 바인딩 된다.
    private String host;
}

package com.seungmoo.studyolleh.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local")
@Component
public class ConsoleEmailService implements EmailService {
    @Override
    public void send(EmailMessage emailMessage) {
        // 어떤 메시지가 보내졌는지 로깅만 한다.
        log.info("sent email: {}", emailMessage.getMessage());
    }
}

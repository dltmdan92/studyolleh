package com.seungmoo.studyolleh.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService{

    // 여기서는 application-dev.properties가 적용된 Spring-boot가 자동 셋팅해준
    // JavaMailSender Bean이 DI 된다.
    private final JavaMailSender javaMailSender;

    @Override
    public void send(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // 첨부파일 같은거 보낼때면 multipart : true로 주면 된다.
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            // TODO 일단은 html로 안하니까 html:false 처리한다.
            mimeMessageHelper.setText(emailMessage.getMessage(), false);
            javaMailSender.send(mimeMessage);
            log.info("sent emial: {}", emailMessage.getMessage());
        } catch (MessagingException e) {
            log.error("failed to send email", e);
            e.printStackTrace();
        }
    }
}

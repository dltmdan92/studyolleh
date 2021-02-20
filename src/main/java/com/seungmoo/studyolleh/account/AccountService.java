package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    /**
     * 신규 회원 처리하는 process
     * @param signUpForm
     */
    public void processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        // 이메일 확인 용 토큰 만들기
        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage = sendSignUpConfirmEmail(newAccount);
        javaMailSender.send(mailMessage);
    }

    /**
     * 회원 정보 저장
     * @param signUpForm
     * @return 저장된 회원 정보 (Jpa managed 상태)
     */
    private Account saveNewAccount(SignUpForm signUpForm) {
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
        return newAccount;
    }

    /**
     * 회원 가입 확인 용 E-mail 내용 작성
     * @param newAccount 저장된 회원 정보, 이 회원 정보를 통해 회원 가입 확인 용 E-mail을 보낸다.
     * @return mailMessage
     */
    private SimpleMailMessage sendSignUpConfirmEmail(Account newAccount) {
        // 이메일을 보내 봅시다.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 받는 사람
        mailMessage.setTo(newAccount.getEmail());
        // 제목
        mailMessage.setSubject("스터디올래, 회원 가입 인증");
        // 본문
        mailMessage.setText("/check-email-token" +
                "?token="+ newAccount.getEmailCheckToken()
                +"&email="+ newAccount.getEmail());
        return mailMessage;
    }
}

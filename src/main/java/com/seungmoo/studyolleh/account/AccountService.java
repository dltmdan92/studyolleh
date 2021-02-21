package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // 이거는 주입 받으려면 Spring-security 관련 별도 설정 필요함
    //private final AuthenticationManager authenticationManager;

    /**
     * 신규 회원 처리하는 process
     * @param signUpForm
     */
    @Transactional // 이걸 붙여줌으로써 newAccount 인스턴스는 JPA Persistent 상태가 유지 된다.
    public Account processNewAccount(SignUpForm signUpForm) {
        // @Transactional 이 안 붙어 있으면 얘는 Detached 상태!!
        Account newAccount = saveNewAccount(signUpForm);
        // 이메일 확인 용 토큰 만들기
        // 얘는 @Transactional 덕분에 여기서도 Persistent 상태 이기 떄문에 JPA의 LifeCycle 중 managed 상태이다.
        // 그러므로 generateEmailCheckToken 메서드를 사용하면! --> JPA가 DB에도 알아서 반영을 해준다!!
        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage = sendSignUpConfirmEmail(newAccount);
        javaMailSender.send(mailMessage);
        return newAccount;
    }

    /**
     * 회원 정보 저장
     * @param signUpForm
     * @return 저장된 회원 정보
     */
    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                // TODO encoding 반드시 해야함
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        // NEW 회원 정보 저장
        Account newAccount = accountRepository.save(account);
        // 여기 안에서 만큼은 newAccount는 Persistent 상태임. 그러나 이 메소드를 나가면! Detached 상태이다.
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

    /**
     * 로그인 처리
     * @param account
     */
    public void login(Account account) {
        // 이렇게 하는 건 정석은 아님, 참고
        // 그냥 SimpleGrantedAuthority까지 바로 만들어서 SecurityContextHolder에 setAuthentication 함.
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // 원래는 이렇게 하는 거임
        /*
        UsernamePasswordAuthenticationToken token1 = new UsernamePasswordAuthenticationToken(
                account.getNickname(),
                plainTextPassword // 원래 정석은 plainText로 받은 password를 써야 한다. BUT DB 저장 안하기 때문에 못쓴다.
        );
        authenticationManager.authenticate(token1);
        */
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}

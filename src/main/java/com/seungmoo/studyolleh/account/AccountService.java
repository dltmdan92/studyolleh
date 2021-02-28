package com.seungmoo.studyolleh.account;

import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.settings.form.Notifications;
import com.seungmoo.studyolleh.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 엔티티 객체의 정보 변경은 반드시!! Transaction안에서 수행해야
 * 영속성 컨텍스트에서 Persistent한 상태로 엔티티가 관리되고, DB 반영된다.
 *
 * 데이터 조회는 굳이 Transaction 안에서 할 필요는 없다.
 *
 * Repository를 사용하면 기본적으로 SimpleJpaRepository(구현체)가 Transactional 선언하므로
 * 트랜잭션 적용
 */
@Transactional
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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
        sendSignUpConfirmEmail(newAccount);
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
    public void sendSignUpConfirmEmail(Account newAccount) {
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

        javaMailSender.send(mailMessage);
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

    // readOnlu해주면 write Lock을 안씀
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {

        Account account = Optional.ofNullable(accountRepository.findByEmail(emailOrNickname))
                .orElse(accountRepository.findByNickname(emailOrNickname));
        Optional.ofNullable(account).orElseThrow(() -> new UsernameNotFoundException(emailOrNickname));
        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        // 모델 맵핑 source -> destination
        // 이렇게 하면 profile의 내용이 account 인스턴스에 반영이 된다. 아래 set Code들이 필요 없어진다.
        modelMapper.map(profile, account);
        /*account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());
        account.setProfileImage(profile.getProfileImage());*/

        // 이렇게 repository 통해서 save 실행해주면
        // 해당 Detached 상태의 account 객체를 DB에 업데이트 쳐준다.
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        // 현재 account는 Detached 상태임!
        // 반드시 패스워드 인코딩 할 것.
        account.setPassword(passwordEncoder.encode(newPassword));
        // 이렇게 명시적으로 save 해줘야 다시 persistent 상태
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        /*account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());*/
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        // 현재 account는 detached 상태이므로, 명시적으로 save를 한번 해줘야 한다.
        accountRepository.save(account);
        login(account);
    }
}

package com.seungmoo.studyolleh.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
// equals, hashCode 사용할 때 id field만 쓴다.
// 디폴트로 설정한 경우에, 순환참조로 인한 무한loop가 발생하여 stackoverflow 될 수 있다고 한다.
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    // 기본적으로 String은 varchar(255) 로 매핑된다.
    @Lob // texttype에 매핑된다 (긴 텍스트 형)
    @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    // 스터디 생성,등록,수정 이벤트를 어떻게 받을 지?? email or web
    private boolean studyCreatedByEmail = false;

    private boolean studyCreatedByWeb = true;

    private boolean studyEnrollmentResultByEmail = false;

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail = false;

    private boolean studyUpdatedByWeb = true;

    // @ManyToMany -> 실제로는 다대다 관계는 없고, 다대1, 1대다 이렇게 된다.
    // Account <-- Account_Tag --> Tag 이렇게 연관관계가 생성된다.
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime emailCheckTokenGeneratedAt;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean isManagerOf(Study study) {
        return study.getManagers().contains(this);
    }
}

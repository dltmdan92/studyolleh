package com.seungmoo.studyolleh.modules.study;

import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.account.UserAccount;
import com.seungmoo.studyolleh.modules.tag.Tag;
import com.seungmoo.studyolleh.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Study와 연결되어 있는 데이터들 Entity들을 한꺼번에 갖고올 때
// @NamedEntityGraph 통해서 Join해서 한꺼번어 갖고올 수 있다. (Eager loading)
/*@NamedEntityGraph(name = "Study.withAll",
        attributeNodes = {
            @NamedAttributeNode("tags"),
            @NamedAttributeNode("zones"),
            @NamedAttributeNode("managers"),
            @NamedAttributeNode("members"),
        }
)
@NamedEntityGraph(name = "Study.withTagsAndManagers",
        attributeNodes = {
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("managers")
        }
)
@NamedEntityGraph(name = "Study.withZonesAndManagers",
        attributeNodes = {
                @NamedAttributeNode("zones"),
                @NamedAttributeNode("managers")
        }
)
@NamedEntityGraph(name = "Study.withManagers",
        attributeNodes = {
                @NamedAttributeNode("managers")
        }
)*/
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    // mappedBy 셋팅 안해주고 그냥 이렇게 @ManyToMany하면 단방향 관계 이다.
    // 그냥 Study가 Account를 참조하는 식으로만 되어있음.
    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    // 여기서는 EAGER로 갖고 오도록 한다.
    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    // int primitive type으로 셋팅하니까 JPA에서 DDL 할 때 오류남, Wrapper 클래스로 해준다.
    private Integer memberCount;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    // Account는 Study를 참조할 수 없다 (아키텍처 상)
    // Study가 Account를 참조하도록 한다.
    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    // 공개된 스터디는 삭제하지 못한다.
    public boolean isRemovable() {
        return !this.published; // TODO 모임을 했던 스터디는 삭제할 수 없다.
    }

    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }
}

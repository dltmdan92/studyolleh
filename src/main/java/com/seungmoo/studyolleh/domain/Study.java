package com.seungmoo.studyolleh.domain;

import com.seungmoo.studyolleh.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Study와 연결되어 있는 데이터들 Entity들을 한꺼번에 갖고올 때
// @NamedEntityGraph 통해서 Join해서 한꺼번어 갖고올 수 있다. (Eager loading)
@NamedEntityGraph(name = "Study.withAll",
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

    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }
}

package com.seungmoo.studyolleh.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private Integer limitOfEnrollments;

    // mappedBy 이거를 해줘야 양방향 관계가 성립된다. (JPA가 양방향 관계라는 걸 이해한다.)
    // 이거 안해주면 조인테이블이 또 생겨버린다.
    // Enrollment에서 event Foreign key를 갖게 된다.
    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    // @Enumerated : Jpa에서 Enum타입 쓰려면 반드시 셋팅
    // 반드시 EnumType.STRING으로 해라 (Ordinal로 셋팅하면 DB에 enumtype이 순번으로 멕여짐)
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;
}

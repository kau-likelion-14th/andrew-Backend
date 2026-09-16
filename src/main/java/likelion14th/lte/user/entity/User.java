package likelion14th.lte.user.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.login.domain.RefreshToken;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.youtube.domain.SavedSong;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(unique = true)
    private String providerId;

    @Column(columnDefinition = "TEXT")
    private String s3Image;

    @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers;

    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedSong> savedSongs;


    // User.java 내부 추가/수정 사항
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "statistic_id")
    private Statistic statistic;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    @Builder
    public User(String providerId, String username, String userTag, String introduction, String profileImage) {
        this.providerId = providerId;
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
        this.profileImage = profileImage;
        // 도메인 규칙: 유저가 생성되면 통계 데이터도 자동 생성
        this.statistic = Statistic.create();
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
        this.savedSongs = new ArrayList<>();
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }
}

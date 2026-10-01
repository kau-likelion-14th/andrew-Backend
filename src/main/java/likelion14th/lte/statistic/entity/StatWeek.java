package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity; // 프로젝트의 BaseEntity 패키지 위치에 맞춰 확인
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stat_week")
public class StatWeek extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeekEnum week;

    @Column(nullable = false)
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistic_id")
    private Statistic statistic;

    @Builder
    private StatWeek(WeekEnum week, int count, Statistic statistic) {
        this.week = week;
        this.count = count;
        this.statistic = statistic;
    }

    public static StatWeek create(WeekEnum week, Statistic statistic) {
        return StatWeek.builder()
                .week(week)
                .count(0)
                .statistic(statistic)
                .build();
    }

    public void increaseCount() {
        this.count++;
    }
}
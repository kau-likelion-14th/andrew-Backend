package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "statistic")
public class Statistic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistic_id")
    private Long id;

    private int streak;

    private int monthPercent;

    @OneToMany(mappedBy = "statistic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatWeek> statWeeks = new ArrayList<>();

    // 정적 팩토리 메서드
    public static Statistic create() {
        Statistic statistic = new Statistic();
        statistic.streak = 0;
        statistic.monthPercent = 0;
        statistic.initializeWeeks();
        return statistic;
    }

    // private 초기화 메서드 (캡슐화)
    private void initializeWeeks() {
        for (WeekEnum week : WeekEnum.values()) {
            this.statWeeks.add(StatWeek.create(week, this));
        }
    }

    // 비즈니스 로직 캡슐화: 최다 완료 요일 계산
    public WeekEnum getMostTodoWeek() {
        return this.statWeeks.stream()
                .max(Comparator.comparingInt(StatWeek::getCount))
                .map(StatWeek::getWeek)
                .orElse(WeekEnum.MON);
    }

    // 비즈니스 로직 캡슐화: streak 갱신
    public void updateStreak(boolean isSuccess) {
        if (isSuccess) {
            this.streak++;
        } else {
            this.streak = 0;
        }
    }

    // 비즈니스 로직 캡슐화: 최근 30일 완료율 갱신
    public void updateMonthPercent(int monthPercent) {
        this.monthPercent = monthPercent;
    }
}
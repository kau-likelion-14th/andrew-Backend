package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.StatisticResponse;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    // 과제 1: 통계 조회 API Service
    @Transactional
    public StatisticResponse getStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        Statistic statistic = user.getStatistic();

        // 기존 유저 데이터여서 statistic이 null인 경우 방어 로직
        if (statistic == null) {
            statistic = Statistic.create();
            user.setStatistic(statistic);
            userRepository.save(user);    // CascadeType.ALL로 인해 Statistic도 같이 저장됨
        }

        return StatisticResponse.from(statistic);
    }

    // 과제 2: 단일 유저 통계 갱신 로직 (어제 하루 기준)
    @Transactional
    public void updateStatistic(User user) {
        Long userId = user.getId();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DayOfWeek yesterdayOfWeek = yesterday.getDayOfWeek();

        // 1. 어제 투두 수행 결과 분석
        boolean hasCompleted = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(userId, yesterday, true);
        boolean hasFailed = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(userId, yesterday, false);

        // 어제 완료 투두가 1개 이상 있고, 미완료 투두가 0개면 성공
        boolean isSuccess = hasCompleted && !hasFailed;

        Statistic statistic = user.getStatistic();

        // 2. streak 갱신
        statistic.updateStreak(isSuccess);

        // 3. 어제가 성공한 날이면 요일별 카운트 +1
        if (isSuccess) {
            statistic.getStatWeeks().stream()
                    .filter(statWeek -> statWeek.getWeek().toDayOfWeek() == yesterdayOfWeek)
                    .findFirst()
                    .ifPresent(StatWeek::increaseCount);
        }

        // 4. 최근 30일 완료율 계산 (어제 기준 30일 전 ~ 어제)
        LocalDate startDate = yesterday.minusDays(30);
        int completedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(userId, startDate, yesterday, true);
        int uncompletedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(userId, startDate, yesterday, false);

        int totalCount = completedCount + uncompletedCount;
        int monthPercent = 0;

        // 분모가 0이 아닌 경우에만 계산 (ArithmeticException 방지)
        if (totalCount > 0) {
            monthPercent = (completedCount * 100) / totalCount;
        }

        statistic.updateMonthPercent(monthPercent);
    }

    // 과제 3: 전체 유저 배치 갱신 (페이징 & EntityManager 메모리 관리)
    @Transactional
    public void updateAllStatistics() {
        int page = 0;
        int size = 500;
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, size));

            for (User user : userPage.getContent()) {
                updateStatistic(user);
            }

            // 영속성 컨텍스트 비우기 (대용량 메모리 관리)
            entityManager.flush();
            entityManager.clear();

            page++;
        } while (userPage.hasNext());
    }
}
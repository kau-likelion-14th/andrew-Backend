package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.statistic.entity.WeekEnum;
import likelion14th.lte.todo.entity.TodoDate;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    // 과제 1: 통계 조회 API Service
    // 투두 완료 직후에도 반영되도록 투두 기록(오늘 포함)으로 조회 시점에 계산
    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        List<TodoDate> todoDates =
                todoDateRepository.findAllByTodo_User_IdAndDateLessThanEqual(user.getId(), today);

        // 날짜별 성공 여부: 그날의 투두를 모두 완료했으면 성공
        Map<LocalDate, Boolean> successByDate = todoDates.stream()
                .collect(Collectors.toMap(TodoDate::getDate, TodoDate::isCompleted, Boolean::logicalAnd));

        return new StatisticResponse(
                calculateStreak(successByDate, today),
                calculateMonthPercent(todoDates, today),
                calculateMostTodoWeek(successByDate)
        );
    }

    // 오늘부터 거꾸로 연속 성공한 날 수. 오늘은 아직 끝나지 않았으므로 미완료여도 어제까지의 기록은 유지
    private int calculateStreak(Map<LocalDate, Boolean> successByDate, LocalDate today) {
        LocalDate date = successByDate.getOrDefault(today, false) ? today : today.minusDays(1);
        int streak = 0;
        while (successByDate.getOrDefault(date, false)) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }

    // 오늘을 포함한 최근 30일 완료율
    private int calculateMonthPercent(List<TodoDate> todoDates, LocalDate today) {
        LocalDate startDate = today.minusDays(29);
        List<TodoDate> recent = todoDates.stream()
                .filter(todoDate -> !todoDate.getDate().isBefore(startDate))
                .toList();
        if (recent.isEmpty()) {
            return 0;
        }
        long completedCount = recent.stream().filter(TodoDate::isCompleted).count();
        return (int) (completedCount * 100 / recent.size());
    }

    // 성공한 날이 가장 많은 요일. 성공한 날이 없으면 null, 같으면 앞선 요일
    private WeekEnum calculateMostTodoWeek(Map<LocalDate, Boolean> successByDate) {
        Map<WeekEnum, Long> countByWeek = successByDate.entrySet().stream()
                .filter(Map.Entry::getValue)
                .collect(Collectors.groupingBy(
                        entry -> WeekEnum.from(entry.getKey().getDayOfWeek()), Collectors.counting()));

        return Arrays.stream(WeekEnum.values())
                .filter(countByWeek::containsKey)
                .reduce((first, second) ->
                        countByWeek.get(second) > countByWeek.get(first) ? second : first)
                .orElse(null);
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

        // 통계가 없는 기존 유저 때문에 전체 배치가 실패하지 않도록 생성
        if (statistic == null) {
            statistic = Statistic.create();
            user.setStatistic(statistic);
        }

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
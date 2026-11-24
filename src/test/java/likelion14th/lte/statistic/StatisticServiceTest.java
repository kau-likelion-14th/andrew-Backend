package likelion14th.lte.statistic;

import jakarta.persistence.EntityManager;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.WeekEnum;
import likelion14th.lte.statistic.service.StatisticService;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticServiceTest {

    private final LocalDate today = LocalDate.now();

    private TodoDate todo(int daysAgo, boolean completed) {
        TodoDate todoDate = TodoDate.create(null, today.minusDays(daysAgo));
        todoDate.setCompleted(completed);
        return todoDate;
    }

    private StatisticResponse statistic(TodoDate... todoDates) {
        UserRepository userRepository = mock(UserRepository.class);
        TodoDateRepository todoDateRepository = mock(TodoDateRepository.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(todoDateRepository.findAllByTodo_User_IdAndDateLessThanEqual(any(), any()))
                .thenReturn(List.of(todoDates));
        return new StatisticService(userRepository, todoDateRepository, mock(EntityManager.class))
                .getStatistic(1L);
    }

    @Test
    void 오늘_투두를_모두_완료하면_바로_반영된다() {
        StatisticResponse result = statistic(todo(0, true), todo(0, true));
        assertEquals(1, result.getStreak());
        assertEquals(100, result.getMonthPercent());
        assertEquals(WeekEnum.from(today.getDayOfWeek()), result.getMostTodoWeek());
    }

    @Test
    void 오늘이_미완료여도_어제까지의_연속_기록은_유지된다() {
        StatisticResponse result = statistic(todo(0, true), todo(0, false), todo(1, true), todo(2, true));
        assertEquals(2, result.getStreak());
        assertEquals(75, result.getMonthPercent());
    }

    @Test
    void 중간에_실패하거나_투두가_없으면_연속이_끊긴다() {
        assertEquals(1, statistic(todo(0, true), todo(1, false), todo(2, true)).getStreak());
        assertEquals(1, statistic(todo(0, true), todo(2, true)).getStreak());
    }

    @Test
    void 최근_30일만_달성률에_포함하고_기록이_없으면_0이다() {
        assertEquals(100, statistic(todo(0, true), todo(29, true), todo(30, false)).getMonthPercent());
        StatisticResponse empty = statistic();
        assertEquals(0, empty.getStreak());
        assertEquals(0, empty.getMonthPercent());
        assertNull(empty.getMostTodoWeek());
    }

    @Test
    void 성공한_날이_가장_많은_요일을_반환한다() {
        StatisticResponse result = statistic(todo(0, true), todo(1, true), todo(8, true));
        assertEquals(WeekEnum.from(today.minusDays(1).getDayOfWeek()), result.getMostTodoWeek());
    }
}

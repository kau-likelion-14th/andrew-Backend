package likelion14th.lte.todo.repository;

import likelion14th.lte.todo.entity.TodoDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoDateRepository extends JpaRepository<TodoDate, Long> {
    // 투두랑 - 날짜로 하나 끄집어내는
    Optional<TodoDate> findByTodo_IdAndDate(Long todoId, LocalDate date);

    List<TodoDate> findAllByTodo_User_IdAndDate(Long userId, LocalDate date);

    List<TodoDate> findAllByTodo_IdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    void deleteAllByTodo_IdAndDateGreaterThanEqual(Long todoId, LocalDate date);

    List<TodoDate> findAllByTodo_User_IdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // 특정 날짜의 완료/미완료 투두 존재 여부 확인
    boolean existsByTodo_User_IdAndDateAndCompleted(Long userId, LocalDate date, boolean completed);

    // 기간 내 완료/미완료 투두 개수 조회
    int countByTodo_User_IdAndDateBetweenAndCompleted(Long userId, LocalDate start, LocalDate end, boolean completed);
}

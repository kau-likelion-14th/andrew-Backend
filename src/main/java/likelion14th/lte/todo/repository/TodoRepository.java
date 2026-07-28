package likelion14th.lte.todo.repository;


import likelion14th.lte.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 스케줄러에서 루틴인 애들을 전부 싸그리 긁어다가 TodoDate 애들 쭉 생성해주려고
    List<Todo> findAllByRoutineEnabledTrue();
}

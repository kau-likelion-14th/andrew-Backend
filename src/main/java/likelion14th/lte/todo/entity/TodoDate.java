package likelion14th.lte.todo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "todo_date",
        uniqueConstraints = @UniqueConstraint(
                name ="uk_todo_date",
                columnNames = {"todo_id", "date"}
        )
)
public class TodoDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    public static TodoDate create(Todo todo, LocalDate date) {
        TodoDate todoDate = new TodoDate();
        todoDate.todo = todo;
        todoDate.date = date;
        todoDate.completed = false;

        return todoDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }
}

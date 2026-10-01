package likelion14th.lte.follow.repository;

import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);
    Optional<Follow> findByFromUserAndToUser(User fromUser, User toUser);

    // 과제 2: 나를 타겟으로 지정한 팔로우 목록 조회
    List<Follow> findAllByToUser(User toUser);

    // 과제 3: 내가 팔로우를 요청한 목록 조회
    List<Follow> findAllByFromUser(User fromUser);
}
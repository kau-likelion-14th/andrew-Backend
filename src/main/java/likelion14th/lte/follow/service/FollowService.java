package likelion14th.lte.follow.service;

import likelion14th.lte.follow.dto.FollowUserRequest;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.dto.UserNameDto;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.follow.repository.FollowRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    private static UserNameDto getUserNameDto(String name) {
        if(!name.contains("#")){
            return new UserNameDto(name, null);
        }
        String[] parts = name.split("#", 2);
        if(parts[1].length() != 8) {
            throw new GeneralException(ErrorCode.INVALID_HANDLE_FORMAT);
        }
        return new UserNameDto(parts[0], parts[1]);
    }

    @Transactional
    public FollowUserResponse followUser(Long fromUserId, Long toUserId) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_TARGET_NOT_FOUND));

        if (fromUser.getId().equals(toUser.getId())) {
            throw new GeneralException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (followRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new GeneralException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        Follow follow = Follow.builder()
                .toUser(toUser)
                .fromUser(fromUser)
                .build();

        followRepository.save(follow);

        return FollowUserResponse.from(follow.getToUser());
    }

    @Transactional(readOnly = true)
    public Page<FollowUserResponse> searchCanFollowers(Long userId, String targetName, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        UserNameDto nameDto = getUserNameDto(targetName);
        Page<User> users;
        if(nameDto.userTag() != null && !nameDto.userTag().isEmpty()) {
            User target = userRepository.findByUserTag(nameDto.userTag())
                    .orElse(null);
            if(target == null) {
                return Page.empty(pageable);
            }
            users = new PageImpl<>(List.of(target), pageable, 1);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCase(nameDto.userName(), pageable);
        }

        List<User> canFollowUsers = users
                .getContent()
                .stream()
                .filter(target -> !target.getId().equals(userId))
                .filter(target -> !followRepository.existsByFromUserAndToUser(user, target))
                .toList();

        return new PageImpl<>(canFollowUsers, pageable, canFollowUsers.size()).map(FollowUserResponse::from);
    }

    // ================= 과제 추가 기능 =================

    // 과제 1. 언팔로우 (팔로우 취소)
    @Transactional
    public void unfollow(Long userId, Long toUserId) {
        // 1. 자기 자신 언팔로우 예외 체크
        if (userId.equals(toUserId)) {
            throw new GeneralException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED); // FOLLOW_4001 매핑
        }

        // 2. 본인(요청자) 존재 검증
        User fromUser = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND)); // USER_4041 매핑

        // 3. 대상 유저 존재 검증
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_TARGET_NOT_FOUND)); // FOLLOW_4041 매핑

        // 4. 기존 팔로우 관계 존재 여부 확인 후 삭제
        Follow follow = followRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_NOT_FOUND)); // FOLLOW_4042 매핑

        followRepository.delete(follow);
    }

    // 과제 2. 팔로워 목록 조회
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND)); // USER_4041 매핑

        // 나를 타겟으로 삼은 데이터(toUser == 나) 조회 -> 팔로우를 건 사람(fromUser)의 정보를 반환
        return followRepository.findAllByToUser(user).stream()
                .map(follow -> FollowUserResponse.from(follow.getFromUser()))
                .toList();
    }

    // 과제 3. 팔로잉 목록 조회
    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND)); // USER_4041 매핑

        // 내가 팔로우를 건 데이터(fromUser == 나) 조회 -> 팔로우를 당한 사람(toUser)의 정보를 반환
        return followRepository.findAllByFromUser(user).stream()
                .map(follow -> FollowUserResponse.from(follow.getToUser()))
                .toList();
    }

    // 과제 4. 팔로우 가능 유저 목록 전체 페이징 조회
    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getCanFollowUsers(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorCode.USER_NOT_FOUND); // USER_4041 매핑
        }

        // 실제 UserRepository에 정의된 findCanFollow 메서드명으로 올바르게 호출
        Page<User> canFollowUsersPage = userRepository.findCanFollow(userId, pageable);

        // Page 구조를 그대로 유지하면서 DTO로 변환하여 반환
        return canFollowUsersPage.map(FollowUserResponse::from);
    }
}
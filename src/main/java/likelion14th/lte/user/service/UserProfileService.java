package likelion14th.lte.user.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileService{

    // [Q5. Service 안에서 new UserRepository() 로 객체를 직접 생성하지 않고,
    // 외부에서 의존성 주입(DI)을 받는 이유는 무엇인가요? (결합도와 단위 테스트 관점)]
    // 답변:
    // 결합도 : 서비스가 구현체를 직접 new로 생성하면 해당 클래스에 강하게 결합(의존)됩니다. 외부에서 주입(DI)을 받으면 서비스는 UserRepository 인터페이스의 명세만 알면 되므로 코드가 유연해집니다.
    // 단위테스트 : 실제 데이터베이스와 연결되는 UserRepository 대신, 테스트용 가짜 객체인 모킹(Mock) 객체를 가볍게 주입할 수 있습니다.
    // 덕분에 DB를 켜지 않고도 서비스 레이어의 순수한 비즈니스 로직만 빠르게 독립적으로 테스트할 수 있습니다.
    private final UserRepository userRepository;

    // [Q6. (코딩 문제) 만약 클래스 위의 @RequiredArgsConstructor를 지운다면,
    // 우리가 직접 작성해야 할 의존성 주입용 자바 '생성자' 코드는 어떤 모습일까요? 아래에 직접 코딩해 보세요.]
    /*
        여기에 생성자 코드 작성:

        // @RequiredArgsConstructor를 지웠을 때 작성할 코드
        public UserProfileService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

    */

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse createTestUser(CreateTestUserRequest request){

        // [Q7. 일반적인 생성자 new User(name, intro, tag) 방식을 쓰지 않고,
        // User.builder()...build() 라는 '빌더 패턴'을 사용하여 객체를 조립했을 때 얻는 장점은 무엇인가요?]
        // 답변: 매개변수의 자리에 어떤 값이 들어가는지 이름(username(), introduction())을 통해 직관적으로 알 수 있습니다.
        User newUser = User.builder()
                .username(request.getUsername())
                .introduction(request.getIntroduction())
                .userTag(request.getUserTag())
                .build();

        User savedUser;
        try{
            // [Q8. 데이터를 저장하는 이 메서드 위에 @Transactional이 반드시 붙어야 하는 이유는 무엇인가요?
            // (저장 도중 DB 서버가 끊겼을 때의 상황을 가정해서 설명하세요)]
            // 답변: 만약 트랜잭션이 없다면 비정상적으로 쪼개진 데이터가 DB에 반영되어 데이터 무결성이 깨지는 대참사가 발생할 수 있습니다.
            savedUser = userRepository.save(newUser);
        } catch (Exception e){
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        return UserProfileResponse.from(savedUser);
    }
}
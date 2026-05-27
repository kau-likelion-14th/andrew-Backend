package likelion14th.lte.user.dto.response;

import likelion14th.lte.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse{
    private String userName;
    private String profileImageUrl;
    private String introduction;

    // [Q4. Controller가 DB에서 꺼낸 원본 Entity(User)를 클라이언트 화면에 그대로 반환하지 않고,
    // 굳이 from() 메서드를 통해 DTO로 한번 변환해서 내보내는 핵심적인 이유 2가지는 무엇인가요?]
    // 답변:
    // 1. 엔티티를 그대로 반환하면 비밀번호, 주민번호, 내부 시스템 ID 등 외부에 공개되어서는 안 되는 민감한 DB 스키마 구조가 그대로 API에 노출됩니다.
    // DTO를 통해 화면에 필요한 데이터만 쏙 골라내어 보낼 수 있습니다.
    // 2. 엔티티가 다른 엔티티와 연관 관계(예: @OneToMany)를 맺고 있을 경우, JSON으로 변환하는 과정에서 서로를 무한 호출하는 순환 참조(Infinite Loop) 에러가 발생할 수 있습니다.
    // 또한, DB 테이블 구조가 바뀌더라도 프론트엔드가 받는 API 포맷(DTO)은 유지할 수 있어 변화에 유연하게 대응할 수 있습니다.
    public static UserProfileResponse from(User user){
        return new UserProfileResponse(
                user.getUsername() + "#" + user.getUserTag(),
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}

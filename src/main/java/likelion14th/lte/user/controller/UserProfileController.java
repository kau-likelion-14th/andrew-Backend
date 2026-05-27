package likelion14th.lte.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileController{

    public final UserProfileService userProfileService;

    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요? (단일 책임 원칙 관점)]
    // 답변: 만약 Controller가 DB 접근까지 직접 처리하면 코드가 무거워지고 변경 사항이 생겼을 때 유지보수가 불가능해집니다.
    @GetMapping
    public ApiResponse<UserProfileResponse> getUserProfile(@RequestParam Long userId){
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PostMapping
    public ApiResponse<UserProfileResponse> createTestUser(
            // [Q10. 클라이언트가 보낸 JSON 텍스트 데이터가 어떻게 자바 객체인 CreateTestUserRequest로
            // 변환 되는지앞의 어노테이션과 연관 지어 설명해 보세요.]
            // 답변: 스프링은 내부적으로 HttpMessageConverter(기본적으로 Jackson 라이브러리의 ObjectMapper)를 작동시킵니다.
            // 이 변환기가 JSON의 Key 구조와 자바 객체의 필드명을 매핑하여 텍스트 데이터를 자바의 객체 인스턴스로 동적 변환(역직렬화, Deserialization)해 줍니다.
            @RequestBody CreateTestUserRequest request
    ){
        UserProfileResponse response = userProfileService.createTestUser(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}

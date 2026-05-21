package likelion14th.lte.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name="Test", description="테스트용")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary="헬스 체크", description="테스트용")
    @GetMapping("/health")
    public String health() { return "LikeLion 14th 화이팅! \uD83E\uDD81"; }

    @GetMapping("/item/{itemId}")
    public String getItem(
            @Parameter(description="조회할 ID", example="1")
            @PathVariable Long itemId
    ) {
        return itemId + "번";
    }

    @GetMapping("/items")
    public String searchItems(
            @RequestParam String keyward,
            @RequestParam(required = false) String category
    ) {
        return keyward + category;
    }
}

package likelion14th.lte.statistic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.statistic.dto.StatisticResponse;
import likelion14th.lte.statistic.service.StatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistic")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "통계 API", description = "유저 수행 기록 통계 조회를 담당하는 API입니다.")
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping
    @Operation(summary = "과제 1. 통계 조회 API", description = "유저의 streak, 최근 30일 완료율, 최다 투두 완료 요일을 반환합니다.")
    public ApiResponse<StatisticResponse> getStatistic(@RequestParam Long userId) {
        StatisticResponse response = statisticService.getStatistic(userId);
        return ApiResponse.onSuccess(SuccessCode.STATISTICS_GET_SUCCESS, response);
    }
}
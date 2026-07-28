package likelion14th.lte.statistic.schedule;

import likelion14th.lte.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticScheduler {

    private final StatisticService statisticService;

    // 매일 새벽 00:10:00에 통계 전체 배치 갱신 실행
    @Scheduled(cron = "0 10 0 * * *")
    public void runStatisticBatch() {
        log.info("매일 자동 통계 배치 작업 시작 (00:10)");
        statisticService.updateAllStatistics();
        log.info("매일 자동 통계 배치 작업 완료");
    }
}
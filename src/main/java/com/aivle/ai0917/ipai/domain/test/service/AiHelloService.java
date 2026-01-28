package com.aivle.ai0917.ipai.domain.test.service;

import com.aivle.ai0917.ipai.domain.test.client.AiHelloClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiHelloService {

    private final AiHelloClient aiHelloClient;

    public String checkAiServer() {
        log.info("AI 서버 연결 시도 중...");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); // 시간 측정 시작

        try {
            String result = aiHelloClient.checkConnection().block();

            stopWatch.stop(); // 시간 측정 종료
            log.info("AI 서버 응답 완료! 소요 시간: {} ms", stopWatch.getTotalTimeMillis());

            return result;
        } catch (Exception e) {
            log.error("AI 서버 연결 실패: {}", e.getMessage());
            throw e;
        }
    }
}

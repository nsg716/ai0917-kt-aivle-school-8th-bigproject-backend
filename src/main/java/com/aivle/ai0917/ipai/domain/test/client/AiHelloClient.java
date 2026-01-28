package com.aivle.ai0917.ipai.domain.test.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiHelloClient {

    private final WebClient aiWebClient;

    public Mono<String> checkConnection() {
        // 실제 호출되는 시점에 로그가 찍히도록 doOnSubscribe 활용
        return aiWebClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(subscription -> log.info("FastAPI 호출 시작..."));
    }
}

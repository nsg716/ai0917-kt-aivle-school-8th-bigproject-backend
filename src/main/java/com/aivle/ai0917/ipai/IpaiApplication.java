package com.aivle.ai0917.ipai;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication
public class IpaiApplication {
	@PostConstruct


	public void started() {
		// 이 코드가 있어야 LocalDateTime.now()가 DB와 같은 'Asia/Seoul' 기준으로 동작합니다.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
	public static void main(String[] args) {
		SpringApplication.run(IpaiApplication.class, args);
	}

}

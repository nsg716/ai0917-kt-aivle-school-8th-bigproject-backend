package com.aivle.ai0917.ipai.infra.naver.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yaml의 naver.* 설정을 읽어오는 클래스
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "naver")
public class NaverOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getRedirectUri() { return redirectUri; }

    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
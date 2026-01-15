package com.aivle.ai0917.ipai.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yaml의 jwt.* 설정 바인딩
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpMinutes;

    public String getSecret() { return secret; }
    public long getAccessTokenExpMinutes() { return accessTokenExpMinutes; }

    public void setSecret(String secret) { this.secret = secret; }
    public void setAccessTokenExpMinutes(long accessTokenExpMinutes) { this.accessTokenExpMinutes = accessTokenExpMinutes; }
}
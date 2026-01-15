package com.aivle.ai0917.ipai.infra.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 access_token 발급 응답 DTO
 */
public class NaverTokenDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public String getExpiresIn() { return expiresIn; }
    public String getError() { return error; }
    public String getErrorDescription() { return errorDescription; }
}

package com.aivle.ai0917.ipai.infra.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 사용자 정보 조회 응답 DTO
 *
 * GET https://openapi.naver.com/v1/nid/me
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfileDto {

    private String resultcode;
    private String message;

    @JsonProperty("response")
    private Profile profile;

    public String getResultcode() { return resultcode; }
    public String getMessage() { return message; }
    public Profile getProfile() { return profile; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {

        /** 네이버 고유 사용자 ID(가장 중요) */
        private String id;

        private String email;
        private String name;

        /** 성별: "M" / "F" */
        private String gender;

        /** 생일: "MM-DD" */
        private String birthday;

        /** 출생년도: "YYYY" */
        private String birthyear;

        /** 휴대전화번호 */
        private String mobile;

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getGender() { return gender; }
        public String getBirthday() { return birthday; }
        public String getBirthyear() { return birthyear; }
        public String getMobile() { return mobile; }
    }
}
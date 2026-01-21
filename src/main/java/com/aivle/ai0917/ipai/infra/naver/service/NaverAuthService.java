package com.aivle.ai0917.ipai.infra.naver.service;

import com.aivle.ai0917.ipai.infra.naver.dto.NaverLoginResultDto;
import com.aivle.ai0917.ipai.infra.naver.dto.NaverProfileDto;
import com.aivle.ai0917.ipai.infra.naver.dto.NaverTokenDto;
import com.aivle.ai0917.ipai.infra.naver.properties.NaverOAuthProperties;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * 네이버 OAuth2 로그인 처리 서비스
 *
 * 처리 흐름:
 * 1) 네이버 로그인 URL 생성(redirect)
 * 2) callback에서 code/state 받음
 * 3) code로 access_token 발급
 * 4) access_token으로 사용자 프로필 조회
 * 5) DB에 사용자 있으면 로그인 / 없으면 회원가입 후 로그인
 */
@Service
public class NaverAuthService {

    private final NaverOAuthProperties props;
    //private final UserRepository userRepository;
    private final WebClient webClient;

    public NaverAuthService(NaverOAuthProperties props, UserRepository userRepository) {
        this.props = props;
        //this.userRepository = userRepository;
        this.webClient = WebClient.builder().build();
    }

    /** 네이버 로그인 페이지로 보낼 URL 생성 */
    public LoginUrlResult buildLoginUrl() {

        // CSRF 공격 방지용 state 값 (정석은 서버에 저장해두고 callback에서 비교)
        String state = UUID.randomUUID().toString();
        String redirect = URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);

        String url =
                "https://nid.naver.com/oauth2.0/authorize"
                        + "?response_type=code"
                        + "&client_id=" + props.getClientId()
                        + "&redirect_uri=" + redirect
                        + "&state=" + state;

        return new LoginUrlResult(url, state);
    }

    /**
     * 네이버 로그인 완료 후 callback에서 호출되는 핵심 로직
     * - 사용자 정보 조회 후 DB 저장/갱신
     *//*
    public User loginOrRegister(String code, String state) {
        String accessToken = getAccessToken(code, state);
        NaverProfileDto profileResponse = getProfile(accessToken);

        var p = profileResponse.getProfile();

        // 네이버 고유 ID는 절대 변하지 않으므로 우리 서비스의 외부 식별자로 사용
        String naverId = p.getId();

        return userRepository.findByNaverId(naverId)
                .map(user -> {
                    // 이미 가입된 사용자면 최신 정보로 업데이트
                    // (네이버 동의 항목이 바뀔 수 있으므로 갱신하는 게 좋음)
                    user.setEmail(p.getEmail());
                    user.setName(p.getName());
                    user.setGender(p.getGender());
                    user.setBirthYear(p.getBirthyear());
                    user.setBirthday(p.getBirthday());
                    user.setMobile(p.getMobile());
                    user.setUpdatedAt(java.time.Instant.now());
                    return userRepository.save(user);
                })
                // NaverAuthService.java
                .orElseGet(() -> {
                    User created = User.builder()
                            .naverId(naverId)
                            .email(p.getEmail())
                            .name(p.getName())
                            .gender(p.getGender())
                            .birthYear(p.getBirthyear())
                            .birthday(p.getBirthday())
                            .mobile(p.getMobile())
                            .role("Author") // 필드 이름을 명시하므로 훨씬 안전함
                            .build();
                    return userRepository.save(created);
                });
    }*/
    /**
     * 네이버 로그인 처리 API (JSON 응답용)
     * 로직: DB 저장 전 상태를 체크하여 신규/기존 여부를 정확히 판별
     *//*
    public NaverLoginResultDto loginOrRegisterWithStatus(String code, String state) {
        String accessToken = getAccessToken(code, state);
        NaverProfileDto profileResponse = getProfile(accessToken);
        var p = profileResponse.getProfile();
        String naverId = p.getId();

        // 1. [핵심] DB에서 먼저 조회하여 '현재' 가입 여부를 확인합니다.
        Optional<User> userOptional = userRepository.findByNaverId(naverId);

        // 2. 이 시점에서 신규 회원 여부를 '확정' 짓습니다. (이후에 save를 해도 이 값은 변하지 않음)
        boolean isNewMember = userOptional.isEmpty();

        User user;
        if (isNewMember) {
            // [신규 회원] 가입에 필요한 엔티티 생성 (아직 DB 저장 전)
            user = User.builder()
                    .naverId(naverId)
                    .email(p.getEmail())
                    .name(p.getName())
                    .gender(p.getGender())
                    .birthYear(p.getBirthyear())
                    .birthday(p.getBirthday())
                    .mobile(p.getMobile())
                    .role("Author")
                    .build();
        } else {
            // [기존 회원] 기존 엔티티를 가져와서 최신 정보로 업데이트
            user = userOptional.get();
            user.setEmail(p.getEmail());
            user.setName(p.getName());
            user.setGender(p.getGender());
            user.setBirthYear(p.getBirthyear());
            user.setBirthday(p.getBirthday());
            user.setMobile(p.getMobile());
            user.setUpdatedAt(java.time.Instant.now());
        }

        // 3. 마지막에 DB에 반영 (신규면 Insert, 기존이면 Update)
        User savedUser = userRepository.save(user);

        // 4. 아까 2번 단계에서 확정해둔 isNewMember 플래그를 담아서 반환
        return new NaverLoginResultDto(savedUser, isNewMember);
    }*/

    public NaverProfileDto.Profile fetchProfile(String code, String state) {
        String accessToken = getAccessToken(code, state);
        NaverProfileDto profileResponse = getProfile(accessToken);
        return profileResponse.getProfile();
    }

    /** code로 access_token 발급 요청 */
    private String getAccessToken(String code, String state) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("code", code);
        form.add("state", state);

        NaverTokenDto tokenResponse = webClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(NaverTokenDto.class)
                .block();

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            String err = (tokenResponse == null) ? "null response" : tokenResponse.getError();
            String desc = (tokenResponse == null) ? "" : tokenResponse.getErrorDescription();
            throw new RuntimeException("네이버 토큰 발급 실패: " + err + " / " + desc);
        }

        return tokenResponse.getAccessToken();
    }

    /** access_token으로 사용자 정보 조회 */
    private NaverProfileDto getProfile(String accessToken) {

        NaverProfileDto profileResponse = webClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverProfileDto.class)
                .block();

        if (profileResponse == null || profileResponse.getProfile() == null) {
            throw new RuntimeException("네이버 프로필 조회 실패: response is null");
        }

        if (!"00".equals(profileResponse.getResultcode())) {
            throw new RuntimeException("네이버 프로필 조회 실패: " + profileResponse.getMessage());
        }

        return profileResponse;
    }

    /** 로그인 URL + state를 함께 반환할 때 사용(필요하면 확장 가능) */
    public record LoginUrlResult(String url, String state) {}
}
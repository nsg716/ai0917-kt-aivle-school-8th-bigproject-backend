package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.repository.UserRepository;
import com.aivle.ai0917.ipai.infra.naver.service.EmailVerificationService;
import com.aivle.ai0917.ipai.global.security.jwt.JwtProvider;
import com.aivle.ai0917.ipai.global.security.jwt.PendingSignupTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/signup")
public class SignupController {

    private static final String PENDING_COOKIE = "pendingSignup";
    private static final String ACCESS_COOKIE  = "accessToken";

    private final PendingSignupTokenProvider pendingTokenProvider;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    public SignupController(
            PendingSignupTokenProvider pendingTokenProvider,
            EmailVerificationService emailVerificationService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.pendingTokenProvider = pendingTokenProvider;
        this.emailVerificationService = emailVerificationService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * (A) 네이버 로그인 후 "확인 페이지"에서 보여줄 값 가져오기
     * - 프론트(/signup/naver)가 로딩될 때 호출
     * - pendingSignup 쿠키가 있어야 함(없으면 네이버 로그인 다시)
     */
    @GetMapping("/naver/pending")
    public Map<String, Object> getPendingProfile(HttpServletRequest request) {
        String token = readCookie(request, PENDING_COOKIE);
        if (token == null) {
            throw new RuntimeException("pendingSignup 쿠키가 없습니다. 네이버 로그인부터 다시 진행하세요.");
        }

        var p = pendingTokenProvider.parsePendingToken(token);

        // ✅ 이 값들을 프론트 화면 input에 채워서 보여주면 됨
        return Map.of(
                "naverId", p.naverId(),
                "name", p.name(),
                "gender", p.gender(),
                "birthday", p.birthday(),
                "birthYear", p.birthYear(),
                "mobile", p.mobile()
        );
    }

    /**
     * (B) 이메일 인증코드 발송
     * - 프론트에서 "인증 요청" 버튼 누르면 호출
     */
    @PostMapping("/email/request")
    public Map<String, Object> requestEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) throw new RuntimeException("email이 비어있습니다.");

        emailVerificationService.sendCode(email);
        return Map.of("ok", true);
    }

    /**
     * (C) 이메일 인증코드 검증
     * - 프론트에서 code 입력 후 "확인" 버튼 누르면 호출
     */
    @PostMapping("/email/verify")
    public Map<String, Object> verifyEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code  = body.get("code");
        if (email == null || email.isBlank()) throw new RuntimeException("email이 비어있습니다.");
        if (code == null || code.isBlank()) throw new RuntimeException("code가 비어있습니다.");

        boolean ok = emailVerificationService.verifyCode(email, code);
        return Map.of("ok", ok);
    }

    /**
     * (D) 가입 완료(여기서 처음으로 DB 저장)
     * - 프론트에서 "회원가입 완료" 버튼 누르면 호출
     *
     * 요청 바디: { "siteEmail": "...", "sitePwd": "..." }
     *
     * 동작:
     * 1) pendingSignup 쿠키에서 네이버 프로필 꺼냄
     * 2) 이메일 인증 완료 여부 확인
     * 3) siteEmail 중복 확인
     * 4) sitePwd BCrypt 해시
     * 5) ✅ User 저장
     * 6) accessToken 쿠키 발급(로그인 상태)
     * 7) pendingSignup 쿠키 삭제
     */
    @PostMapping("/naver/complete")
    public Map<String, Object> completeSignup(@RequestBody Map<String, String> body,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {

        String pendingToken = readCookie(request, PENDING_COOKIE);
        if (pendingToken == null) {
            throw new RuntimeException("pendingSignup 쿠키가 없습니다. 네이버 로그인부터 다시 진행하세요.");
        }

        var p = pendingTokenProvider.parsePendingToken(pendingToken);

        String siteEmail = body.get("siteEmail"); // ✅ users.siteEmail
        String sitePwd   = body.get("sitePwd");   // ✅ users.sitePwd(해시 저장)

        if (siteEmail == null || siteEmail.isBlank()) throw new RuntimeException("siteEmail이 비어있습니다.");
        if (sitePwd == null || sitePwd.isBlank()) throw new RuntimeException("sitePwd가 비어있습니다.");

        // ✅ 이메일 인증이 되어야만 가입 완료 가능
        if (!emailVerificationService.isVerified(siteEmail)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        // ✅ 이메일 중복 방지
        if (userRepository.existsBySiteEmail(siteEmail)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // ✅ 비밀번호는 절대 평문 저장 X (BCrypt 해시 저장)
        String hashedPwd = passwordEncoder.encode(sitePwd);

        User user = User.builder()
                .naverId(p.naverId())
                .name(emptyToNull(p.name()))
                .gender(emptyToNull(p.gender()))
                .birthday(emptyToNull(p.birthday()))
                .birthYear(emptyToNull(p.birthYear()))
                .mobile(emptyToNull(p.mobile()))
                .siteEmail(siteEmail)
                .sitePwd(hashedPwd)
                .role("Author")
                .build();

        User saved = userRepository.save(user);

        // ✅ 가입 완료와 동시에 로그인 처리(accessToken 쿠키 발급)
        String accessJwt = jwtProvider.createAccessToken(saved.getId(), saved.getRole());

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, accessJwt)
                .httpOnly(true)
                .secure(cookieSecure)   // 로컬 false / AWS true(HTTPS)
                .path("/")
                .sameSite(sameSite)     // 로컬 Lax / AWS(도메인 분리) None
                .maxAge(Duration.ofMinutes(60))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        // ✅ pending 쿠키는 더 이상 필요 없으니 삭제
        ResponseCookie deletePending = ResponseCookie.from(PENDING_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deletePending.toString());

        return Map.of("ok", true, "userId", saved.getId());
    }

    // --------------------
    // 공통 유틸
    // --------------------
    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String emptyToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}

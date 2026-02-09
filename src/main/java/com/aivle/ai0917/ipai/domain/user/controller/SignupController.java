package com.aivle.ai0917.ipai.domain.user.controller;

import com.aivle.ai0917.ipai.domain.admin.access.model.UserRole;
import com.aivle.ai0917.ipai.domain.user.model.User;
import com.aivle.ai0917.ipai.domain.user.service.UserService;
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
    private final UserService userService;

    // 회원가입 완료(네이버 가입)에서는 기존대로 Controller에서 encode하고 싶으면 유지 가능
    // 비밀번호 재설정은 Service에서 처리하도록 바꿨음(그래도 여기 남겨둬도 컴파일엔 문제없음)
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    public SignupController(
            PendingSignupTokenProvider pendingTokenProvider,
            EmailVerificationService emailVerificationService,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.pendingTokenProvider = pendingTokenProvider;
        this.emailVerificationService = emailVerificationService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/naver/pending")
    public Map<String, Object> getPendingProfile(HttpServletRequest request) {
        String token = readCookie(request, PENDING_COOKIE);
        if (token == null) {
            throw new RuntimeException("pendingSignup 쿠키가 없습니다. 네이버 로그인부터 다시 진행하세요.");
        }

        var p = pendingTokenProvider.parsePendingToken(token);

        return Map.of(
                "naverId", p.naverId(),
                "name", p.name(),
                "gender", p.gender(),
                "birthday", p.birthday(),
                "birthYear", p.birthYear(),
                "mobile", p.mobile()
        );
    }

    @PostMapping("/email/request")
    public Map<String, Object> requestEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) throw new RuntimeException("email이 비어있습니다.");

        emailVerificationService.sendCode(email);
        return Map.of("ok", true);
    }

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
     * (E) 비밀번호 재설정 - 인증코드 발송
     * - 모달에서 name + siteEmail 입력 후 "인증코드 보내기" 클릭
     * 요청 바디: { "name": "...", "siteEmail": "..." }
     *
     * ✅ Controller -> Service -> Repository 구조 준수
     */
    @PostMapping("/password/email/request")
    public Map<String, Object> requestPasswordResetEmailCode(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String siteEmail = body.get("siteEmail");

        if (name == null || name.isBlank()) throw new RuntimeException("name이 비어있습니다.");
        if (siteEmail == null || siteEmail.isBlank()) throw new RuntimeException("siteEmail이 비어있습니다.");

        // ✅ Service 통해 사용자 존재 확인
        boolean exists = userService.getUserByNameAndSiteEmail(name, siteEmail).isPresent();
        if (exists) {
            emailVerificationService.sendCode(siteEmail);
        }

        // ✅ 보안상 존재/미존재 구분 없이 동일 응답 권장
        return Map.of("ok", true, "message", "입력하신 이메일로 인증 코드를 발송했습니다. (계정이 존재하는 경우)");
    }

    /**
     * (F) 비밀번호 재설정 완료 - sitePwd 업데이트
     * 요청 바디: { "siteEmail": "...", "newPassword": "...", "newPasswordConfirm": "..." }
     *
     * Controller -> Service -> Repository 구조 준수
     */
    @PostMapping("/password/reset")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> body) {
        String siteEmail = body.get("siteEmail");
        String newPassword = body.get("newPassword");
        String newPasswordConfirm = body.get("newPasswordConfirm");

        if (siteEmail == null || siteEmail.isBlank()) throw new RuntimeException("siteEmail이 비어있습니다.");
        if (newPassword == null || newPassword.isBlank()) throw new RuntimeException("newPassword가 비어있습니다.");
        if (newPasswordConfirm == null || newPasswordConfirm.isBlank()) throw new RuntimeException("newPasswordConfirm가 비어있습니다.");
        if (!newPassword.equals(newPasswordConfirm)) throw new RuntimeException("비밀번호가 일치하지 않습니다.");

        // ✅ 이메일 인증이 완료되어야만 변경 가능
        if (!emailVerificationService.isVerified(siteEmail)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았거나 만료되었습니다.");
        }

        // ✅ 비밀번호 변경 로직은 Service가 책임(인코딩+저장 포함)
        userService.updateSitePassword(siteEmail, newPassword);

        // 인증 재사용 방지하려면 EmailVerificationService에 invalidate 추가 후 호출
        // emailVerificationService.invalidate(siteEmail);

        return Map.of("ok", true, "message", "비밀번호가 변경되었습니다.");
    }

    @PostMapping("/naver/complete")
    public Map<String, Object> completeSignup(@RequestBody Map<String, String> body,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {

        String pendingToken = readCookie(request, PENDING_COOKIE);
        if (pendingToken == null) {
            throw new RuntimeException("pendingSignup 쿠키가 없습니다. 네이버 로그인부터 다시 진행하세요.");
        }

        var p = pendingTokenProvider.parsePendingToken(pendingToken);

        String siteEmail = body.get("siteEmail");
        String sitePwd   = body.get("sitePwd");

        if (siteEmail == null || siteEmail.isBlank()) throw new RuntimeException("siteEmail이 비어있습니다.");
        if (sitePwd == null || sitePwd.isBlank()) throw new RuntimeException("sitePwd가 비어있습니다.");

        if (!emailVerificationService.isVerified(siteEmail)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        if (userService.existsBySiteEmail(siteEmail)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // (여기는 일단 기존대로 Controller에서 해시 후 userService.registerUser 호출 유지)
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
                .role(UserRole.Author)
                .build();

        User saved = userService.registerUser(user);

        String accessJwt = jwtProvider.createAccessToken(saved.getId(), user.getRole());

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_COOKIE, accessJwt)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(Duration.ofMinutes(60))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie deletePending = ResponseCookie.from(PENDING_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deletePending.toString());

        return Map.of("ok", true, "userId", saved.getId());
    }

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


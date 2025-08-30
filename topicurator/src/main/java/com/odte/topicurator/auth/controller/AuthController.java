// file: src/main/java/com/odte/topicurator/auth/controller/AuthController.java
package com.odte.topicurator.auth.controller;

import com.odte.topicurator.auth.application.AuthService;
import com.odte.topicurator.auth.controller.dto.*;
import com.odte.topicurator.common.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Long>>> register(@RequestBody RegisterReq req) {
        Long id = auth.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입 성공", Map.of("id", id)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenRes>> login(@RequestBody LoginReq req) {
        TokenRes tokens = auth.login(req);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRes>> refresh(@RequestBody Map<String,String> b) {
        TokenRes tokens = auth.refresh(b.get("refreshToken"));
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("인증 토큰이 없습니다."));
        }
        String token = authHeader.substring(7);
        try {
            // 내부에서 exp 확인 & 블랙리스트 등록
            auth.logout(token);
            return ResponseEntity.ok(ApiResponse.successWithNoData("로그아웃 처리 완료"));
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("만료된 토큰입니다."));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("유효하지 않은 토큰입니다."));
        }
    }
}

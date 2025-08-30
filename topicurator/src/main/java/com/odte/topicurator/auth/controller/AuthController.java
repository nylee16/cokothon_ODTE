package com.odte.topicurator.auth.controller;

import com.odte.topicurator.auth.application.AuthService;
import com.odte.topicurator.auth.controller.dto.LoginReq;
import com.odte.topicurator.auth.controller.dto.LogoutReq;
import com.odte.topicurator.auth.controller.dto.RegisterReq;
import com.odte.topicurator.auth.controller.dto.TokenRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req) {
        Long id = auth.register(req);
        return ResponseEntity.status(201).body(Map.of("id", id));
    }

    @PostMapping("/login")
    public TokenRes login(@RequestBody LoginReq req) {
        return auth.login(req);
    }

    @PostMapping("/refresh")
    public TokenRes refresh(@RequestBody Map<String,String> b) {
        return auth.refresh(b.get("refreshToken")); }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value="Authorization", required=false) String authorization,
            @RequestBody(required=false) LogoutReq body) {
        String access = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : null;
        auth.logout(access, body==null?null:body.refreshToken());
        return ResponseEntity.noContent().build();
    }
}

package com.odte.topicurator.auth.controller;

import com.odte.topicurator.auth.application.AuthService;
import com.odte.topicurator.auth.controller.dto.MeRes;
import com.odte.topicurator.auth.controller.dto.UpdateMeReq;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService auth;
    public UserController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/me")
    public MeRes me(Authentication authentication){
        Long uid = (Long) authentication.getPrincipal();
        return auth.me(uid);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> update(Authentication authentication, @RequestBody UpdateMeReq req){
        Long uid = (Long) authentication.getPrincipal();
        auth.updateMe(uid, req);
        return ResponseEntity.ok().build();
    }
}

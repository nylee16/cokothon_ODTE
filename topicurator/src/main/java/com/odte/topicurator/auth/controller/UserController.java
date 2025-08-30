// file: src/main/java/com/odte/topicurator/auth/controller/UserController.java
package com.odte.topicurator.auth.controller;

import com.odte.topicurator.auth.application.AuthService;
import com.odte.topicurator.auth.controller.dto.MeRes;
import com.odte.topicurator.auth.controller.dto.UpdateMeReq;
import com.odte.topicurator.common.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<MeRes>> me(Authentication authentication){
        Long uid = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("조회 성공", auth.me(uid)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> update(Authentication authentication, @RequestBody UpdateMeReq req){
        Long uid = (Long) authentication.getPrincipal();
        auth.updateMe(uid, req);
        return ResponseEntity.ok(ApiResponse.successWithNoData("수정 완료"));
    }
}

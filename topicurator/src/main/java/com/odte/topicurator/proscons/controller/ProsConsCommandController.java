package com.odte.topicurator.proscons.controller;

import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.proscons.application.ProsConsGenerateService;
import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import com.odte.topicurator.proscons.controller.dto.ProsConsSummarizeReq;
import com.odte.topicurator.auth.Domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProsConsCommandController {

    private final ProsConsGenerateService service;

    @Operation(summary = "뉴스 URL 요약/찬반 생성",
            description = """
                             - save=false(기본): 저장 없이 미리보기 반환(비로그인 허용)
                             - save=true : newsId 필수, 로그인 필수 → Pros/Cons 저장 후 ID 반환
                             """)
    @PostMapping("/proscons/summarize")
    public ResponseEntity<ApiResponse<ProsConsRes>> summarize(
            @Valid @RequestBody ProsConsSummarizeReq req,
            Authentication authentication // null 가능
    ) {
        Long uid = resolveUserId(authentication); // <-- 여기서 안전하게 UID 추출
        var res = service.summarize(req, uid);
        String msg = req.saveOrDefault() ? "생성 및 저장 성공" : "생성 성공(미저장)";
        return ResponseEntity.ok(ApiResponse.success(msg, res));
    }

    /** Authentication에서 안전하게 사용자 ID를 꺼내는 헬퍼 */
    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();

        // Spring Security 기본 anonymous 문자열 방지
        if (principal instanceof String s && "anonymousUser".equalsIgnoreCase(s)) {
            return null;
        }

        if (principal instanceof CustomUserDetails cud) {
            return cud.getId(); // B안: 편의 메서드 사용
        }

        // 그 외 커스텀 principal 타입을 쓰는 경우 대비 (필요 시 추가)
        return null;
    }
}

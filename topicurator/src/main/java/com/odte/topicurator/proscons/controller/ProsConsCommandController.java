package com.odte.topicurator.proscons.controller;

import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.proscons.application.ProsConsGenerateService;
import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import com.odte.topicurator.proscons.controller.dto.ProsConsSummarizeReq;
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
        Long uid = authentication != null ? (Long) authentication.getPrincipal() : null;
        var res = service.summarize(req, uid);
        String msg = req.saveOrDefault() ? "생성 및 저장 성공" : "생성 성공(미저장)";
        return ResponseEntity.ok(ApiResponse.success(msg, res));
    }
}

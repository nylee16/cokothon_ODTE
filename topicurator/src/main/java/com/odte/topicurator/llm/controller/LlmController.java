package com.odte.topicurator.llm.controller;

import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.llm.application.LlmService;
import com.odte.topicurator.llm.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService service;

    @Operation(summary = "URL 본문 추출(디버그/사전검증)")
    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<ExtractResponse>> extract(@Valid @RequestBody ExtractRequest req) {
        var res = service.extract(req);
        return ResponseEntity.ok(ApiResponse.success("추출 성공", res));
    }

    @Operation(summary = "뉴스 URL 단건 요약/Pros/Neutral/Cons + bias")
    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<SummarizeResponse>> summarize(@Valid @RequestBody SummarizeRequest req) {
        var res = service.summarize(req);
        return ResponseEntity.ok(ApiResponse.success("요약 성공", res));
    }

    @Operation(summary = "뉴스 URL 다건 요약/찬반 생성 (배치)")
    @PostMapping("/summarize-batch")
    public ResponseEntity<ApiResponse<BatchSummarizeResponse>> summarizeBatch(
            @Valid @RequestBody BatchSummarizeRequest req) {
        var res = service.summarizeBatch(req);
        return ResponseEntity.ok(ApiResponse.success("배치 요약 성공", res));
    }

    @Operation(summary = "프리폼 채팅(디버그)")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest req) {
        var res = service.chat(req);
        return ResponseEntity.ok(ApiResponse.success("응답 성공", res));
    }
}

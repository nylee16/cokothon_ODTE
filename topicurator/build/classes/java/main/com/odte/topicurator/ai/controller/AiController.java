package com.odte.topicurator.ai;

import com.odte.topicurator.ai.dto.NewsRequest;
import com.odte.topicurator.ai.dto.NewsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze-news")
    public ResponseEntity<?> analyzeNews(@RequestBody NewsRequest request) {
        List<String> links = request.getLinks();
        if (links == null || links.isEmpty()) {
            return ResponseEntity.badRequest().body("링크를 최소 1개 이상 보내야 합니다.");
        }
        if (links.size() > 5) {
            return ResponseEntity.badRequest().body("최대 5개의 링크만 가능합니다.");
        }

        List<NewsResponse> responses = new ArrayList<>();

        for (String link : links) {
            try {
                NewsResponse res = geminiService.analyzeNews(link);
                responses.add(res);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Gemini API 호출 실패: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(responses);
    }
}

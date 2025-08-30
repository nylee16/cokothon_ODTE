package com.odte.topicurator.llm.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odte.topicurator.llm.config.LlmClient;
import com.odte.topicurator.llm.config.LlmProperties;
import com.odte.topicurator.llm.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmClient llm;
    private final LlmProperties props;
    private final ObjectMapper om = new ObjectMapper();

    /* -------------------- 외부 공개 메서드 -------------------- */

    public ExtractResponse extract(ExtractRequest req) {
        var doc = fetch(req.url());
        return new ExtractResponse(extractTitle(doc), extractMainText(doc));
    }

    /** 단건 요약 (URL을 LLM이 직접 읽는 프롬프트) */
    public SummarizeResponse summarize(SummarizeRequest req) {
        String prompt = promptForSingle(req.languageOrDefault(), req.url());
        String raw = llm.chat(props.getModel(), List.of(new ChatMessage("user", prompt)), 0.2, 900);
        log.debug("[LLM] single raw: {}", raw);
        var j = safeJson(raw);
        return mapOneFromLooseJson(j, req.url(), null);
    }

    /** 다건 요약 (여러 URL을 한 번에) */
    public BatchSummarizeResponse summarizeBatch(BatchSummarizeRequest req) {
        final int chunk = 8; // 토큰 초과 방지
        List<SummarizeResponse> all = new ArrayList<>();
        for (int i=0;i<req.urls().size();i+=chunk) {
            var slice = req.urls().subList(i, Math.min(req.urls().size(), i+chunk));
            String prompt = promptForBatch(req.languageOrDefault(), slice);
            String raw = llm.chat(props.getModel(), List.of(new ChatMessage("user", prompt)), 0.2, 2500);
            log.debug("[LLM] batch raw: {}", raw);
            var j = safeJson(raw);

            if (j.isArray()) {
                for (var n : j) all.add(mapOneFromLooseJson(n, n.path("url").asText(null), null));
            } else if (j.has("items") && j.get("items").isArray()) {
                for (var n : j.get("items")) all.add(mapOneFromLooseJson(n, n.path("url").asText(null), null));
            } else {
                all.add(mapOneFromLooseJson(j, j.path("url").asText(null), null));
            }
        }
        return new BatchSummarizeResponse(all);
    }

    /** 디버그용 프리폼 채팅 */
    public ChatResponse chat(ChatRequest req) {
        String out = llm.chat(props.getModel(), req.messages(), req.temperature(), req.maxTokens());
        return new ChatResponse(out);
    }

    /* -------------------- Prompt 템플릿 -------------------- */

    private String promptForSingle(String lang, String url){
        return """
        아래 뉴스 URL을 읽고 JSON으로만 출력해.
        필드:
        - "url": 기사 원본 URL
        - "summary": 기사 요약 (약 200자)
        - "pro": 찬성 의견 (약 200자)
        - "con": 반대 의견 (약 200자)
        - "neutrality": 중립성/신뢰도(0~100)
        - "bias": -100(강한 진보) ~ +100(강한 보수)의 정수
        **반드시** 설명문/코드블록 없이 JSON만 출력.

        URL: %s
        """.formatted(url);
    }

    private String promptForBatch(String lang, List<String> urls){
        StringBuilder sb = new StringBuilder();
        sb.append("""
        아래 여러 뉴스 URL을 각각 읽고, 결과를 JSON 배열로만 출력해.
        각 원소는 다음 필드로 구성:
        { "url":"...", "summary":"...", "pro":"...", "con":"...", "neutrality":0-100, "bias":-100..100 }
        설명문/코드블록 없이 배열(JSON)만 출력.
        """);
        for (int i=0;i<urls.size();i++){
            sb.append("%d) %s\n".formatted(i+1, urls.get(i)));
        }
        return sb.toString();
    }

    /* -------------------- JSON 매핑/정규화 -------------------- */

    private SummarizeResponse mapOneFromLooseJson(JsonNode j, String fallbackUrl, String title){
        String url = j.hasNonNull("url") ? j.get("url").asText() : fallbackUrl;
        String summary = pickText(j, "summary");
        String pros    = pickText(j, "pros", "pro");
        String cons    = pickText(j, "cons", "con");
        String neutral = pickText(j, "neutral");

        // neutrality/ confidence 등 다양한 키 허용 + 문자열도 정수로 파싱
        Integer neutrality = pickInt(j, null, "neutrality", "confidence");
        int bias = clampInt(pickInt(j, 0, "bias"), -100, 100);

        return new SummarizeResponse(
                title, url, summary, pros, neutral, cons,
                bias, j.hasNonNull("biasReason") ? j.get("biasReason").asText() : null,
                (neutrality != null ? clampInt(neutrality, 0, 100) : null)
        );
    }

    private String pickText(JsonNode j, String... keys){
        for (var k: keys) if (j.hasNonNull(k)) return j.get(k).asText("");
        return "";
    }
    private Integer pickInt(JsonNode j, Integer def, String... keys){
        for (var k: keys){
            if (j.has(k) && !j.get(k).isNull()){
                var n = j.get(k);
                if (n.isNumber()) return n.asInt();
                try { return Integer.parseInt(n.asText().trim()); } catch (Exception ignored) {}
            }
        }
        return def;
    }

    private JsonNode safeJson(String content) {
        try {
            String c = content == null ? "" : content.trim();
            if (c.startsWith("```")) {
                c = c.replaceAll("^```(json)?", "")
                        .replaceAll("```$", "").trim();
            }
            return om.readTree(c);
        } catch (Exception e) {
            // 실패 시 최소 스키마로 보정
            return om.createObjectNode()
                    .put("summary", content == null ? "" : content)
                    .put("pros", "")
                    .put("neutral", "")
                    .put("cons", "")
                    .put("bias", 0);
        }
    }

    /* -------------------- HTML 추출 유틸 -------------------- */

    public ExtractResponse quickExtract(String url) { // 필요 시 외부에서 재사용
        var d = fetch(url);
        return new ExtractResponse(extractTitle(d), extractMainText(d));
    }

    private org.jsoup.nodes.Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Topicurator/1.0)")
                    .timeout((int) Duration.ofMillis(props.getTimeoutMs()).toMillis())
                    .get();
        } catch (Exception e) {
            throw new IllegalArgumentException("URL을 읽는 데 실패했습니다: " + e.getMessage());
        }
    }

    private String extractTitle(Document doc) {
        String t = doc.title();
        if (t != null && !t.isBlank()) return t;
        var og = doc.selectFirst("meta[property=og:title]");
        return og != null ? og.attr("content") : "";
    }

    private String extractMainText(Document doc) {
        var sb = new StringBuilder();
        // 아주 단순한 추출: 길이가 어느정도 되는 텍스트만 모음
        doc.select("article,section,p,div").stream().limit(400).forEach(el -> {
            String line = el.text();
            if (line != null && line.length() > 40) {
                sb.append(line).append('\n');
            }
        });
        return sb.toString();
    }

    private int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}

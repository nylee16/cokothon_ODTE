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

    /** 단건 요약 (정제 텍스트 기반) */
    public SummarizeResponse summarize(SummarizeRequest req) {
        // 1) 본문 추출 & 정제
        var doc = fetch(req.url());
        var title = extractTitle(doc);
        var clean = cleanMainText(extractMainText(doc), req.url());

        // 2) LLM 프롬프트(JSON 강제)
        String prompt = promptForSingleFromText(req.languageOrDefault(), req.url(), title, clean);

        // 3) 호출 & 파싱
        String raw = llm.chat(props.getModel(), List.of(new ChatMessage("user", prompt)), 0.2, 900);
        log.debug("[LLM] single(raw): {}", raw);
        var j = safeJson(raw);

        // 4) 매핑 + 검증(키워드 교차 확인 후 필요 시 1회 재시도)
        var out = mapOneFromLooseJson(j, req.url(), title);
        var checked = validateAndMaybeReprompt(out, clean, () -> {
            String retry = llm.chat(
                    props.getModel(),
                    List.of(new ChatMessage("user",
                            promptForSingleFromText_strict(req.languageOrDefault(), req.url(), title, clean))),
                    0.2,
                    900
            );
            return mapOneFromLooseJson(safeJson(retry), req.url(), title);
        });

        return checked;
    }

    /** 다건 요약 (URL별 격리 호출로 섞임 방지) */
    public BatchSummarizeResponse summarizeBatch(BatchSummarizeRequest req) {
        List<SummarizeResponse> all = new ArrayList<>(req.urls().size());
        for (String url : req.urls()) {
            try {
                var single = summarize(new SummarizeRequest(url, req.language()));
                all.add(single);
            } catch (Exception e) {
                log.warn("summarize fail for {}: {}", url, e.toString());
                all.add(new SummarizeResponse(
                        /*title*/ null, url,
                        /*summary*/ "요약에 실패했습니다.",
                        /*pros*/ "", /*neutral*/ "", /*cons*/ "",
                        /*bias*/ 0, /*biasReason*/ null, /*confidence*/ 0
                ));
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
    // (참고용: 기존 URL-직접읽기 프롬프트. 현재 경로에서는 사용하지 않지만 유지)
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

    // (참고용: 기존 배치 프롬프트. 현재는 URL별 격리 호출로 대체)
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

    /** 텍스트 기반 단건 요약 프롬프트 */
    private String promptForSingleFromText(String lang, String url, String title, String cleanText) {
        return """
        너는 뉴스 요약기다. 아래 '본문'만 사용해 결과를 **JSON**으로만 출력하라.
        필드:
        - "url": 원본 URL
        - "title": 제목(가능하면 제공 제목 사용, 없으면 본문으로 생성)
        - "summary": 3~5문장 핵심 요약
        - "pros": 정책/조치의 기대효과(2문장 이상)
        - "neutral": 날짜·수치·당사자 발언 등 사실 맥락(2문장 이상)
        - "cons": 리스크/반대 논리(2문장 이상)
        - "bias": -10(매우 비판)~+10(매우 찬성) 정수
        - "biasReason": 위 점수의 근거 1~2문장
        - "confidence": 0~100 (요약-본문 일치감)

        제약:
        - URL 외 자료 검색이나 추정 금지. 본문에 없는 내용 추가 금지.
        - 반드시 **설명문/코드블록 없이 JSON만** 출력.

        URL: %s
        제공제목: %s

        본문:
        %s
        """.formatted(url, nullToEmpty(title), trimForTokens(cleanText, 8000));
    }

    /** 텍스트 기반 단건 요약 프롬프트(엄격 모드, 재시도용) */
    private String promptForSingleFromText_strict(String lang, String url, String title, String cleanText) {
        return """
        위와 동일한 규격으로 JSON만 출력하되, 다음 추가 제약을 지켜라.
        - "summary": 정확히 4문장
        - "pros","neutral","cons": 각각 최소 2문장
        - 본문 키워드(주요 인물/기관/지명/기업) 명시적 포함

        URL: %s
        제공제목: %s
        본문:
        %s
        """.formatted(url, nullToEmpty(title), trimForTokens(cleanText, 8000));
    }

    /* -------------------- JSON 매핑/정규화 -------------------- */

    private SummarizeResponse mapOneFromLooseJson(JsonNode j, String fallbackUrl, String title){
        String url = j.hasNonNull("url") ? j.get("url").asText() : fallbackUrl;

        String outTitle = pickText(j, "title");
        if (outTitle.isBlank()) outTitle = title; // 제공 제목 보정

        String summary = pickText(j, "summary");
        String pros    = pickText(j, "pros", "pro");
        String cons    = pickText(j, "cons", "con");
        String neutral = pickText(j, "neutral", "neutrality_text"); // 추가 키도 허용

        // neutrality / confidence 정수 파싱
        Integer neutrality = pickInt(j, null, "neutrality", "confidence");

        // bias 스케일 보정: -100..100, -10..10 모두 수용 → -10..10으로 정규화
        Integer biasRaw = pickInt(j, 0, "bias");
        int bias = 0;
        if (biasRaw != null){
            if (Math.abs(biasRaw) > 10) bias = clampInt(Math.round(biasRaw / 10f), -10, 10);
            else bias = clampInt(biasRaw, -10, 10);
        }

        String biasReason = j.hasNonNull("biasReason") ? j.get("biasReason").asText() : null;

        return new SummarizeResponse(
                outTitle, url, summary, pros, neutral, cons,
                bias, biasReason,
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

    /* -------------------- HTML 추출/정제 유틸 -------------------- */

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
        // og:title > twitter:title > <title>
        var og = doc.selectFirst("meta[property=og:title]");
        if (og != null && !og.attr("content").isBlank()) return og.attr("content").trim();
        var tw = doc.selectFirst("meta[name=twitter:title]");
        if (tw != null && !tw.attr("content").isBlank()) return tw.attr("content").trim();
        String t = doc.title();
        return t == null ? "" : t.trim();
    }

    private String extractMainText(Document doc) {
        // 우선순위: article > main > 네이버/언론사 공통 셀렉터
        var article = doc.selectFirst("article");
        if (article != null) return article.text();

        var main = doc.selectFirst("main");
        if (main != null) return main.text();

        var news = doc.select("#newsct_article, #articeBody, .newsct_article, .article_body").first();
        if (news != null) return news.text();

        // 폴백: 과도한 div/p 스캔을 피하고 p만 제한
        var sb = new StringBuilder();
        doc.select("p").stream().limit(200).forEach(el -> {
            String line = el.text();
            if (line != null && line.length() > 30) sb.append(line).append('\n');
        });
        return sb.toString();
    }

    /** 네이버/YTN 등 공통 노이즈 제거 + 길이 제한 */
    private String cleanMainText(String raw, String url){
        if (raw == null) return "";
        String t = raw;

        // 반복/랭킹/댓글/저작권/추천기사/편집 노트/구독 유도 등 제거
        t = t.replaceAll("(?m)^\\s*\\[앵커\\].*$", "");
        t = t.replaceAll("(?m)^\\s*\\[기자\\].*$", "");
        t = t.replaceAll("(?i)Copyright.*All rights reserved.*", "");
        t = t.replaceAll("(?m)^\\s*이 기사를 추천.*$", "");
        t = t.replaceAll("(?m)^\\s*댓글.*$","");
        t = t.replaceAll("(?m)^\\s*네이버 AI 뉴스.*$", "");
        t = t.replaceAll("(?m)^\\s*YTN LIVE.*$", "");

        // 지나치게 짧은 줄 제거 & 공백 정리
        var sb = new StringBuilder();
        for (String line : t.split("\\R")) {
            String s = line.trim();
            if (s.length() >= 40 && !isMostlyNoise(s)) sb.append(s).append('\n');
        }
        String cleaned = sb.toString().trim();

        // 길이 제한(토큰 세이프티)
        return trimForTokens(cleaned, 9000);
    }

    private boolean isMostlyNoise(String s){
        // 메뉴/랭킹/구독/광고/연관기사 등 흔한 패턴
        return s.matches(".*(구독|랭킹|헤드라인|관련 기사|추천|댓글 정책|YTN LIVE|네이버).*");
    }

    private String trimForTokens(String s, int maxChars){
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, maxChars);
    }

    /* -------------------- 결과 검증/가드레일 -------------------- */

    /** 요약 결과 검증. 키워드 겹침이 너무 낮으면 한 번 재프롬프트 */
    private SummarizeResponse validateAndMaybeReprompt(
            SummarizeResponse out,
            String cleanText,
            java.util.concurrent.Callable<SummarizeResponse> retryFn
    ) {
        if (out == null) return null;

        // 필수 필드 보정
        String sum = nullToEmpty(out.summary());
        String pros = nullToEmpty(out.pros());
        String cons = nullToEmpty(out.cons());
        String neu  = nullToEmpty(out.neutral());

        // 키워드 추출(아주 단순 버전)
        var kws = topKeywords(cleanText, 12);
        int overlap = overlapCount(sum + " " + pros + " " + cons + " " + neu, kws);

        boolean ok = !sum.isBlank() && !neu.isBlank() && overlap >= 3;
        if (ok) return out;

        try {
            var retried = retryFn.call();
            return retried != null ? retried : out;
        } catch (Exception e) {
            log.warn("retry failed: {}", e.toString());
            return out;
        }
    }

    private List<String> topKeywords(String text, int limit){
        var out = new ArrayList<String>();
        if (text == null) return out;
        for (String w : text.split("[^\\p{L}\\p{N}_-]+")) {
            String s = w.trim();
            if (s.length() >= 2 && s.length() <= 30) {
                // 기업/지명/인물 후보 위주 간단 필터
                if (Character.isUpperCase(s.codePointAt(0)) ||
                        s.matches(".*(전자|하이닉스|중국|미국|시안|우시|다롄|장비|허가|반도체|생산|공장|제재).*")) {
                    out.add(s);
                }
            }
            if (out.size() >= limit) break;
        }
        return out;
    }

    private int overlapCount(String text, List<String> kws){
        int c = 0;
        String lower = text == null ? "" : text.toLowerCase();
        for (String k : kws) if (!k.isBlank() && lower.contains(k.toLowerCase())) c++;
        return c;
    }

    private static String nullToEmpty(String s){ return s == null ? "" : s; }

    /* -------------------- 공용 유틸 -------------------- */

    private int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}

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
        var doc   = fetch(req.url());
        var title = extractTitle(doc);
        var clean = cleanMainText(extractMainText(doc), req.url());

        // 2) LLM 프롬프트(JSON 강제)
        String prompt = promptForSingleFromText(req.languageOrDefault(), req.url(), title, clean);

        // 3) 호출 & 파싱(+언랩)
        String raw = llm.chat(props.getModel(), List.of(new ChatMessage("user", prompt)), 0.2, 900);
        log.debug("[LLM] single(raw): {}", raw);
        var j = unwrap(safeJson(raw));

        // 4) 매핑
        var out = mapOneFromLooseJson(j, req.url(), title);

        // 5) 검증/보정: 요약-본문/제목 품질 체크 → 재시도 → 폴백
        out = fixIfBad(out, title, clean, () -> {
            String retry = llm.chat(
                    props.getModel(),
                    List.of(new ChatMessage("user",
                            promptForSingleFromText_strict(req.languageOrDefault(), req.url(), title, clean))),
                    0.2,
                    900
            );
            return mapOneFromLooseJson(unwrap(safeJson(retry)), req.url(), title);
        });

        // 6) (선택) 키워드 기반 추가 검증 후, 필요 시 요약만 리페어
        if (isBadSummary(out.summary(), title)) {
            try {
                String repair = llm.chat(
                        props.getModel(),
                        List.of(new ChatMessage("user",
                                promptRepairSummary(req.languageOrDefault(), req.url(), title, clean))),
                        0.2,
                        400
                );
                var rj = unwrap(safeJson(repair));
                String repaired = pickText(rj, "summary", "요약");
                if (!isBadSummary(repaired, title)) {
                    out = new SummarizeResponse(
                            out.title(), out.link(),
                            repaired, out.pros(), out.neutral(), out.cons(),
                            out.bias(), out.biasReason(), out.confidence()
                    );
                }
            } catch (Exception e) {
                log.warn("summary repair failed: {}", e.toString());
            }
        }

        // 7) 그래도 안되면 추출적 폴백
        if (isBadSummary(out.summary(), title)) {
            String fb = extractiveFallbackSummary(clean, title, 3, 320);
            out = new SummarizeResponse(
                    out.title(), out.link(),
                    fb, out.pros(), out.neutral(), out.cons(),
                    out.bias(), out.biasReason(), out.confidence()
            );
        }

        return out;
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

    /** 텍스트 기반 단건 요약 프롬프트(객체만 허용) */
    private String promptForSingleFromText(String lang, String url, String title, String cleanText) {
        return """
        너는 뉴스 요약기다. 아래 '본문'만 사용해 결과를 **JSON**으로만 출력하라.
        반드시 **단일 JSON 객체**로 출력하고 **배열은 절대 사용하지 말라.**
        필수 필드:
        - "url": 원본 URL
        - "title": 제목(가능하면 제공 제목 사용, 없으면 본문으로 생성)
        - "summary": 3~5문장, 200~450자. **제목 문구를 그대로 반복 금지**, 다른 표현으로 핵심을 압축.
        - "pros": 기대효과(2문장 이상)
        - "neutral": 사실 맥락(2문장 이상, 수치/시점/주체 포함)
        - "cons": 리스크(2문장 이상)
        - "bias": -10(매우 비판)~+10(매우 찬성) 정수
        - "biasReason": 근거 1~2문장
        - "confidence": 0~100 (요약-본문 일치감)

        제약:
        - URL 외 자료 검색/추정 금지. 본문에 없는 내용 추가 금지.
        - **설명문/코드블록 없이 JSON만** 출력.
        - "summary"는 "title"과 동일한 텍스트를 사용하지 말 것.

        URL: %s
        제공제목: %s

        본문:
        %s
        """.formatted(url, nullToEmpty(title), trimForTokens(cleanText, 8000));
    }

    /** 엄격 모드(재시도용) */
    private String promptForSingleFromText_strict(String lang, String url, String title, String cleanText) {
        return """
        위와 동일 규격으로 JSON만 출력.
        추가 제약:
        - "summary": 정확히 4문장, 220~420자. **제목과 동일/부분복사 금지**(동일 구절을 다른 표현으로 바꿔라).
        - "pros","neutral","cons": 각각 최소 2문장.
        - 본문 주요 키워드(인물/기관/지명/기업) 최소 3개 포함.
        - **단일 JSON 객체(배열 금지)**

        URL: %s
        제공제목: %s
        본문:
        %s
        """.formatted(url, nullToEmpty(title), trimForTokens(cleanText, 8000));
    }

    /** summary만 재생성(리페어) */
    private String promptRepairSummary(String lang, String url, String title, String cleanText){
        return """
        아래 본문만 사용해 "summary"만 다시 작성하라. **JSON 객체**로 {"summary":"..."} 만 출력.
        요구사항:
        - 3~5문장, 220~420자
        - **제목 문구와 동일/부분복사 금지**, 의미는 유지하되 표현은 바꿀 것
        - 날짜/주체/핵심 조치/영향 순으로 논리적으로 요약

        제목: %s
        URL: %s

        본문:
        %s
        """.formatted(nullToEmpty(title), url, trimForTokens(cleanText, 6000));
    }

    /* -------------------- JSON 매핑/정규화 -------------------- */

    private SummarizeResponse mapOneFromLooseJson(JsonNode j, String fallbackUrl, String providedTitle){
        String url = pickText(j, "url", "link", "source", "원본", "URL");
        if (url.isBlank()) url = fallbackUrl;

        String outTitle = pickText(j, "title", "제목", "headline");
        if (outTitle.isBlank()) outTitle = providedTitle;

        String summary = pickText(j, "summary", "요약");
        String pros    = pickText(j, "pros", "pro", "찬성", "장점", "기대효과");
        String cons    = pickText(j, "cons", "con", "반대", "단점", "우려", "리스크");
        String neutral = pickText(j, "neutral", "neutral_text", "neutrality_text", "중립", "사실", "배경");

        Integer neutrality = pickInt(j, null, "neutrality", "confidence", "신뢰도");
        Integer biasRaw    = pickInt(j, 0, "bias", "편향");

        int bias = 0;
        if (biasRaw != null){
            bias = Math.abs(biasRaw) > 10 ? clampInt(Math.round(biasRaw / 10f), -10, 10)
                    : clampInt(biasRaw, -10, 10);
        }

        String biasReason = pickText(j, "biasReason", "bias_reason", "근거", "근거설명");
        Integer conf      = (neutrality != null ? clampInt(neutrality, 0, 100) : null);

        return new SummarizeResponse(outTitle, url, summary, pros, neutral, cons, bias, biasReason, conf);
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

    /** 결과 루트가 배열/래퍼일 때 첫 객체로 언랩 */
    private JsonNode unwrap(JsonNode j) {
        if (j == null) return om.createObjectNode();
        if (j.isArray()) return j.size() > 0 ? j.get(0) : om.createObjectNode();
        if (j.has("item")) return j.get("item");
        if (j.has("result")) return j.get("result");
        if (j.has("data")) return j.get("data");
        if (j.has("items") && j.get("items").isArray() && j.get("items").size() > 0) return j.get("items").get(0);
        return j;
    }

    private JsonNode safeJson(String content) {
        try {
            String c = content == null ? "" : content.trim();
            if (c.startsWith("```")) {
                c = c.replaceAll("^```(json)?", "")
                        .replaceAll("```$", "").trim();
            }
            return om.readTree(c);
        } catch (Exception e1) {
            try {
                String c = content == null ? "" : content;
                int sObj = c.indexOf('{'); int eObj = c.lastIndexOf('}');
                int sArr = c.indexOf('['); int eArr = c.lastIndexOf(']');
                String cand = null;
                if (sObj >= 0 && eObj > sObj) cand = c.substring(sObj, eObj + 1);
                else if (sArr >= 0 && eArr > sArr) cand = c.substring(sArr, eArr + 1);
                if (cand != null) return om.readTree(cand);
            } catch (Exception ignored) {}
            return om.createObjectNode()
                    .put("summary", content == null ? "" : content)
                    .put("pros", "")
                    .put("neutral", "")
                    .put("cons", "")
                    .put("bias", 0);
        }
    }

    /* -------------------- 품질 검증/보정 -------------------- */

    private SummarizeResponse fixIfBad(
            SummarizeResponse out,
            String title,
            String cleanText,
            java.util.concurrent.Callable<SummarizeResponse> retryFn
    ) {
        if (out == null) return null;

        String sum = nullToEmpty(out.summary());
        String neu = nullToEmpty(out.neutral());

        // 키워드 간단 스코어
        var kws = topKeywords(cleanText, 12);
        int overlap = overlapCount(sum + " " + neu, kws);

        boolean badSummary = isBadSummary(sum, title);
        boolean weakAlign  = overlap < 3;

        if (!badSummary && !weakAlign && !neu.isBlank()) return out;

        try {
            var retried = retryFn.call();
            if (retried != null && !isBadSummary(retried.summary(), title)) return retried;
        } catch (Exception e) {
            log.warn("retry failed: {}", e.toString());
        }
        return out;
    }

    private boolean isBadSummary(String summary, String title){
        String s = nullToEmpty(summary).replaceAll("\\s+", " ").trim();
        String t = nullToEmpty(title).replaceAll("\\s+", " ").trim();
        if (s.isBlank()) return true;
        if (s.equalsIgnoreCase(t)) return true;            // 완전 동일
        if (s.length() < 60) return true;                  // 너무 짧음(경험값)
        // 제목을 그대로 포함(선두/말미)하는 단순 복붙 방지
        if (!t.isBlank() && (s.startsWith(t) || s.endsWith(t))) return true;
        return false;
    }

    /** 추출적 폴백: 본문 첫 2~3문장 합치기, 길이 제한 */
    private String extractiveFallbackSummary(String clean, String title, int maxSentences, int maxChars){
        if (clean == null || clean.isBlank()) return nullToEmpty(title);
        String[] sents = clean.split("(?<=[.!?…]|[\\.\\?!]”|\\.”)\\s+");
        var sb = new StringBuilder();
        int used = 0;
        for (String s : sents){
            String line = s.trim();
            if (line.length() < 25) continue;
            sb.append(line).append(' ');
            used++;
            if (used >= maxSentences) break;
            if (sb.length() >= maxChars) break;
        }
        String out = sb.toString().trim();
        if (out.isBlank()) out = title == null ? "" : title;
        if (out.length() > maxChars) out = out.substring(0, maxChars).trim();
        return out;
    }

    private List<String> topKeywords(String text, int limit){
        var out = new ArrayList<String>();
        if (text == null) return out;
        for (String w : text.split("[^\\p{L}\\p{N}_-]+")) {
            String s = w.trim();
            if (s.length() >= 2 && s.length() <= 30) {
                if (Character.isUpperCase(s.codePointAt(0)) ||
                        s.matches(".*(전자|하이닉스|중국|미국|시안|우시|다롄|장비|허가|반도체|생산|공장|제재|정부|기업|생산차질).*")) {
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

    /* -------------------- HTML 추출/정제 -------------------- */

    public ExtractResponse quickExtract(String url) {
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
        var og = doc.selectFirst("meta[property=og:title]");
        if (og != null && !og.attr("content").isBlank()) return og.attr("content").trim();
        var tw = doc.selectFirst("meta[name=twitter:title]");
        if (tw != null && !tw.attr("content").isBlank()) return tw.attr("content").trim();
        String t = doc.title();
        return t == null ? "" : t.trim();
    }

    private String extractMainText(Document doc) {
        var article = doc.selectFirst("article");
        if (article != null) return article.text();

        var main = doc.selectFirst("main");
        if (main != null) return main.text();

        var news = doc.select("#newsct_article, #articeBody, .newsct_article, .article_body").first();
        if (news != null) return news.text();

        var sb = new StringBuilder();
        doc.select("p").stream().limit(200).forEach(el -> {
            String line = el.text();
            if (line != null && line.length() > 30) sb.append(line).append('\n');
        });
        return sb.toString();
    }

    private String cleanMainText(String raw, String url){
        if (raw == null) return "";
        String t = raw;
        t = t.replaceAll("(?m)^\\s*\\[앵커\\].*$", "");
        t = t.replaceAll("(?m)^\\s*\\[기자\\].*$", "");
        t = t.replaceAll("(?i)Copyright.*All rights reserved.*", "");
        t = t.replaceAll("(?m)^\\s*이 기사를 추천.*$", "");
        t = t.replaceAll("(?m)^\\s*댓글.*$","");
        t = t.replaceAll("(?m)^\\s*네이버 AI 뉴스.*$", "");
        t = t.replaceAll("(?m)^\\s*YTN LIVE.*$", "");

        var sb = new StringBuilder();
        for (String line : t.split("\\R")) {
            String s = line.trim();
            if (s.length() >= 40 && !isMostlyNoise(s)) sb.append(s).append('\n');
        }
        String cleaned = sb.toString().trim();
        return trimForTokens(cleaned, 9000);
    }

    private boolean isMostlyNoise(String s){
        return s.matches(".*(구독|랭킹|헤드라인|관련 기사|추천|댓글 정책|YTN LIVE|네이버).*");
    }

    private String trimForTokens(String s, int maxChars){
        if (s == null) return "";
        if (s.length() <= maxChars) return s;
        return s.substring(0, maxChars);
    }

    /* -------------------- 공용 유틸 -------------------- */

    private int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
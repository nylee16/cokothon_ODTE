package com.odte.topicurator.proscons.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"default","dev"})
public class DummyLlmSummarizer implements LlmSummarizer {
    @Override
    public Result summarizeFromUrl(String url) {
        log.info("[LLM-DUMMY] summarize {}", url);
        return new Result(
                "제목(더미)",
                "핵심 요약(더미)",
                "찬성 포인트(더미)",
                "중립 관점(더미)",
                "반대 포인트(더미)",
                Short.valueOf((short) 10)
        );
    }
}

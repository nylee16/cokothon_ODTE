package com.odte.topicurator.proscons.infrastructure;

public interface LlmSummarizer {
    record Result(String title, String summary, String pros, String neutral, String cons, int bias) {}
    Result summarizeFromUrl(String url);
}

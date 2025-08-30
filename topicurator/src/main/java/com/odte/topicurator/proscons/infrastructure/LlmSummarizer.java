package com.odte.topicurator.proscons.infrastructure;

public interface LlmSummarizer {
    record Result(String title, String summary, String pros, String neutral, String cons, Short bias) {}
    Result summarizeFromUrl(String url);
}

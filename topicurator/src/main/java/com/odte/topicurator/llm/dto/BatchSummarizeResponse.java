package com.odte.topicurator.llm.dto;

import java.util.List;

public record BatchSummarizeResponse(List<SummarizeResponse> items) {}

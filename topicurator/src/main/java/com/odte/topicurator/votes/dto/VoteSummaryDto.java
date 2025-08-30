package com.odte.topicurator.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteSummaryDto {
    private long prosCount;
    private long consCount;
    private long neutralCount;
}

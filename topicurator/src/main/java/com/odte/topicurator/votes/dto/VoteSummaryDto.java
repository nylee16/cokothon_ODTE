package com.odte.topicurator.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteSummaryDto {
    private long totalVotes;
    private long prosCount;
    private long consCount;
    private long neutralCount;
    private double proPercentage;
    private double conPercentage;
    private double neutralPercentage;
}
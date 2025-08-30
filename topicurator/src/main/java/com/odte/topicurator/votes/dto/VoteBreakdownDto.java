package com.odte.topicurator.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteBreakdownDto {
    private String group;          // ex) "남성", "여성", "20대", "30대", "학생"
    private long prosCount;
    private long consCount;
    private long neutralCount;

    private double proPercentage;      // ✅ 퍼센트 추가
    private double conPercentage;
    private double neutralPercentage;

    public VoteBreakdownDto(String group, long prosCount, long consCount, long neutralCount) {
        this.group = group;
        this.prosCount = prosCount;
        this.consCount = consCount;
        this.neutralCount = neutralCount;

        long total = prosCount + consCount + neutralCount;
        this.proPercentage = total > 0 ? (prosCount * 100.0 / total) : 0.0;
        this.conPercentage = total > 0 ? (consCount * 100.0 / total) : 0.0;
        this.neutralPercentage = total > 0 ? (neutralCount * 100.0 / total) : 0.0;
    }
}

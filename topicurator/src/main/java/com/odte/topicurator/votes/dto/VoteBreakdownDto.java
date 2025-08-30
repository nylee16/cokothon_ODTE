package com.odte.topicurator.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoteBreakdownDto {
    private String group;      // ex) "남성", "여성" or "20대", "30대" or "학생", "회사원"
    private long prosCount;
    private long consCount;
    private long neutralCount;
}

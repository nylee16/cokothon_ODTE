package com.odte.topicurator.votes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteRequestDto {
    // "PROS", "CONS", "NEUTRAL" 중 하나
    private String choice;
}

package com.odte.topicurator.votes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteCreationResponseDto {
    private Long voteId;
    private Long newsId;
    private String choice;
}

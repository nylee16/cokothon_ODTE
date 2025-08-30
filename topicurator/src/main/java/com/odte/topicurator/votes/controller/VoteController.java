package com.odte.topicurator.votes.controller;

import com.odte.topicurator.entity.Votes;
import com.odte.topicurator.votes.dto.VoteRequestDto;
import com.odte.topicurator.votes.dto.VoteSummaryDto;
import com.odte.topicurator.votes.dto.VoteBreakdownDto;
import com.odte.topicurator.votes.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // 🗳️ 투표 등록
    @PutMapping("/{newsId}/votes")
    public ResponseEntity<Votes> vote(
            @PathVariable Long newsId,
            @RequestBody VoteRequestDto requestDto,
            @RequestHeader("X-USER-ID") Long userId // ⚠️ JWT 연동 시 교체 예정
    ) {
        Votes vote = voteService.vote(newsId, userId, requestDto);
        return ResponseEntity.ok(vote);
    }

    // 📊 전체 통계 조회
    @GetMapping("/{newsId}/votes/summary")
    public ResponseEntity<VoteSummaryDto> getVoteSummary(@PathVariable Long newsId) {
        VoteSummaryDto summary = voteService.getVoteSummary(newsId);
        return ResponseEntity.ok(summary);
    }

    // 📊 분포 통계 조회 (성별/연령/직업)
    @GetMapping("/{newsId}/votes/breakdown")
    public ResponseEntity<List<VoteBreakdownDto>> getVoteBreakdown(
            @PathVariable Long newsId,
            @RequestParam String dimension
    ) {
        List<VoteBreakdownDto> breakdown = voteService.getVoteBreakdown(newsId, dimension);
        return ResponseEntity.ok(breakdown);
    }
}

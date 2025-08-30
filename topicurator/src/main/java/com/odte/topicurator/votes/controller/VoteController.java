package com.odte.topicurator.votes.controller;

import com.odte.topicurator.entity.Votes;
import com.odte.topicurator.votes.dto.VoteRequestDto;
import com.odte.topicurator.votes.dto.VoteSummaryDto;
import com.odte.topicurator.votes.dto.VoteBreakdownDto;
import com.odte.topicurator.votes.service.VoteService;
import com.odte.topicurator.common.dto.ApiResponse;
import com.odte.topicurator.auth.Domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    // 🗳️ 투표 등록
    @PutMapping("/{newsId}/votes")
    public ResponseEntity<ApiResponse<Votes>> vote(
            @PathVariable Long newsId,
            @RequestBody VoteRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails // ✅ JWT 연동된 사용자 정보 주입
    ) {
        Long userId = userDetails.getId(); // User 엔티티 ID 바로 꺼냄
        Votes vote = voteService.vote(newsId, userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("투표 등록 성공", vote));
    }

    // 📊 전체 통계 조회
    @GetMapping("/{newsId}/votes/summary")
    public ResponseEntity<ApiResponse<VoteSummaryDto>> getVoteSummary(@PathVariable Long newsId) {
        VoteSummaryDto summary = voteService.getVoteSummary(newsId);
        return ResponseEntity.ok(ApiResponse.success("투표 통계 조회 성공", summary));
    }

    // 📊 분포 통계 조회 (성별/연령/직업)
    @GetMapping("/{newsId}/votes/breakdown")
    public ResponseEntity<ApiResponse<List<VoteBreakdownDto>>> getVoteBreakdown(
            @PathVariable Long newsId,
            @RequestParam String dimension
    ) {
        List<VoteBreakdownDto> breakdown = voteService.getVoteBreakdown(newsId, dimension);
        return ResponseEntity.ok(ApiResponse.success("투표 분포 통계 조회 성공", breakdown));
    }
}

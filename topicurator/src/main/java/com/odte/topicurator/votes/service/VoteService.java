package com.odte.topicurator.votes.service;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.Proscons;
import com.odte.topicurator.entity.User;
import com.odte.topicurator.entity.Votes;
import com.odte.topicurator.votes.dto.VoteBreakdownDto;
import com.odte.topicurator.votes.dto.VoteRequestDto;
import com.odte.topicurator.votes.dto.VoteSummaryDto;
import com.odte.topicurator.votes.dto.VoteCreationResponseDto;
import com.odte.topicurator.votes.repository.VoteRepository;
import com.odte.topicurator.repository.NewsRepository;
import com.odte.topicurator.repository.ProsconsRepository;
import com.odte.topicurator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final NewsRepository newsRepository;
    private final ProsconsRepository prosconsRepository;
    private final UserRepository userRepository;

    @Transactional
    public VoteCreationResponseDto vote(Long newsId, Long userId, VoteRequestDto requestDto) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다."));

        Proscons proscons = prosconsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스에 대한 찬반 요약이 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ⚠️ 중복 투표 방지 로직은 필요 없다고 하셨으니 주석 처리하거나 삭제
        // if (voteRepository.existsByUserIdAndProsconsId(userId, proscons.getId())) {
        //     throw new IllegalStateException("이미 투표했습니다.");
        // }

        // ✅ choice 유효성 검사
        String choice = requestDto.getChoice().toUpperCase();
        if (!(choice.equals("PROS") || choice.equals("CONS") || choice.equals("NEUTRAL"))) {
            throw new IllegalArgumentException("투표 선택은 PROS / CONS / NEUTRAL 중 하나여야 합니다.");
        }

        Votes vote = new Votes();
        vote.setUser(user);
        vote.setProscons(proscons);
        vote.setChoice(choice);
        vote.setCreatedAt(LocalDateTime.now());

        Votes savedVote = voteRepository.save(vote);

        return new VoteCreationResponseDto(savedVote.getId(), newsId, savedVote.getChoice());
    }

    // 📊 전체 통계 조회
    @Transactional
    public VoteSummaryDto getVoteSummary(Long newsId) {
        long pros = voteRepository.countProsByNewsId(newsId);
        long cons = voteRepository.countConsByNewsId(newsId);
        long neutral = voteRepository.countNeutralByNewsId(newsId);

        long total = pros + cons + neutral;
        double proPct = total > 0 ? (pros * 100.0 / total) : 0;
        double conPct = total > 0 ? (cons * 100.0 / total) : 0;
        double neutralPct = total > 0 ? (neutral * 100.0 / total) : 0;

        return new VoteSummaryDto(total, pros, cons, neutral, proPct, conPct, neutralPct);
    }

    // 📊 분포 통계 조회 (성별/연령/직업)
    @Transactional
    public List<VoteBreakdownDto> getVoteBreakdown(Long newsId, String dimension) {
        return switch (dimension.toLowerCase()) {
            case "gender" -> voteRepository.breakdownByGender(newsId);
            case "age"    -> {
                int currentYear = Year.now().getValue();
                yield voteRepository.breakdownByAge(newsId, currentYear);
            }
            case "job"    -> voteRepository.breakdownByJob(newsId);
            default -> throw new IllegalArgumentException("dimension 값은 gender|age|job 중 하나여야 합니다.");
        };
    }
}

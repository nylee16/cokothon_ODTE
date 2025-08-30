package com.odte.topicurator.votes.service;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.Prosncons;
import com.odte.topicurator.entity.User;
import com.odte.topicurator.entity.Votes;
import com.odte.topicurator.votes.dto.VoteBreakdownDto;
import com.odte.topicurator.votes.dto.VoteRequestDto;
import com.odte.topicurator.votes.dto.VoteSummaryDto;
import com.odte.topicurator.votes.repository.VoteRepository;
import com.odte.topicurator.repository.NewsRepository;
import com.odte.topicurator.repository.ProsnconsRepository;
import com.odte.topicurator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final NewsRepository newsRepository;
    private final ProsnconsRepository prosnconsRepository;
    private final UserRepository userRepository;

    @Transactional
    public Votes vote(Long newsId, Long userId, VoteRequestDto requestDto) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("뉴스를 찾을 수 없습니다."));

        Prosncons prosncons = prosnconsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스에 대한 찬반 요약이 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ⚠️ 중복 투표 방지 로직은 필요 없다고 하셨으니 주석 처리하거나 삭제
        // if (voteRepository.existsByUserIdAndProsnconsId(userId, prosncons.getId())) {
        //     throw new IllegalStateException("이미 투표했습니다.");
        // }

        // ✅ choice 유효성 검사
        String choice = requestDto.getChoice().toUpperCase();
        if (!(choice.equals("PROS") || choice.equals("CONS") || choice.equals("NEUTRAL"))) {
            throw new IllegalArgumentException("투표 선택은 PROS / CONS / NEUTRAL 중 하나여야 합니다.");
        }

        Votes vote = new Votes();
        vote.setUser(user);
        vote.setProsncons(prosncons);
        vote.setChoice(choice);
        vote.setCreatedAt(LocalDateTime.now());

        return voteRepository.save(vote);
    }

    // 📊 전체 통계 조회
    @Transactional
    public VoteSummaryDto getVoteSummary(Long newsId) {
        long pros = voteRepository.countProsByNewsId(newsId);
        long cons = voteRepository.countConsByNewsId(newsId);
        long neutral = voteRepository.countNeutralByNewsId(newsId);

        return new VoteSummaryDto(pros, cons, neutral);
    }

    // 📊 분포 통계 조회 (성별/연령/직업)
    @Transactional
    public List<VoteBreakdownDto> getVoteBreakdown(Long newsId, String dimension) {
        return switch (dimension.toLowerCase()) {
            case "gender" -> voteRepository.breakdownByGender(newsId);
            case "age"    -> voteRepository.breakdownByAge(newsId);
            case "job"    -> voteRepository.breakdownByJob(newsId);
            default -> throw new IllegalArgumentException("dimension 값은 gender|age|job 중 하나여야 합니다.");
        };
    }
}

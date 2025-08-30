package com.odte.topicurator.proscons.application;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.User;
import com.odte.topicurator.proscons.controller.dto.ProsConsRes;
import com.odte.topicurator.proscons.controller.dto.ProsConsSummarizeReq;
import com.odte.topicurator.proscons.domain.ProsCons;
import com.odte.topicurator.proscons.infrastructure.LlmSummarizer;
import com.odte.topicurator.proscons.infrastructure.ProsConsRepository;
import com.odte.topicurator.repository.NewsRepository;
import com.odte.topicurator.repository.UserRepository; // ✅ 여기로 변경

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProsConsGenerateService {

    private final LlmSummarizer llm;
    private final NewsRepository newsRepo;
    private final ProsConsRepository prosConsRepo;
    private final UserRepository userRepo; // ✅ 타입 변경

    @Transactional
    public ProsConsRes summarize(ProsConsSummarizeReq req, Long requesterIdOrNull) {
        var r = llm.summarizeFromUrl(req.url());

        if (req.saveOrDefault()) {
            if (requesterIdOrNull == null) throw new AccessDeniedException("로그인이 필요합니다.");
            if (req.newsId() == null) throw new IllegalArgumentException("save=true일 때는 newsId가 필요합니다.");

            News news = newsRepo.findById(req.newsId())
                    .orElseThrow(() -> new NoSuchElementException("뉴스를 찾을 수 없습니다. ID=" + req.newsId()));

            if (prosConsRepo.existsByNews_IdAndLink(news.getId(), req.url())) {
                throw new IllegalArgumentException("이미 동일 URL로 생성된 요약이 있습니다.");
            }

            User author = userRepo.findById(requesterIdOrNull)   // ✅ User 반환
                    .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID=" + requesterIdOrNull));

            ProsCons pc = new ProsCons();
            pc.setNews(news);
            pc.setCreatedBy(author);         // ✅ ProsCons.createdBy 가 User인 경우
            pc.setSummary(r.summary());
            pc.setLink(req.url());
            pc.setPros(r.pros());
            pc.setNeutral(r.neutral());
            pc.setCons(r.cons());
            pc.setBias(r.bias());

            pc = prosConsRepo.save(pc);

            return new ProsConsRes(
                    pc.getId(), news.getId(), author.getId(), author.getUsername(),
                    pc.getSummary(), pc.getLink(), pc.getPros(), pc.getNeutral(), pc.getCons(), pc.getBias()
            );
        }

        return new ProsConsRes(
                null, req.newsId(), null, null,
                r.summary(), req.url(), r.pros(), r.neutral(), r.cons(), r.bias()
        );
    }
}

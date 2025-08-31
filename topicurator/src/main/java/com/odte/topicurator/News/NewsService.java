package com.odte.topicurator.News;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.Proscons;
import com.odte.topicurator.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    // 뉴스 상세 조회 (Proscons ID 포함)
    @Transactional(readOnly = true)
    public NewsTopDto getNewsDetail(Long newsId) {
        News n = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + newsId));

        Long prosconsId = n.getProscons() != null ? n.getProscons().getId() : null;

        return new NewsTopDto(
                n.getId(),
                n.getTitle(),
                n.getDescription(),
                n.getTeaserText(),
                n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                n.getViews(),
                n.getCreatedAt(),
                n.getImageUrl(),
                n.getCategory(),
                prosconsId
        );
    }

    // 상위 뉴스 조회 (Proscons ID 포함)
    @Transactional(readOnly = true)
    public List<NewsTopDto> getTopNews(int limit, String period) {
        LocalDateTime since = parsePeriodSafe(period);
        Pageable page = PageRequest.of(0, Math.max(1, limit));

        return newsRepository.findByCreatedAtAfterOrderByViewsDesc(since, page)
                .getContent().stream()
                .map(n -> {
                    Long prosconsId = n.getProscons() != null ? n.getProscons().getId() : null;
                    return new NewsTopDto(
                            n.getId(),
                            n.getTitle(),
                            n.getDescription(),
                            n.getTeaserText(),
                            n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                            n.getViews(),
                            n.getCreatedAt(),
                            n.getImageUrl(),
                            n.getCategory(),
                            prosconsId
                    );
                })
                .toList();
    }

    // 카테고리별 뉴스 조회 (Proscons ID 포함)
    @Transactional(readOnly = true)
    public Page<NewsTopDto> getNewsByCategory(String category, Pageable pageable) {
        return newsRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(n -> {
                    Long prosconsId = n.getProscons() != null ? n.getProscons().getId() : null;
                    return new NewsTopDto(
                            n.getId(),
                            n.getTitle(),
                            n.getDescription(),
                            n.getTeaserText(),
                            n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                            n.getViews(),
                            n.getCreatedAt(),
                            n.getImageUrl(),
                            n.getCategory(),
                            prosconsId
                    );
                });
    }

    // 조회수 증가
    @Transactional
    public void increaseViews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + newsId));
        news.setViews(news.getViews() == null ? 1 : news.getViews() + 1);
    }

    // 기간 파싱 (안전하게 처리)
    private LocalDateTime parsePeriodSafe(String period) {
        LocalDateTime now = LocalDateTime.now();
        if (period == null || period.isBlank()) return now.minusHours(24);

        try {
            if (period.endsWith("h")) {
                int hours = Integer.parseInt(period.substring(0, period.length() - 1));
                return now.minusHours(Math.max(1, hours));
            } else if (period.endsWith("d")) {
                int days = Integer.parseInt(period.substring(0, period.length() - 1));
                return now.minusDays(Math.max(1, days));
            }
        } catch (NumberFormatException ignore) {
            // 기본: 24시간 전
        }
        return now.minusHours(24);
    }
}

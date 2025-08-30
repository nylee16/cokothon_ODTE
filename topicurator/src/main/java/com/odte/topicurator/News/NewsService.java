package com.odte.topicurator.News;

import com.odte.topicurator.entity.News;
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

    @Transactional(readOnly = true)
    public NewsTopDto getNewsDetail(Long newsId) {
        // createdBy 접근이 필요하므로 트랜잭션 안에서 DTO로 변환
        News n = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + newsId));

        return new NewsTopDto(
                n.getId(),
                n.getTitle(),
                n.getDescription(),
                n.getTeaserText(),
                n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                n.getViews(),
                n.getCreatedAt(),
                n.getImageUrl(),
                n.getCategory()
        );
    }

    @Transactional(readOnly = true)
    public List<NewsTopDto> getTopNews(int limit, String period) {
        LocalDateTime since = parsePeriodSafe(period); // ✅ NPE/형식오류 방지
        var page = PageRequest.of(0, Math.max(1, limit)); // 최소 1
        var slice = newsRepository.findByCreatedAtAfterOrderByViewsDesc(since, page);

        return slice.getContent().stream()
                .map(n -> new NewsTopDto(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription(),
                        n.getTeaserText(),
                        n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                        n.getViews(),
                        n.getCreatedAt(),
                        n.getImageUrl(),
                        n.getCategory()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NewsTopDto> getNewsByCategory(String category, Pageable pageable) {
        return newsRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(n -> new NewsTopDto(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription(),
                        n.getTeaserText(),
                        n.getCreatedBy() != null ? n.getCreatedBy().getUsername() : null,
                        n.getViews(),
                        n.getCreatedAt(),
                        n.getImageUrl(),
                        n.getCategory()
                ));
    }

    @Transactional
    public void increaseViews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + newsId));
        news.setViews(news.getViews() == null ? 1 : news.getViews() + 1);
    }

    // ✅ null/빈문자/형식오류 모두 처리 (기본: 24시간)
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
            // fall through to default
        }
        return now.minusHours(24);
    }
}

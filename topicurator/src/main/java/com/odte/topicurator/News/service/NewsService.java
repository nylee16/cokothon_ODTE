package com.odte.topicurator.News.service;

import com.odte.topicurator.News.dto.NewsTopDto;
import com.odte.topicurator.entity.News;
import com.odte.topicurator.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsTopDto getNewsDetail(Long newsId) {
        return newsRepository.findById(newsId)
                .map(n -> new NewsTopDto(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription(),
                        n.getTeaserText(),
                        n.getCreatedBy().getUsername(), // User.username
                        n.getViews(),
                        n.getCreatedAt(),
                        n.getImageUrl(),
                        n.getCategory()
                ))
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다. ID: " + newsId));
    }

    public List<NewsTopDto> getTopNews(int limit, String period) {
        LocalDateTime since = parsePeriod(period); // 24h, 7d 등 변환
        List<News> newsList = newsRepository.findByCreatedAtAfterOrderByViewsDesc(since);

        return newsList.stream()
                .limit(limit) // 상위 N개
                .map(n -> new NewsTopDto(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription(),
                        n.getTeaserText(),
                        n.getCreatedBy().getUsername(), // User.username
                        n.getViews(),
                        n.getCreatedAt(),
                        n.getImageUrl(),
                        n.getCategory()
                ))
                .collect(Collectors.toList());
    }

    public Page<NewsTopDto> getNewsByCategory(String category, Pageable pageable) {
        return newsRepository.findByCategory(category, pageable)
                .map(n -> new NewsTopDto(
                        n.getId(),
                        n.getTitle(),
                        n.getDescription(),
                        n.getTeaserText(),
                        n.getCreatedBy().getUsername(),
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

    private LocalDateTime parsePeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        if (period.endsWith("h")) {
            int hours = Integer.parseInt(period.replace("h", ""));
            return now.minusHours(hours);
        } else if (period.endsWith("d")) {
            int days = Integer.parseInt(period.replace("d", ""));
            return now.minusDays(days);
        }
        return now.minusHours(24); // 기본 24시간
    }
}

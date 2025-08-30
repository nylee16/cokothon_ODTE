package com.odte.topicurator.News.controller;

import com.odte.topicurator.News.dto.NewsTopDto;
import com.odte.topicurator.News.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/top")
    public ResponseEntity<List<NewsTopDto>> getTopNews(
            @RequestParam(defaultValue = "3") int limit,
            @RequestParam(defaultValue = "24h") String period
    ) {
        List<NewsTopDto> topNews = newsService.getTopNews(limit, period);
        return ResponseEntity.ok(topNews);
    }

    @GetMapping
    public Page<NewsTopDto> getNewsByCategory(
            @RequestParam String category,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return newsService.getNewsByCategory(category, pageable);
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<NewsTopDto> getNewsDetail(@PathVariable Long newsId) {
        NewsTopDto newsDetail = newsService.getNewsDetail(newsId);
        return ResponseEntity.ok(newsDetail);
    }

    @PostMapping("/{newsId}/impression")
    public ResponseEntity<Map<String, String>> increaseImpression(@PathVariable Long newsId) {
        newsService.increaseViews(newsId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}

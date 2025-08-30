package com.odte.topicurator.News.controller;

import com.odte.topicurator.News.dto.NewsTopDto;
import com.odte.topicurator.News.service.NewsService;
import com.odte.topicurator.common.dto.ApiResponse; // ✅ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<NewsTopDto>>> getTopNews(
            @RequestParam(defaultValue = "3") int limit,
            @RequestParam(defaultValue = "24h") String period
    ) {
        List<NewsTopDto> topNews = newsService.getTopNews(limit, period);
        return ResponseEntity.ok(ApiResponse.success("인기 뉴스 조회 성공", topNews));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NewsTopDto>>> getNewsByCategory(
            @RequestParam String category,
            @PageableDefault(page = 0, size = 10, sort = "createdAt",
                    direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<NewsTopDto> newsPage = newsService.getNewsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("카테고리별 뉴스 조회 성공", newsPage));
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsTopDto>> getNewsDetail(@PathVariable Long newsId) {
        NewsTopDto newsDetail = newsService.getNewsDetail(newsId);
        return ResponseEntity.ok(ApiResponse.success("뉴스 상세 조회 성공", newsDetail));
    }

    @PostMapping("/{newsId}/impression")
    public ResponseEntity<ApiResponse<Void>> increaseImpression(@PathVariable Long newsId) {
        newsService.increaseViews(newsId);
        return ResponseEntity.ok(ApiResponse.successWithNoData("뉴스 조회수 증가 성공"));
    }
}

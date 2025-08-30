package com.odte.topicurator.repository;

import com.odte.topicurator.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // 특정 기간 이후의 뉴스 조회, 조회수 내림차순, 상위 limit개
    List<News> findByCreatedAtAfterOrderByViewsDesc(LocalDateTime since);

    Page<News> findByCategory(String category, Pageable pageable);
}

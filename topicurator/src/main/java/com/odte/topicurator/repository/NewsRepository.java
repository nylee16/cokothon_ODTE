package com.odte.topicurator.repository;

import com.odte.topicurator.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // ✅ since 이후 조회수 내림차순 + createdBy(User) 즉시 로딩 + 페이지/limit 지원
    @EntityGraph(attributePaths = "createdBy")
    Slice<News> findByCreatedAtAfterOrderByViewsDesc(LocalDateTime since, Pageable pageable);

    // 카테고리 페이징도 동일하게 즉시 로딩
    @EntityGraph(attributePaths = "createdBy")
    Page<News> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
}

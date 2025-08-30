package com.odte.topicurator.proscons.domain;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "proscons",
        uniqueConstraints = @UniqueConstraint(name = "uq_proscons_news_user", columnNames = {"news_id", "created_by"})
)
@Getter @Setter
public class ProsCons {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)            // 서비스에서 DTO 변환 시 안전
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(length = 255, nullable = false)
    private String link;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String pros;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String neutral;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String cons;

    @Column(nullable = false)
    private Integer bias; // -100 ~ 100 등 범위는 서비스/DTO에서 검증
}

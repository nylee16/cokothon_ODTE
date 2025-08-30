package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prosncons")
@Data
public class Prosncons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 255)
    private String link;

    @Column(length = 255)
    private String pros;

    @Column(columnDefinition = "TEXT")
    private String neutral;

    @Column(length = 255)
    private String cons;

    private Integer bias;

    private LocalDateTime createdAt;
}

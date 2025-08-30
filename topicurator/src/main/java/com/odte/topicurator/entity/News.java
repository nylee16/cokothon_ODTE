package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
@Getter
@Setter
@NoArgsConstructor
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    private Long views;

    private LocalDateTime createdAt;

    @Column(name = "teaser_text", length = 255)
    private String teaserText;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}

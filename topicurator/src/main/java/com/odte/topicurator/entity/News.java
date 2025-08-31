package com.odte.topicurator.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "teaser_text", length = 255)
    private String teaserText;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // 1:1 관계로 Proscons 연결
    @OneToOne(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Proscons proscons;

    public News(User createdBy, String title, String description, String category, String teaserText, String imageUrl) {
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.category = category;
        this.views = 0L;
        this.createdAt = LocalDateTime.now();
        this.teaserText = teaserText;
        this.imageUrl = imageUrl;
    }
}

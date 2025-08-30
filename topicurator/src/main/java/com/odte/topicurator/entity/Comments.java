package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proscons_id")
    private Proscons proscons;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 10)
    private String choice;

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "hate_count")
    private Long hateCount;

    public Comments(Proscons proscons, User user, String content, String choice) {
        this.proscons = proscons;
        this.user = user;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.choice = choice;
        this.likeCount = 0L;
        this.hateCount = 0L;
    }
}

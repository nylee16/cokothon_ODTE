package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "proscons")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Proscons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
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

    @Column(length = 255)
    private String cons;

    private Integer bias;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Proscons(News news, User createdBy, String summary, String link, String pros, String cons, Integer bias) {
        this.news = news;
        this.createdBy = createdBy;
        this.summary = summary;
        this.link = link;
        this.pros = pros;
        this.cons = cons;
        this.bias = bias;
        this.createdAt = LocalDateTime.now();
    }
}

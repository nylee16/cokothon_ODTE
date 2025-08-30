package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Votes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proscons_id")
    private Proscons proscons;

    @Column(length = 10)
    private String choice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Votes(User user, Proscons proscons, String choice) {
        this.user = user;
        this.proscons = proscons;
        this.choice = choice;
        this.createdAt = LocalDateTime.now();
    }
}

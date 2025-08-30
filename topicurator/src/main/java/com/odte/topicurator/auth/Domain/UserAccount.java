package com.odte.topicurator.auth.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_user_email", columnNames="email"),
                @UniqueConstraint(name="uq_user_username", columnNames="username")
        })
@Getter @Setter
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100) private String email;
    @Column(nullable=false, length=255) private String password;
    @Column(nullable=false, length=50)  private String username;

    @Column(name="birth_year") private Short birthYear;
    @Column(nullable=false, length=20) private String sex;
    @Column(length=255) private String job;

    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @PrePersist void onCreate(){ if (createdAt==null) createdAt = LocalDateTime.now(); }
}


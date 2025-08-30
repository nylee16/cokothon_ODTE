package com.odte.topicurator.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50)
    private String username;

    private Integer age;

    @Column(length = 20)
    private String sex;

    @Column(length = 255)
    private String job;

    private LocalDateTime createdAt;
}
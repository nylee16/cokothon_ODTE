package com.odte.topicurator.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name="birth_year")
    private Short birthYear;

    @Column(length = 20)
    private String sex;

    @Column(length = 255)
    private String job;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String email, String password, String username, Integer age, String sex, String job) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.age = age;
        this.sex = sex;
        this.job = job;
        this.createdAt = LocalDateTime.now();
    }

}
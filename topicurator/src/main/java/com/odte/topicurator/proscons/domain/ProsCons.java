package com.odte.topicurator.proscons.domain;

import com.odte.topicurator.entity.News;
import com.odte.topicurator.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(
        name = "proscons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_proscons_news_user", columnNames = {"news_id", "created_by"}),
                @UniqueConstraint(name = "uq_proscons_news_link", columnNames = {"news_id", "link"})
        }
)
@Check(constraints = "bias BETWEEN 0 AND 100") // DB 레벨에서도 0~100 보장 (DB가 지원하는 경우)
@Getter @Setter
public class ProsCons {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(length = 255, nullable = false)
    private String link;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String pros;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String neutral="";

    @Column(columnDefinition = "TEXT", nullable = true)
    private String cons;

    @Column(nullable = false)
    @Min(value = 0, message = "bias는 0 이상이어야 합니다.")
    @Max(value = 100, message = "bias는 100 이하여야 합니다.")
    private Short bias; // 0~100
}

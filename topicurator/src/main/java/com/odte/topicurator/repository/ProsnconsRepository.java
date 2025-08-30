package com.odte.topicurator.repository;

import com.odte.topicurator.entity.Prosncons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProsnconsRepository extends JpaRepository<Prosncons, Long> {
    java.util.Optional<Prosncons> findByNewsId(Long newsId);
}

package com.odte.topicurator.repository;

import com.odte.topicurator.entity.Proscons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProsconsRepository extends JpaRepository<Proscons, Long> {
    java.util.Optional<Proscons> findByNewsId(Long newsId);
}

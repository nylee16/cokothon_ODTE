package com.odte.topicurator.proscons.infrastructure;

import com.odte.topicurator.proscons.domain.ProsCons;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProsConsRepository extends JpaRepository<ProsCons, Long> {

    @EntityGraph(attributePaths = {"createdBy", "news"})
    Page<ProsCons> findByNews_Id(Long newsId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "news"})
    Optional<ProsCons> findWithJoinsById(Long id);

    boolean existsByNews_IdAndLink(Long newsId, String link);
}

package com.odte.topicurator.repository;

import com.odte.topicurator.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query(
            value = "SELECT c FROM Comments c JOIN FETCH c.user WHERE c.proscons.id = :prosconsId",
            countQuery = "SELECT COUNT(c) FROM Comments c WHERE c.proscons.id = :prosconsId"
    )
    Page<Comments> findByProsconsIdWithUser(@Param("prosconsId") Long prosconsId, Pageable pageable);
}

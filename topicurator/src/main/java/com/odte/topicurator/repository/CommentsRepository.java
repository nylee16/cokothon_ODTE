package com.odte.topicurator.repository;

import com.odte.topicurator.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query(
            value = "SELECT c FROM Comments c JOIN FETCH c.user WHERE c.prosncons.id = :prosconsId",
            countQuery = "SELECT COUNT(c) FROM Comments c WHERE c.prosncons.id = :prosconsId"
    )
    Page<Comments> findByProsnconsIdWithUser(@Param("prosconsId") Long prosconsId, Pageable pageable);
}

package com.odte.topicurator.repository;

import com.odte.topicurator.entity.Votes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotesRepository extends JpaRepository<Votes, Long> {
}

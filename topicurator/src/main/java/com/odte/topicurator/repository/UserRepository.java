package com.odte.topicurator.repository;

import com.odte.topicurator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

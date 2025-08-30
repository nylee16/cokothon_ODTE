package com.odte.topicurator.votes.repository;

import com.odte.topicurator.entity.Votes;
import com.odte.topicurator.votes.dto.VoteBreakdownDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Votes, Long> {

    boolean existsByUserIdAndProsnconsId(Long userId, Long prosnconsId);

    @Query("SELECT COUNT(v) FROM Votes v WHERE v.prosncons.news.id = :newsId AND v.choice = 'PROS'")
    long countProsByNewsId(@Param("newsId") Long newsId);

    @Query("SELECT COUNT(v) FROM Votes v WHERE v.prosncons.news.id = :newsId AND v.choice = 'CONS'")
    long countConsByNewsId(@Param("newsId") Long newsId);

    @Query("SELECT COUNT(v) FROM Votes v WHERE v.prosncons.news.id = :newsId AND v.choice = 'NEUTRAL'")
    long countNeutralByNewsId(@Param("newsId") Long newsId);

    // 📊 성별 분포
    @Query("SELECT new com.odte.topicurator.votes.dto.VoteBreakdownDto(u.sex, " +
            "SUM(CASE WHEN v.choice = 'PROS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'CONS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'NEUTRAL' THEN 1 ELSE 0 END)) " +
            "FROM Votes v JOIN v.user u " +
            "WHERE v.prosncons.news.id = :newsId " +
            "GROUP BY u.sex")
    List<VoteBreakdownDto> breakdownByGender(@Param("newsId") Long newsId);

    // 📊 연령대 분포 (10단위로 그룹핑)
    @Query("SELECT new com.odte.topicurator.votes.dto.VoteBreakdownDto(" +
            "CONCAT((u.age/10)*10, '대'), " +
            "SUM(CASE WHEN v.choice = 'PROS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'CONS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'NEUTRAL' THEN 1 ELSE 0 END)) " +
            "FROM Votes v JOIN v.user u " +
            "WHERE v.prosncons.news.id = :newsId " +
            "GROUP BY (u.age/10)")
    List<VoteBreakdownDto> breakdownByAge(@Param("newsId") Long newsId);

    // 📊 직업 분포
    @Query("SELECT new com.odte.topicurator.votes.dto.VoteBreakdownDto(u.job, " +
            "SUM(CASE WHEN v.choice = 'PROS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'CONS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.choice = 'NEUTRAL' THEN 1 ELSE 0 END)) " +
            "FROM Votes v JOIN v.user u " +
            "WHERE v.prosncons.news.id = :newsId " +
            "GROUP BY u.job")
    List<VoteBreakdownDto> breakdownByJob(@Param("newsId") Long newsId);
}

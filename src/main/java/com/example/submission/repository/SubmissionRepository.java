package com.example.submission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.submission.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long>{
	
	// 특정 회차의 과제들 중에서 해당 유저가 제출한 과제의 ID 목록을 한 번에 조회 
	@Query("""
			SELECT s.assignment.id 
			FROM Submission s
			WHERE s.assignment.session.id = :sessionId
			AND s.member.id = :memberId
	""")
	List<Long> findSubmittedAssignmentIdsBySessionIdAndMemberId(@Param("sessionId") Long sessionId, @Param("memberId") Long memberId);
	
	
	// 과제 제출 여부 확인 - 중복 방지 
	boolean existsByAssignmentIdAndMemberId(Long assignmentId, Long memberId);
}

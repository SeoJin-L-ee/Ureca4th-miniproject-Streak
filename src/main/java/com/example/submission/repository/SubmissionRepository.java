package com.example.submission.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.submission.dto.response.SessionSubmissionCountDto;
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
	
	// 주어진 과제 id(다음 회차의 과제 id들) 중에서 현재 멤버가 제출한 id
	@Query("""
			SELECT DISTINCT s.assignment.id
			FROM Submission s
			WHERE s.member.id = :memberId
				AND s.assignment.id IN :assignmentIds
			""")
	List<Long> findSubmittedAssignmentIds(
			@Param("memberId") Long memberId,
			@Param("assignmentIds") List<Long> assignmentIds
	);
	
	// 스터디 전체의 마감된 과제에 대한 제출 건수
	@Query("""
			SELECT count(s) 
			FROM Submission s
				JOIN s.assignment a
				JOIN a.session se
			WHERE se.study.id = :studyId
				AND a.dueAt < :now
			""")
	long countStudySubmissionForClosed(
			@Param("studyId") Long studyId,
			@Param("now") LocalDateTime now
	);
	
	// 특정 Member 의 마감된 과제에 대한 제출 건수
	@Query("""
			SELECT count(s) 
			FROM Submission s
				JOIN s.assignment a
				JOIN a.session se
			WHERE se.study.id = :studyId
				AND s.member.id = :memberId
				AND a.dueAt < :now
			""")
	long countMemberSubmissionForClosed(
			@Param("memberId") Long memberId,
			@Param("studyId") Long studyId,
			@Param("now") LocalDateTime now
	);
	
	// 회차별 과제 제출 건수
	@Query("""
			SELECT new com.example.submission.dto.response.SessionSubmissionCountDto(
				se.id,
				COUNT(s))
			FROM Submission s
				JOIN s.assignment a
				JOIN a.session se
			WHERE se.id IN :sessionIds
			GROUP BY se.id
			""")
	List<SessionSubmissionCountDto> countSubmissionsBySessionIds(@Param("sessionIds") List<Long> sessionIds);
	
}

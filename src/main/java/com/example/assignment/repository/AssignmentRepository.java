package com.example.assignment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.assignment.dto.response.SessionAssignmentCountDto;
import com.example.assignment.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{
	
	// 특정 회차에 연결된 과제 목록 조회 
	List<Assignment> findAllBySessionId(Long sessionId);

	// 과제 ID와 회차 ID가 일치하는 과제 조회 
	Optional<Assignment> findByIdAndSessionId(Long assignmentId, Long sessionId);

	// 특정 기간 동안 멤버의 과제 조회 
	@Query("""
			SELECT DISTINCT a
			FROM Assignment a 
			JOIN FETCH a.session s
			JOIN FETCH s.study st
			JOIN Participant p ON p.study = st
			WHERE p.member.id = :memberId 
			AND a.dueAt BETWEEN :start AND :end
	""")
	List<Assignment> findByMemberIdAndDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
	
	// 특정 회차에 속하는 과제들 조회
    List<Assignment> findAllBySessionIdOrderByDueAtAsc(Long sessionId);
	
	// 마감기한이 지난 과제 수 (과제 제출률 계산에 사용됨)
	@Query("""
			SELECT COUNT(a)
			FROM Assignment a
			WHERE a.session.study.id = :studyId
				AND a.dueAt < :now
			""")
	long countClosedAssignments(
			@Param("studyId") Long studyId,
			@Param("now") LocalDateTime now
	);
	
	// 회차별 과제 수
	@Query("""
			SELECT new com.example.assignment.dto.response.SessionAssignmentCountDto(
				a.session.id,
				COUNT(a))
			FROM Assignment a
			WHERE a.session.id IN :sessionIds
			GROUP BY a.session.id
			""")
	List<SessionAssignmentCountDto> countAssignmentsBySessionIds(
			@Param("sessionIds") List<Long> sessionIds
    );
}

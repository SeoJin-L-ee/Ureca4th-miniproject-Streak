package com.example.assignment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	
	//마이페이지 - 마감 기한 과제 조회용. 내가 참여 중인 스터디들의, 마감이 아직 안 지난 과제만 마감일 오름차순으로.
	// session/study를 같이 fetch해서 이후 스터디 제목을 꺼낼 때 N+1이 발생하지 않도록
	@Query("""
			SELECT a FROM Assignment a
			JOIN FETCH a.session s
			JOIN FETCH s.study
			WHERE s.study.id IN (SELECT p.study.id FROM Participant p WHERE p.member.id = :memberId)
			AND a.dueAt >= :now
			ORDER BY a.dueAt asc
			""")
	List<Assignment> findUpcomingByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}

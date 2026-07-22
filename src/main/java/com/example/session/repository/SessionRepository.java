package com.example.session.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long>{
	
	boolean existsByStudyIdAndSessionNumber(Long studyId, int sessionNumber);
	
	// 스터디 내에 있는 모든 회차를 조회 
	List<Session> findAllByStudyIdOrderBySessionNumberDesc(Long studyId);
	
	// 스터디 내에 있는 모든 회차의 개수를 조회 
	int countByStudyId(Long studyId);
	
	// 해당 스터디에 존재하는 회차인지 검증 
	boolean existsByIdAndStudyId(Long sessionId, Long studyId);
	
	// 특정 기간동안 멤버의 회차 조회 
	@Query("""
			SELECT DISTINCT s
			FROM Session s
			JOIN FETCH s.study st
			JOIN Participant p ON p.study = st
			WHERE p.member.id = :memberId
				AND s.startsAt BETWEEN :start AND :end
	""")
	List<Session> findByMemberIdAndDateRange(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

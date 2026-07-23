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
	
	//마이페이지 - 오늘 회차 조회용. 내가 참여 중인 스터디들 중 시작 시간이 오늘 범위(start~end) 안인 회차만
	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.study
			WHERE s.study.id IN (SELECT p.study.id FROM Participant p WHERE p.member.id = :memberId)
			AND s.startsAt BETWEEN :start AND :end
			ORDER BY s.startsAt asc
			""")
	List<Session> findTodaySessionsByMemberId(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

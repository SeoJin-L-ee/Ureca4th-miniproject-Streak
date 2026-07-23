package com.example.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
	
	// 특정 회차의 출석 목록 조회 
	List<Attendance> findAllBySessionId(Long sessionId);
	
	//마이페이지 - 평균 출석률/최장 Streak 계산용. 회차 시작 시간(session.startsAt) 오름차순 정렬
	@Query("SELECT a FROM Attendance a JOIN FETCH a.session WHERE a.member.id = :memberId ORDER BY a.session.startsAt ASC")
	List<Attendance> findAllByMemberIdOrderBySessionStartsAtAsc(@Param("memberId") Long memberId);
}

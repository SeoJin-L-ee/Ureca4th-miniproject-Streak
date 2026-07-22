package com.example.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
	
	// 특정 회차의 출석 목록 조회 
	List<Attendance> findAllBySessionId(Long sessionId);
	
	// Member 별 스터디 참석/미참석 횟수 count ( session.study.id)
	int countBySession_Study_IdAndMember_IdAndStatus(Long studyId, Long memberId, AttendanceStatus status);  
}

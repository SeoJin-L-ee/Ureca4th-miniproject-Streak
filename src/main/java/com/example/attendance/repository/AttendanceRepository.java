package com.example.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
	
	// 특정 회차의 출석 목록 조회 
	List<Attendance> findAllBySessionId(Long sessionId);
}

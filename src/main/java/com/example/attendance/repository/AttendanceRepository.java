package com.example.attendance.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.enums.AttendanceStatus;
import com.example.study.dto.response.StudyAttendanceRateDto;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>{
	
	// 특정 회차의 출석 목록 조회 
	List<Attendance> findAllBySessionId(Long sessionId);
	
	// 여러 스터디의 지나간 회차들에 대한 출석률을 GROUP BY로 집계 (Dto 하나 새로 만들어서 거기다가 프로젝션함)
    // 출석(PRESENT)인 경우에만 카운트해서, 전체 회차 수로 나눔
	@Query("""
			SELECT new com.example.study.dto.response.StudyAttendanceRateDto(
				s.study.id AS studyId,
				ROUND(( SUM(CASE WHEN a.status = :presentStatus THEN 1.0 ELSE 0.0 END) / COUNT(a) )*100, 1) AS attendanceRate
			)
			FROM Attendance a
			JOIN a.session s
			WHERE s.study.id IN :studyIds
			  AND s.startsAt < :now
			GROUP BY s.study.id
			""")
	List<StudyAttendanceRateDto> calculateStudyAttendanceRatesByStudyIds(
			@Param("studyIds") List<Long> studyIds,
			@Param("now") LocalDateTime now,
			@Param("presentStatus") AttendanceStatus presentStatus);
}
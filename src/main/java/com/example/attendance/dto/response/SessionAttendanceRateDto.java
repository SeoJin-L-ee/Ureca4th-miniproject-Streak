package com.example.attendance.dto.response;

// 특정 스터디의 회차별 평균 출석률 조회 쿼리에서 사용되는 프로젝션 DTO입니다.
public record SessionAttendanceRateDto(
		Long sessionId,
		Double attendanceRate
) {}

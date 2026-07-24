package com.example.attendance.dto.response;

import com.example.attendance.entity.enums.AttendanceStatus;

// 특정 회원의 회차별 출석 상태 조회 쿼리에서 사용되는 프로젝션 DTO입니다.
public record MemberSessionAttendanceDto(
		Long sessionId,
		AttendanceStatus status
) {}

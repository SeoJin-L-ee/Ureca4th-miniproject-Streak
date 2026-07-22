package com.example.session.dto.response;

import com.example.attendance.entity.enums.AttendanceStatus;

// 스터디 회차 상세 조회 시 출석 정보를 반환하는 DTO 
//SessionInfoResDto 내부에서 사용 
public record SessionAttendanceResDto(
	Long attendanceId,
	Long memberId,
	String memberName,
	AttendanceStatus status
) {}

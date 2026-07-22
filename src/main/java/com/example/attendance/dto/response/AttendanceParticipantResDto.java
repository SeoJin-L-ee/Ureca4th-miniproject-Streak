package com.example.attendance.dto.response;

import com.example.attendance.entity.enums.AttendanceStatus;

// 회차 내 개별 참여자의 출석 정보 DTO 
// AttendanceSessionResDto 내부에서 사용 
public record AttendanceParticipantResDto(
	Long memberId,
	String name,
	
	// 개별 출석 상태 
	AttendanceStatus status
) {}


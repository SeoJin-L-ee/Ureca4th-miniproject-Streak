package com.example.attendance.dto.request;

import com.example.attendance.entity.enums.AttendanceStatus;

// 개별 참여자 출석 변경 요청 DTO 
// BatchSaveAttendanceReqDto 내부에서 사용 
public record UpdateAttendanceReqDto(
	Long memberId,
	AttendanceStatus status
) {}

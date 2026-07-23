package com.example.session.dto.response;

import java.time.LocalDateTime;

import com.example.attendance.entity.enums.AttendanceStatus;

// 특정 회차 정보와, 해당 회차의 내출석여부/출석률/과제제출률 을 합친 DTO입니다.
public record MergedSessionDashboardDataDto(
		long sessionId,
		int sessionNumber,
		String title,
		LocalDateTime startsAt,
		
		AttendanceStatus myAttendanceStatus,
		Double teamAttendanceRate,
		
		Double teamAssignmentSubmissionRate,
		// 해당 회차에 과제가 등록되어 있는지
		boolean hasAssignments
) {}

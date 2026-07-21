package com.example.session.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 해당 회차를 상세 조회 시 응답 DTO
public record SessionInfoResDto(
	Long sessionId,
	int sessionNumber,
	String title,
	String content,
	LocalDateTime startsAt,
	
	// 해당 회차에 부여된 과제 목록 
	List<SessionAssignmentResDto> assignments,
	
	// 해당 회차 출석 정보 
	List<SessionAttendanceResDto> attendances
) {}

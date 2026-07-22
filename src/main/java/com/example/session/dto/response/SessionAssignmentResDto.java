package com.example.session.dto.response;

import java.time.LocalDateTime;

// 스터디 회차 상세 조회 시 해당 회차의 과제 정보를 반환하는 DTO 
// SessionInfoResDto 내부에서 사용 
public record SessionAssignmentResDto(
	Long assignmentId,
	String title, 
	LocalDateTime dueAt,
		
	// 로그인한 사용자의 제출 여부 
	boolean isSubmitted
) {}

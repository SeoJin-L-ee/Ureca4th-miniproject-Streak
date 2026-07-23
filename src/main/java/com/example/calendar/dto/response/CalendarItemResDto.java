package com.example.calendar.dto.response;

import java.time.LocalDateTime;

// Calendar 세부 조회 응답 
// CalendarMonthResDto 내부에서 사용 
public record CalendarItemResDto(
	String type,	// SESSION or ASSIGNMENT
	Long id,		// sessionId or assignmentId
	Long studyId,
	String studyTitle,	// 스터디 이름 
	String title,		// 회차 제목 or 과제 제목 
	LocalDateTime date	// 회차 시작일시 or 과제 마감일시 
) {}

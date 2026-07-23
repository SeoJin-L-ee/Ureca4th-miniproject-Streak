package com.example.assignment.dto.response;

import java.time.LocalDateTime;

// 과제 목록 조회 시 단일 과제 요약 정보 DTO 
public record AssignmentSummaryResDto(
	Long assignmentId,
	int sessionNumber, 
	String title,
	LocalDateTime dueAt
) {}

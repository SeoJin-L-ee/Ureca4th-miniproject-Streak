package com.example.assignment.dto.response;

import java.time.LocalDateTime;

//특정 스터디 과제의 상세 페이지용 DTO
public record AssignmentInfoResDto(
	Long assignmentId,
	Long sessionId,
	int sessionNumber,
	String title,
	String description,
	LocalDateTime dueAt
) {}
package com.example.assignment.dto.response;

// 회차별 과제 수 카운트에 사용되는 DTO입니다. (제출 과제 수와 함께 제출률 계산에 사용)
public record SessionAssignmentCountDto(
		Long sessionId,
		Long assignmentCount
) {}

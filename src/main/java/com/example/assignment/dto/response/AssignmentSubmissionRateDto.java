package com.example.assignment.dto.response;

// 회차별 과제 제출률을 나타내는 DTO입니다. (AssignmentDashboardDataDto 에서 같이 묶임)
public record AssignmentSubmissionRateDto(
		boolean hasAssignments,
		Double teamSubmissionRate
) {}

package com.example.assignment.dto.response;

import java.time.LocalDateTime;

// 다음 회차의 과제 단건을 의미하는 DTO입니다. (AssignmentDashboardDataDto 에서 같이 묶임)
public record NextSessionAssignmentDto(
		Long assignmentId,
		String title,
		LocalDateTime dueAt,
		Long daysUntilDue,
		boolean submitted
) {}

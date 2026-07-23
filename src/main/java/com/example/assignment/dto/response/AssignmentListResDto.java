package com.example.assignment.dto.response;

import java.util.List;

// 회차별 과제 목록 조회 응답 DTO
public record AssignmentListResDto(
	Long sessionId,
	List<AssignmentSummaryResDto> assignments
) {}

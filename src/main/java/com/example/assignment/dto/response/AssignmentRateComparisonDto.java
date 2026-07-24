package com.example.assignment.dto.response;

// 대시보드 화면의 과제 제출률 비교 그래프에 사용되는 DTO입니다.
public record AssignmentRateComparisonDto(
		Double totalRate,
		Double myRate
) {}

package com.example.attendance.dto.response;

// 대시보드 화면의 출석률 비교 그래프에 사용되는 DTO입니다.
public record AttendanceRateComparisonDto(
		Double totalAverage,
		Double myAverage
) {}

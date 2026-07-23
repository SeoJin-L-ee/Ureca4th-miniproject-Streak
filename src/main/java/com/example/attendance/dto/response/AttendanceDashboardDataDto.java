package com.example.attendance.dto.response;

import java.util.Map;

import com.example.attendance.entity.enums.AttendanceStatus;

// 대시보드 화면에 사용되는 출결 데이터 (비교 그래프 데이터, 회차별 내 출석 여부와 팀원 출석률) 를 묶은 DTO입니다.
public record AttendanceDashboardDataDto(
		AttendanceRateComparisonDto comparison,
		Map<Long, Double> teamAttendanceRateBySessionId,
		Map<Long, AttendanceStatus> myStatusBySessionId
) {}

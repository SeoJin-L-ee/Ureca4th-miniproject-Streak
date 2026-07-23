package com.example.study.dto.response;

// 각 스터디별 전체 출석률 조회 쿼리에서 사용되는 프로젝션 DTO입니다.
public record StudyAttendanceRateDto(
		Long studyId,
		Double attendanceRate
) {}

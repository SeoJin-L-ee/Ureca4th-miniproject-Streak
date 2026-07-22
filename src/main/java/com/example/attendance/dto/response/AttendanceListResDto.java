package com.example.attendance.dto.response;

import java.util.List;

// 스터디에 참여한 모든 member의 출석 정보를 보여주는 DTO 
public record AttendanceListResDto(
	Long studyId,
	
	// 전체 진행 회차 수
	int totalSessionCount,
	
	// 스터디 전체 평균 출석률
	double averageAttendanceRate,
	
	// 참여자별 출석 현황 목록
	List<AttendanceMemberResDto> members
) {}

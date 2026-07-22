package com.example.attendance.dto.response;

// 개별 참여자 정보를 보여주는 응답 DTO - 참여자별 출석 현황 보여주기 위함 
// AttendanceListResDto 내부에서 사용 
public record AttendanceMemberResDto(
	Long memberId,
	String name,
	
	// 출석 횟수 
	int attendedCount,
	
	// 결석 횟수  
	int absentCount,
	
	// 개별 출석률 
	double attendanceRate
) {}
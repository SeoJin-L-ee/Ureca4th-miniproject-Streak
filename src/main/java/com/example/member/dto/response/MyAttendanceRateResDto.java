package com.example.member.dto.response;

//마이페이지 - 평균 출석률
public record MyAttendanceRateResDto(int totalCount, int presentCount, int attendanceRate) {
	
}
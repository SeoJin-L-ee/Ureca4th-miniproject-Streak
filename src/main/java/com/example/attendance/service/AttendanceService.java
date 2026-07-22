package com.example.attendance.service;

import com.example.attendance.dto.response.AttendanceListResDto;

public interface AttendanceService {
	
	// 스터디 내 참여자별 출석 현황 조회 
	AttendanceListResDto getMemberAttendances(long studyId, long memberId);
}

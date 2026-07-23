package com.example.attendance.service;

import com.example.attendance.dto.request.BatchSaveAttendanceReqDto;
import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceSessionResDto;

public interface AttendanceService {
	
	// 스터디 내 참여자별 출석 현황 조회 
	AttendanceListResDto getMemberAttendances(long studyId, long memberId);
	
	// 회차별 참여자 출석 목록 조회 
	AttendanceSessionResDto getSessionAttendances(long studyId, long sessionId, long memberId);
	
	// 참여자 출석 사항 저장 
	void updateSessionAttendances(long studyId, long sessionId, long memberId, BatchSaveAttendanceReqDto reqDto);
}

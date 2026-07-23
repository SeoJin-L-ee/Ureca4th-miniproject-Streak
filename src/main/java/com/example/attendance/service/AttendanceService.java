package com.example.attendance.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.attendance.dto.request.BatchSaveAttendanceReqDto;
import com.example.attendance.dto.response.AttendanceDashboardDataDto;
import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceSessionResDto;

public interface AttendanceService {
	
	// 스터디 내 참여자별 출석 현황 조회 
	AttendanceListResDto getMemberAttendances(long studyId, long memberId);
	
	// 회차별 참여자 출석 목록 조회 
	AttendanceSessionResDto getSessionAttendances(long studyId, long sessionId, long memberId);
	
	// 참여자 출석 사항 저장 
	void updateSessionAttendances(long studyId, long sessionId, long memberId, BatchSaveAttendanceReqDto reqDto);
	
	// 출석률 비교 그래프 데이터 & 회차별 내 출석 여부 & 회차별 전체 출석률 한번에 조회
	AttendanceDashboardDataDto findAttendanceDashboardData(Long memberId, Long studyId, List<Long> sessionIds, LocalDateTime now);
}

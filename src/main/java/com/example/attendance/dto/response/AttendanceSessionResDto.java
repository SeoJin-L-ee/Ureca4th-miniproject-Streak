package com.example.attendance.dto.response;

import java.util.List;

// 회차별 참여자 출석 목록 조회 (출석 체크용 조회) 
public record AttendanceSessionResDto(
	Long sessionId,
	
	// 개별 참여자의 출석 정보
	List<AttendanceParticipantResDto> participants
) {}

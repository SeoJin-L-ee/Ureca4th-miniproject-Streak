package com.example.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.dto.response.AttendanceListResDto;
import com.example.attendance.dto.response.AttendanceSessionResDto;
import com.example.attendance.service.AttendanceService;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;
	
	// 스터디 내 참여자별 출석 현황 조회 - 로직만 필요. 이후에 지우기 
	@GetMapping("/studies/{studyId}/attendances")
	public CustomResponse<AttendanceListResDto> getMemberAttendances(
			@PathVariable("studyId") Integer studyId,
			@CurrentUser MemberPrincipal principal
	){
		AttendanceListResDto resDto = attendanceService.getMemberAttendances(studyId, principal.memberId());
		return CustomResponse.onSuccess(resDto);
	}
	
	
	// 회차별 참여자 출석 목록 조회 - 출석 체크용  
	@GetMapping("/studies/{studyId}/sessions/{sessionId}/attendances")
	public CustomResponse<AttendanceSessionResDto> getSessionAttendances(
			@PathVariable("studyId") Integer studyId,
			@PathVariable("sessionId") Integer sessionId,
			@CurrentUser MemberPrincipal principal
	){
		AttendanceSessionResDto resDto = attendanceService.getSessionAttendances(studyId, sessionId, principal.memberId());
		return CustomResponse.onSuccess(resDto);
	}
}

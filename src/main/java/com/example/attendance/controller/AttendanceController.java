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

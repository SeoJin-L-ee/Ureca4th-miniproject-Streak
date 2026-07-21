package com.example.session.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionDetailResDto;
import com.example.session.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {
	
	private final SessionService sessionService;
	
	// 스터디 회차 생성 
	@PostMapping("/studies/{studyId}/sessions")
	public CustomResponse<SessionDetailResDto> insertSession(
			@PathVariable("studyId") Long studyId, 
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateSessionReqDto reqDto
	){
		return sessionService.createSession(studyId, principal.getMemberId(), reqDto);
	}
	
	// 스터디 회차 수정 
	@PatchMapping("/studies/{studyId}/sessions/{sessionId}")
	public CustomResponse<SessionDetailResDto> updateSession(
			@PathVariable("studyId") Long studyId, 
			@PathVariable("sessionId") Long sessionId,
			@CurrentUser MemberPrincipal principal,
			@RequestBody UpdateSessionReqDto reqDto
	){
		return sessionService.updateSession(studyId, sessionId, principal.getMemberId(), reqDto); 
	}
}

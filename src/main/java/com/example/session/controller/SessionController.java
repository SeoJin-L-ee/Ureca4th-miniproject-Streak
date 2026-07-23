package com.example.session.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.session.dto.response.SessionInfoResDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {
	
	private final SessionService sessionService;
	
	// 스터디 회차 생성 
	@PostMapping("/studies/{studyId}/sessions")
	public CustomResponse<SessionResDto> insertSession(
			@PathVariable("studyId") Long studyId, 
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateSessionReqDto reqDto
	){
		SessionResDto resDto = sessionService.createSession(studyId, principal.memberId(), reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	// 스터디 회차 수정 
	@PatchMapping("/studies/{studyId}/sessions/{sessionId}")
	public CustomResponse<SessionResDto> updateSession(
			@PathVariable("studyId") Long studyId, 
			@PathVariable("sessionId") Long sessionId,
			@CurrentUser MemberPrincipal principal,
			@RequestBody UpdateSessionReqDto reqDto
	){
		SessionResDto resDto = sessionService.updateSession(studyId, sessionId, principal.memberId(), reqDto); 
		return CustomResponse.onSuccess(resDto);
	}
	
	// 스터디 회차 삭제 
	@DeleteMapping("/studies/{studyId}/sessions/{sessionId}")
	public CustomResponse<Void> deleteSession(
			@PathVariable("studyId") Long studyId, 
			@PathVariable("sessionId") Long sessionId,
			@CurrentUser MemberPrincipal principal
	){
		sessionService.deleteSession(studyId, sessionId, principal.memberId());
		return CustomResponse.onSuccess(null);
	}
	
	// 스터디 회차 상세 조회 
	@GetMapping("/studies/{studyId}/sessions/{sessionId}")
	public CustomResponse<SessionInfoResDto> detailSession(
			@PathVariable("studyId") Long studyId, 
			@PathVariable("sessionId") Long sessionId,
			@CurrentUser MemberPrincipal principal
	){
		SessionInfoResDto resDto = sessionService.detailSession(studyId, sessionId, principal.memberId());
		return CustomResponse.onSuccess(resDto);
	}
	
}

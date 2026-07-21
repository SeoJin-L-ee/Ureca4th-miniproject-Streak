package com.example.session.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.response.SessionDetailResDto;
import com.example.session.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {
	
	private final SessionService sessionService;
	
	@PostMapping("/studies/{studyId}/sessions")
	public CustomResponse<SessionDetailResDto> insertSession(
			@PathVariable("studyId") Long studyId, 
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateSessionReqDto reqDto
	){
		return sessionService.createSession(studyId, principal.getMemberId(), reqDto);
	}
}

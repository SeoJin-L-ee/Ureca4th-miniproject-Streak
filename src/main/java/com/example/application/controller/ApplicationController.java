package com.example.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.application.dto.request.CreateApplicationReqDto;
import com.example.application.dto.request.UpdateApplicationStatusReqDto;
import com.example.application.dto.response.ApplicationResDto;
import com.example.application.service.ApplicationCommandService;
import com.example.application.service.ApplicationQueryService;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApplicationController {
	
	private final ApplicationCommandService applicationCommandService;
	private final ApplicationQueryService applicationQueryService;
	
	@PostMapping("/studies/{studyId}/applications")
	// 스터디 지원
	public CustomResponse<ApplicationResDto> createApplication(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@Valid @RequestBody CreateApplicationReqDto reqDto
	) {
		ApplicationResDto resDto = applicationCommandService.createApplication(principal.memberId(), studyId, reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	@PatchMapping("/applications/{applicationId}/status")
	// 지원 수락/거절
	public CustomResponse<ApplicationResDto> updateApplicationStatus(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("applicationId") Long applicationId,
			@Valid @RequestBody UpdateApplicationStatusReqDto reqDto
	) {
		ApplicationResDto resDto = applicationCommandService.updateApplicationStatus(principal.memberId(), applicationId, reqDto);
		return CustomResponse.onSuccess(resDto);
	}
	
	@DeleteMapping("/applications/{applicationId}")
	// 지원 삭제(취소)
	public CustomResponse<Void> deleteMyApplication(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("applicationId") Long applicationId
	) {
		applicationCommandService.deleteMyApplication(principal.memberId(), applicationId);
		return CustomResponse.onSuccess(null);
	}
	
	@GetMapping("/studies/{studyId}/applications")
	// 지원자 목록 조회 (스터디장 전용)
	public CustomResponse<List<ApplicationResDto>> getApplications(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId
	) {
		List<ApplicationResDto> resDtoList = applicationQueryService.getApplications(principal.memberId(), studyId);
        return CustomResponse.onSuccess(resDtoList);
    }
}

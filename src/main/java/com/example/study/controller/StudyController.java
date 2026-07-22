package com.example.study.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.service.StudyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyController {

	private final StudyService studyService;
	
	@PostMapping("")
	// 스터디 생성
	public CustomResponse<StudyInfoResDto> createStudy(
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateStudyReqDto reqDto
	) {
		StudyInfoResDto resDto = studyService.createStudy(principal.memberId(), reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	@PatchMapping("/{studyId}")
	// 스터디 수정
	public CustomResponse<StudyInfoResDto> updateStudy(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@RequestBody UpdateStudyReqDto reqDto
	) {
		StudyInfoResDto resDto = studyService.updateStudy(principal.memberId(), studyId, reqDto);
		return CustomResponse.onSuccess(resDto);
	}
	
	@PatchMapping("/{studyId}/status")
	// 스터디 상태 변경 (모집 중, 모집 완료, 종료됨)
	public CustomResponse<StudyInfoResDto> updateStudyStatus(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@RequestParam("status") StudyStatus status
	) {
		StudyInfoResDto resDto = studyService.updateStudyStatus(principal.memberId(), studyId, status);
		return CustomResponse.onSuccess(resDto);
	}
	
	@DeleteMapping("/{studyId}")
	// 스터디 soft delete (DELETED)
	public CustomResponse<Void> softDeleteStudy(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId
	) {
		studyService.softDeleteStudy(principal.memberId(), studyId);
		return CustomResponse.onSuccess(null);
	}
	
}
package com.example.study.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.study.dto.response.StudySummaryListResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.service.StudyCommandService;
import com.example.study.service.StudyQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyController {

	private final StudyCommandService studyCommandService;
	private final StudyQueryService studyQueryService;
	
	@PostMapping("")
	// 스터디 생성
	public CustomResponse<StudyInfoResDto> createStudy(
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateStudyReqDto reqDto
	) {
		StudyInfoResDto resDto = studyCommandService.createStudy(principal.memberId(), reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	@PatchMapping("/{studyId}")
	// 스터디 수정
	public CustomResponse<StudyInfoResDto> updateStudy(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@RequestBody UpdateStudyReqDto reqDto
	) {
		StudyInfoResDto resDto = studyCommandService.updateStudy(principal.memberId(), studyId, reqDto);
		return CustomResponse.onSuccess(resDto);
	}
	
	@PatchMapping("/{studyId}/status")
	// 스터디 상태 변경 (모집 중, 모집 완료, 종료됨)
	public CustomResponse<StudyInfoResDto> updateStudyStatus(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@RequestParam("status") StudyStatus status
	) {
		StudyInfoResDto resDto = studyCommandService.updateStudyStatus(principal.memberId(), studyId, status);
		return CustomResponse.onSuccess(resDto);
	}

	@PatchMapping("/{studyId}/leader")
	// 스터디장 변경 (위임)
	public CustomResponse<UpdateStudyLeaderResDto> updateStudyLeader(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId,
			@RequestParam("newLeaderId") Long newLeaderId
	) {
		UpdateStudyLeaderResDto resDto = studyCommandService.updateStudyLeader(principal.memberId(), studyId, newLeaderId);
		return CustomResponse.onSuccess(resDto);
	}
	
	@DeleteMapping("/{studyId}")
	// 스터디 soft delete (DELETED)
	public CustomResponse<Void> softDeleteStudy(
			@CurrentUser MemberPrincipal principal,
			@PathVariable("studyId") Long studyId
	) {
		studyCommandService.softDeleteStudy(principal.memberId(), studyId);
		return CustomResponse.onSuccess(null);
	}
	
	@GetMapping("")
	// 사용자별 참여 중인 스터디 목록 조회
	public CustomResponse<StudySummaryListResDto> findStudySummaryList(
			@CurrentUser MemberPrincipal principal,
			// sort 기준이 createdAt == 사용자가 최근에 가입한 스터디부터 보여줌 (다른 정렬 방식은 추후에 고민해볼 것)
			@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		StudySummaryListResDto resDto = studyQueryService.findStudySummaryList(principal.memberId(), pageable);
		return CustomResponse.onSuccess(resDto);
	}
	
}
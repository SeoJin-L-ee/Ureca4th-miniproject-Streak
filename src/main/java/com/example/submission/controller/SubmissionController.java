package com.example.submission.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;
import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.request.UpdateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;
import com.example.submission.service.SubmissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {
	
	private final SubmissionService submissionService;
	
	// 과제 제출 
	@PostMapping("/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}/submissions")
	public CustomResponse<SubmissionSummaryResDto> createSubmission(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId, 
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateSubmissionReqDto reqDto
	){
		SubmissionSummaryResDto resDto = submissionService.createSubmission(studyId, sessionId, assignmentId, principal.memberId(), reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	
	// 제출한 과제 수정 
	@PatchMapping("/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}/submissions/{submissionId}")
	public CustomResponse<SubmissionSummaryResDto> updateSubmission(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId, 
			@PathVariable("submissionId") Long submissionId, 
			@CurrentUser MemberPrincipal principal,
			@RequestBody UpdateSubmissionReqDto reqDto
	){
		SubmissionSummaryResDto resDto = submissionService.updateSubmission(studyId, sessionId, assignmentId, submissionId, principal.memberId(), reqDto);
		return CustomResponse.onSuccess(resDto);
	}
	
	// 제출한 과제 삭제 
	@DeleteMapping("/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}/submissions/{submissionId}")
	public CustomResponse<Void> deleteSubmission(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId, 
			@PathVariable("submissionId") Long submissionId, 
			@CurrentUser MemberPrincipal principal
	){
		submissionService.deleteSubmission(studyId, sessionId, assignmentId, submissionId, principal.memberId());
		return CustomResponse.onSuccess(null);
	}
}

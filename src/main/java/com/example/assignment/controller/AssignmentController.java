package com.example.assignment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.service.AssignmentService;
import com.example.global.common.CustomResponse;
import com.example.global.security.CurrentUser;
import com.example.global.security.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {
	
	private final AssignmentService assignmentService;
	
	// 과제 생성 
	@PostMapping("/studies/{studyId}/sessions/{sessionId}/assignments")
	public CustomResponse<AssignmentInfoResDto> createAssignment(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@CurrentUser MemberPrincipal principal,
			@RequestBody CreateAssignmentReqDto reqDto
	){
		AssignmentInfoResDto resDto = assignmentService.createAssignment(studyId, sessionId, principal.memberId(), reqDto);
		return CustomResponse.onSuccess(HttpStatus.CREATED, resDto);
	}
	
	// 과제 수정 
	@PatchMapping("/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}")
	public CustomResponse<AssignmentInfoResDto> updateAssignment(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId,
			@CurrentUser MemberPrincipal principal,
			@RequestBody UpdateAssignmentReqDto reqDto
	){
		AssignmentInfoResDto resDto = assignmentService.updateAssignment(studyId, sessionId, assignmentId, principal.memberId(), reqDto);  
		return CustomResponse.onSuccess(resDto);
	}
	
	// 과제 삭제 
	@DeleteMapping("/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}")
	public CustomResponse<Void> deleteAssignment(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId,
			@CurrentUser MemberPrincipal principal
	){
		assignmentService.deleteAssignment(studyId, sessionId, assignmentId, principal.memberId());
		return CustomResponse.onSuccess(null);
	}
	
	// 과제 상세 조회 
	@GetMapping("/api/studies/{studyId}/sessions/{sessionId}/assignments/{assignmentId}")
	public CustomResponse<AssignmentInfoResDto> detailAssignment(
			@PathVariable("studyId") Long studyId,
			@PathVariable("sessionId") Long sessionId,
			@PathVariable("assignmentId") Long assignmentId,
			@CurrentUser MemberPrincipal principal
	){
		AssignmentInfoResDto resDto = assignmentService.detailAssignment(studyId, sessionId, assignmentId, principal.memberId());
		return CustomResponse.onSuccess(resDto);
	}
}

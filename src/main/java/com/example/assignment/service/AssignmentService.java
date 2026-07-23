package com.example.assignment.service;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.dto.response.AssignmentListResDto;

public interface AssignmentService {
	
	// 과제 생성 - 스터디장만 가능 
	AssignmentInfoResDto createAssignment(Long studyId, Long sessionId, Long memberId, CreateAssignmentReqDto reqDto);
	
	// 과제 수정 - 스터디장만 가능 
	AssignmentInfoResDto updateAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId, UpdateAssignmentReqDto reqDto);
	
	// 과제 삭제 - 스터디장만 가능 
	void deleteAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId);
	
	// 과제 상세 조회 
	AssignmentInfoResDto detailAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId);
	
	// 과제 목록 조회 
	AssignmentListResDto listAssignment(Long studyId, Long sessionId, Long memberId);
}

package com.example.assignment.service;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;

public interface AssignmentService {
	
	// 과제 생성 - 스터디장만 가능 
	AssignmentInfoResDto createAssignment(Long studyId, Long sessionId, Long memberId, CreateAssignmentReqDto reqDto);  
}

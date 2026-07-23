package com.example.assignment.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentDashboardDataDto;
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
	
	// 과제 제출률 비교 그래프 데이터 & 다음 회차의 과제들 & 회차별 과제 제출률 한번에 조회
	AssignmentDashboardDataDto findAssignmentDashboardData(
			Long memberId,
			Long studyId,
			long currentParticipantCnt,
			Long nextSessionId,
			List<Long> sessionIds,
			LocalDateTime now
	);
}

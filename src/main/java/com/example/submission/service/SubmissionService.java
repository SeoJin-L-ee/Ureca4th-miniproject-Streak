package com.example.submission.service;

import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.request.UpdateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionListResDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;

public interface SubmissionService {
	
	// 과제 제출 
	SubmissionSummaryResDto createSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId, CreateSubmissionReqDto reqDto);

	// 제출한 과제 수정 - 본인만 가능 
	SubmissionSummaryResDto updateSubmission(Long studyId, Long sessionId, Long assignmentId, Long submissionId, Long memberId, UpdateSubmissionReqDto reqDto);

	// 제출한 과제 삭제 - 본인만 가능 
	void deleteSubmission(Long studyId, Long sessionId, Long assignmentId, Long submissionId, Long memberId);
	
	// 과제별 제출 목록 조회 
	SubmissionListResDto listSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId);
}

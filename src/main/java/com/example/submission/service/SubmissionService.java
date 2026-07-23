package com.example.submission.service;

import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;

public interface SubmissionService {
	
	// 과제 제출 
	SubmissionSummaryResDto createSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId, CreateSubmissionReqDto reqDto);
}

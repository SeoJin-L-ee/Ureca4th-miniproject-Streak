package com.example.submission.dto.response;

import java.util.List;

// 특정 스터디 과제 제출 목록 조회 페이지용 DTO
public record SubmissionListResDto(
	Long assignmentId,
	List<SubmissionInfoResDto> submissions
) {}

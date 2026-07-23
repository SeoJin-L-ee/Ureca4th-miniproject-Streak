package com.example.submission.dto.response;

//특정 스터디 과제 제출의 상세 페이지용 DTO
public record SubmissionSummaryResDto(
	Long submissionId,
	Long memberId,
	String content
) {}

package com.example.submission.dto.response;

// 회차별 과제 제출 건수 조회 쿼리에서 사용되는 프로젝션 DTO입니다.
public record SessionSubmissionCountDto(
		Long sessionId,
		Long submissionCount
) {}

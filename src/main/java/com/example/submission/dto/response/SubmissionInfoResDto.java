package com.example.submission.dto.response;

import java.time.LocalDateTime;

// 특정 스터디 과제 제출 목록 조회 페이지용 DTO
// SubmissionListResDto 내부에서 사용 
public record SubmissionInfoResDto(
	Long submissionId,
	Long memberId, 
	String memberName,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	
	// 제출 여부 
	boolean isSubmitted
) {
	
}

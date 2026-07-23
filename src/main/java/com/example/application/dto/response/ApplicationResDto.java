package com.example.application.dto.response;

import com.example.application.entity.enums.ApplicationStatus;

// 지원 정보 + 지원 동기 DTO
public record ApplicationResDto(
		Long applicationId,
		Long studyId,
		Long applicantId,
		String applicantName,
		String content,
		ApplicationStatus status
) {}

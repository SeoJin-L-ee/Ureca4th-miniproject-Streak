package com.example.application.dto.request;

import com.example.application.entity.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

// 지원 상태 변경 요청 시 전달되는 DTO (수락/거절)
public record UpdateApplicationStatusReqDto(
		@NotNull(message = "변경할 지원 상태를 입력해 주세요.")
		ApplicationStatus status
) {}

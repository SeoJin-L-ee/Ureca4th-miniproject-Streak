package com.example.application.dto.request;

import jakarta.validation.constraints.NotBlank;

// 지원 생성 요청 시 전달되는 DTO
public record CreateApplicationReqDto(
		@NotBlank(message = "지원 내용을 입력해 주세요")
		String content
) {}

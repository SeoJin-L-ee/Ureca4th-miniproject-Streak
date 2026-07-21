package com.example.session.dto.response;

import java.time.LocalDateTime;

// 스터디 회차 생성 및 수정 시 응답하는 DTO
public record SessionResDto(
	Long sessionId,
	int sessionNumber,
	String title,
	String content,
	LocalDateTime startsAt
) {}

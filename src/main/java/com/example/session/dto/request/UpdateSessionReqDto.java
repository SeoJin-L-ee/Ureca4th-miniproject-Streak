package com.example.session.dto.request;

import java.time.LocalDateTime;

// 스터디 회차 수정 시 요청하는 DTO
public record UpdateSessionReqDto(
	Integer sessionNumber,	// 수정 안 함과 0 구분 
	String title,
	String content,
	LocalDateTime startsAt
) {}

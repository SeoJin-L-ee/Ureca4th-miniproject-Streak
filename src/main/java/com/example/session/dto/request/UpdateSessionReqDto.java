package com.example.session.dto.request;

import java.time.LocalDateTime;

public record UpdateSessionReqDto(
	Integer sessionNumber,	// 수정 안 함과 0 구분 
	String title,
	String content,
	LocalDateTime startsAt
) {}

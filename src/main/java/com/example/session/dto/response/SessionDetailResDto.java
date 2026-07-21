package com.example.session.dto.response;

import java.time.LocalDateTime;

public record SessionDetailResDto(
	Long sessionId,
	int sessionNumber,
	String title,
	String content,
	LocalDateTime startsAt
) {}

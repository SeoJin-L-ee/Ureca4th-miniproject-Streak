package com.example.session.dto.request;

import java.time.LocalDateTime;

public record CreateSessionReqDto(
	int sessionNumber,
	String title,
	String content,
	LocalDateTime startsAt
) {}

package com.example.session.dto.response;

import java.time.LocalDateTime;

//스터디 회차 목록 조회 시 개별 회차 응답 DTO
public record SessionListResDto(
	long sessionId,
	int sessionNumber,
	LocalDateTime startsAt,
	String title
) {}

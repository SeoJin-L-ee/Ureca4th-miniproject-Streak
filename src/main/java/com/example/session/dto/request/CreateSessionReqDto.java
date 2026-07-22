package com.example.session.dto.request;

import java.time.LocalDateTime;

// 스터디 회차 생성 시 요청하는 DTO 
public record CreateSessionReqDto(
	int sessionNumber,
	String title,
	String content,
	LocalDateTime startsAt
) {}

package com.example.session.dto.response;

import java.time.LocalDateTime;

// 스터디 회차 단건 summary
// (다음 회차 조회 시와, 회차목록 조회 시(다른 데이터와 합쳐져서 사용됨)에 사용)
public record SessionSummaryResDto(
	long sessionId,
	int sessionNumber,
	LocalDateTime startsAt,
	String title
) {}

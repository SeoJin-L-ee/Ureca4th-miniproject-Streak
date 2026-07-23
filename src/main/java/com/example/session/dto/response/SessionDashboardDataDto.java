package com.example.session.dto.response;

import org.springframework.data.domain.Page;

// 대시보드 화면에 사용되는 회차 데이터 (다음 회차, 전체 회차목록) 를 묶은 DTO입니다.
// (SessionService에서 한번에 가져오기 위해 사용)
public record SessionDashboardDataDto(
		SessionSummaryResDto nextSession,
		Page<SessionSummaryResDto> sessions
) {}

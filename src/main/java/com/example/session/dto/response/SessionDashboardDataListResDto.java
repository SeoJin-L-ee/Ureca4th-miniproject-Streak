package com.example.session.dto.response;

import java.util.List;

// MergedSessionDashboardDataDto(특정 회차 정보 + 해당 회차의 내출석여부/출석률/과제제출률)을 Page 형식으로 가지는 DTO입니다.
// 스터디 대시보드 조회 시에 사용됨
public record SessionDashboardDataListResDto(
		int currentPage,
		int pageSize,
		int totalPages,
		long totalElements,
		boolean hasNext,
		List<MergedSessionDashboardDataDto> sessionList
) {}

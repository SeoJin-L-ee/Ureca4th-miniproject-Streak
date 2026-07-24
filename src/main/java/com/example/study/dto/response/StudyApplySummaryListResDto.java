package com.example.study.dto.response;

import java.util.List;

// 스터디 찾기 화면에서 목록 조회 시 사용되는 전체 목록 DTO
public record StudyApplySummaryListResDto(
		int currentPageNum,
		int pageSize,
		int totalPages,
		long totalElements,
		boolean hasNext,
		List<StudyApplySummaryResDto> studyList
) {}

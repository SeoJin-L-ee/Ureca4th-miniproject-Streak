package com.example.study.dto.response;

import java.util.List;

//사용자별 참여 중인 스터디 목록 조회 시 사용되는 전체 목록 DTO입니다.
public record StudySummaryListResDto(
        int currentPageNum,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean hasNext,
		List<StudySummaryResDto> studyList
) {}

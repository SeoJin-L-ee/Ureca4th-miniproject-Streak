package com.example.study.service;

import org.springframework.data.domain.Pageable;

import com.example.study.dto.response.StudySummaryListResDto;

public interface StudyQueryService {
	// 사용자별 참여 중인 스터디 목록 조회
    StudySummaryListResDto findStudySummaryList(Long memberId, Pageable pageable);
}

package com.example.study.service;

import org.springframework.data.domain.Pageable;

import com.example.study.dto.response.StudyDashboardResDto;
import com.example.study.dto.response.StudySummaryListResDto;

public interface StudyQueryService {
	// 사용자별 참여 중인 스터디 목록 조회
    StudySummaryListResDto findStudySummaryList(Long memberId, Pageable pageable);
    
    // 특정 스터디 상세 조회 (대시보드)
    StudyDashboardResDto findStudyDashboard(Long memberId, Long studyId, Pageable pageable);
}

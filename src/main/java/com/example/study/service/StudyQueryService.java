package com.example.study.service;

import org.springframework.data.domain.Pageable;

import com.example.study.dto.response.StudyApplyDetailResDto;
import com.example.study.dto.response.StudyApplySummaryListResDto;
import com.example.study.dto.response.StudyDashboardResDto;
import com.example.study.dto.response.StudySummaryListResDto;
import com.example.study.entity.enums.StudyCategory;

public interface StudyQueryService {
	// 사용자별 참여 중인 스터디 목록 조회
    StudySummaryListResDto findStudySummaryList(Long memberId, Pageable pageable);
    
    // 특정 스터디 상세 조회 (대시보드)
    StudyDashboardResDto findStudyDashboard(Long memberId, Long studyId, Pageable pageable);
    
	// 스터디 탐색 목록 조회 (전체, 카테고리별, 제목 검색)
	StudyApplySummaryListResDto getStudiesForApply(Long memberId, StudyCategory category, String title, Pageable pageable);
	
	// 스터디 상세 조회 (스터디 찾기 화면에서)
	StudyApplyDetailResDto getStudyDetailForApply(Long memberId, Long studyId);
	
}

package com.example.application.service;

import java.util.List;

import com.example.application.dto.response.ApplicationResDto;

public interface ApplicationQueryService {
	// 지원자 목록 조회 (스터디장 전용)
	List<ApplicationResDto> getApplications(Long memberId, Long studyId);
}

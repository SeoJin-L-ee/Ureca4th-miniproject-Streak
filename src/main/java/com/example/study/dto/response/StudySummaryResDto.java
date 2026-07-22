package com.example.study.dto.response;

import com.example.study.entity.enums.StudyCategory;

// 사용자별 참여 중인 스터디 목록 조회 시 사용되는 단건 DTO입니다. (StudySummaryListResDto에 리스트로 포함됨)
public record StudySummaryResDto(
		Long studyId,
		String title,
		StudyCategory category,
		String nextSessionInfo,
		boolean isLeader,
		Double attendanceRate
) {}

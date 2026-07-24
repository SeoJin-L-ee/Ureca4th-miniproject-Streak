package com.example.study.dto.response;

import java.time.LocalDateTime;

import com.example.study.dto.response.StudyApplySummaryResDto.MyStudyStatus;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

// 스터디 찾기 화면에서 단건 상세 조회 시 사용되는 DTO
public record StudyApplyDetailResDto(
		Long studyId,
		String title,
		Long leaderId,
		String leaderName,
		String description,
		
		StudyCategory category,
		StudyStatus status,
		MyStudyStatus myStatus,
		
		int currentParticipantCnt,
		int capacity,
		boolean isLeader, // 현재 유저가 스터디장인지
		LocalDateTime createdAt
) {}

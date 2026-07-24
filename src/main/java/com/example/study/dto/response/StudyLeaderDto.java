package com.example.study.dto.response;

// 여러 스터디의 스터디장 정보를 한번에 조회할 때 사용하는 프로젝션 DTO
public record StudyLeaderDto(
		Long studyId,
		String leaderName
) {}

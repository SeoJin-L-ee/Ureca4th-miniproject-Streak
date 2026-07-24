package com.example.study.dto.response;

// 여러 스터디의 참여자 수를 한 번에 계산할 때 사용되는 프로젝션 DTO
public record StudyParticipantCountDto(
		Long studyId,
		Long count
) {}

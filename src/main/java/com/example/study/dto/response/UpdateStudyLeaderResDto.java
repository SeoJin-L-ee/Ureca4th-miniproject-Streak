package com.example.study.dto.response;

// 스터디장 변경 시 반환되는 간단한 DTO입니다.
public record UpdateStudyLeaderResDto(
		Long studyId,
		Long newLeaderId, // memberId임. not participantId
		String newLeaderName
) {}

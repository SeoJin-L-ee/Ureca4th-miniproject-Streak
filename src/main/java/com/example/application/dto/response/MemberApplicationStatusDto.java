package com.example.application.dto.response;

import com.example.application.entity.enums.ApplicationStatus;

// 스터디 목록 조회를 위해 현재 사용자의 지원 상태를 조회하는 로직에서 사용되는 프로젝션 DTO
public record MemberApplicationStatusDto(
		Long studyId,
		ApplicationStatus status
) {}

package com.example.study.dto.request;

import com.example.study.entity.enums.StudyCategory;

// 스터디 수정 요청에 사용되는 DTO입니다.
public record UpdateStudyReqDto(
		String title,
		String description,
		int capacity,
		StudyCategory category
) {}

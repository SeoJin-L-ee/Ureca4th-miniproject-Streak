package com.example.study.dto.request;

import com.example.study.entity.enums.StudyCategory;

// 스터디 생성 요청에 사용되는 DTO입니다.
public record CreateStudyReqDto(
		String title,
		String description,
		int capacity,
		StudyCategory category
) {}

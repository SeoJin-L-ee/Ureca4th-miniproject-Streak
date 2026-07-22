package com.example.study.dto.response;

import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

// 특정 스터디의 상세 페이지용 DTO입니다.
public record StudyInfoResDto(
		String title,
		String description,
		int capacity,
		StudyCategory category,
		StudyStatus studyStatus
) {}

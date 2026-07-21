package com.example.study.converter;

import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyStatus;

public class StudyConverter {
	
	// CreateStudyReqDto -> Study
	public static Study toStudy(CreateStudyReqDto reqDto) {
		return Study.builder()
				.title(reqDto.title())
				.description(reqDto.description())
				.capacity(reqDto.capacity())
				.category(reqDto.category())
				.status(StudyStatus.RECRUITING)
				.isDeleted(false)
				.build();
	}
	
	// Study -> StudyInfoResDto
	public static StudyInfoResDto toStudyInfoResDto(Study study) {
		return new StudyInfoResDto(
				study.getTitle(),
				study.getDescription(),
				study.getCapacity(),
				study.getCategory(),
				study.getStatus()
		);
	}

}

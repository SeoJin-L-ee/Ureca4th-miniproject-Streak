package com.example.application.converter;

import com.example.application.dto.response.ApplicationResDto;
import com.example.application.entity.Application;

public class ApplicationConverter {
	
	// Application -> ApplicationResDto
	public static ApplicationResDto toApplicationResDto(Application application) {
		return new ApplicationResDto(
				application.getId(),
				application.getStudy().getId(),
				application.getApplicant().getId(),
				application.getApplicant().getName(),
				application.getContent(),
				application.getStatus(),
				application.getCreatedAt());
	}

}

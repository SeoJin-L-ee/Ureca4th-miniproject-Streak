package com.example.assignment.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//스터디 과제 생성 시 요청하는 DTO 
public record CreateAssignmentReqDto(
		
	@NotBlank(message = "과제 제목은 필수 입력 항목입니다.")
	String title,
	
	@NotBlank(message = "과제 설명은 필수 입력 항목입니다.")
	String description,
	
	@NotNull(message = "마감 일시는 필수 입력 항목입니다.")
	LocalDateTime dueAt
) {}

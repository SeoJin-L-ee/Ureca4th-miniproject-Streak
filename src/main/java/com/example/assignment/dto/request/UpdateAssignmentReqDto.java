package com.example.assignment.dto.request;

import java.time.LocalDateTime;

//스터디 과제 수정 시 요청하는 DTO 
public record UpdateAssignmentReqDto(
	String title,
	String description,
	LocalDateTime dueAt
) {}

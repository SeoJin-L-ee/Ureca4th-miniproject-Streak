package com.example.assignment.converter;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.entity.Assignment;
import com.example.session.entity.Session;

public class AssignmentConverter {
	
	// CreateAssignmentReqDto 을 Assignment 엔티티로 변경 
	public static Assignment toAssignment(Session session, CreateAssignmentReqDto reqDto) {
		return Assignment.builder()
				.session(session)
				.title(reqDto.title())
				.description(reqDto.description())
				.dueAt(reqDto.dueAt())
				.build();
					
	}
	
	
	// Assignment 엔티티를 AssignmentInfoResDto로 변경 
	public static AssignmentInfoResDto toAssignmentInfoResDto(Assignment assignment) {
		return new AssignmentInfoResDto(
				assignment.getId(),
				assignment.getSession().getId(),
				assignment.getSession().getSessionNumber(),
				assignment.getTitle(),
				assignment.getDescription(),
				assignment.getDueAt()
		);
	}
}

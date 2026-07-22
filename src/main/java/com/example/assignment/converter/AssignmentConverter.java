package com.example.assignment.converter;

import java.util.List;

import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.dto.response.AssignmentListResDto;
import com.example.assignment.dto.response.AssignmentSummaryResDto;
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
	
	
	// Assignment 엔티티를 AssignmentSummaryResDto로 변경 
	public static AssignmentSummaryResDto toAssignmentSummaryResDto(Assignment assignment) {
		return new AssignmentSummaryResDto(
				assignment.getId(),
				assignment.getSession().getSessionNumber(),
				assignment.getTitle(),
				assignment.getDueAt()
		);
	}
	
	// Assignment 엔티티 리스트를 AssignmentListResDto로 변경 
	public static AssignmentListResDto toAssignmentListResDto(Long sessionId, List<Assignment> assignments) {
		List<AssignmentSummaryResDto> summaryList = assignments.stream()
				.map(AssignmentConverter::toAssignmentSummaryResDto)
				.toList();
		
		return new AssignmentListResDto(sessionId, summaryList);
	}
}

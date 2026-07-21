package com.example.session.converter;

import com.example.assignment.entity.Assignment;
import com.example.session.dto.response.SessionAssignmentResDto;


public class SessionAssignmentConverter {

	// 회차 상세 조회 시 과제 정보를 응답 DTO로 변환 
	public static SessionAssignmentResDto toSessionAssignmentResDto(Assignment assignment, boolean isSubmitted) {
		return new SessionAssignmentResDto(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDueAt(), 
                isSubmitted 	// 현재 로그인한 사용자의 과제 제출 여부 
        );
	}
}

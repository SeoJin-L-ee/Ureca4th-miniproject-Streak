package com.example.session.converter;

import java.util.List;

import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.response.SessionAssignmentResDto;
import com.example.session.dto.response.SessionAttendanceResDto;
import com.example.session.dto.response.SessionInfoResDto;
import com.example.session.dto.response.SessionListResDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.entity.Session;
import com.example.study.entity.Study;

public class SessionConverter {
	
	// CreateSessionReqDto -> Session 
	public static Session toSession(CreateSessionReqDto dto, Study study) {
		return Session.builder()
				.study(study)
				.sessionNumber(dto.sessionNumber())
				.title(dto.title())
				.content(dto.content())
				.startsAt(dto.startsAt())
				.build();
	}
	
	
	// Session -> SessionDetailResDto
	public static SessionResDto toDetailResDto(Session session) {
		return new SessionResDto(
				session.getId(),
				session.getSessionNumber(),
				session.getTitle(),
				session.getContent(),
				session.getStartsAt()
		);
	}
	
	
	// SessionInfoResDTO를 생성하는 Converter 
	public static SessionInfoResDto toSessionInfoResDto(
			Session session,
			List<SessionAssignmentResDto> assignments,
            List<SessionAttendanceResDto> attendances,
            int attendanceRate, 
            int assignmentRate
	) {
		return new SessionInfoResDto(
				session.getId(),
				session.getSessionNumber(),
				session.getTitle(),
				session.getContent(),
				session.getStartsAt(),
				assignments,
				attendances,
				attendanceRate,
				assignmentRate
		);
				
	}
	
	// 스터디 회차 목록 조회 응답 DTO 변환 (Session -> SessionListResDto)
	public static SessionListResDto toSessionListResDto(Session session) {
		return new SessionListResDto(
                session.getId(),
                session.getSessionNumber(),
                session.getStartsAt(),
                session.getTitle()
        );
	}
}

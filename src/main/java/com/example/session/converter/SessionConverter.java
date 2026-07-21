package com.example.session.converter;

import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.response.SessionDetailResDto;
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
	public static SessionDetailResDto toDetailResDto(Session session) {
		return new SessionDetailResDto(
				session.getId(),
				session.getSessionNumber(),
				session.getTitle(),
				session.getContent(),
				session.getStartsAt()
		);
	}
}

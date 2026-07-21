package com.example.session.service;

import org.springframework.stereotype.Service;

import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.converter.SessionConverter;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionResDto;
import com.example.session.entity.Session;
import com.example.session.exception.code.SessionErrorCode;
import com.example.session.repository.SessionRepository;
import com.example.study.entity.Study;
import com.example.study.exception.code.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
	
	private final SessionRepository sessionRepository;
	private final StudyRepository studyRepository;
	private final ParticipantRepository participantRepository;
	
	
	// 스터디 회차 생성 
	@Override
	public SessionResDto createSession(long studyId, long memberId, CreateSessionReqDto reqDto) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
			
		Session session = SessionConverter.toSession(reqDto, study);
		Session savedSession = sessionRepository.save(session);
		
		return SessionConverter.toDetailResDto(savedSession);
	}

	
	// 스터디 회차 수정 
	@Override
	public SessionResDto updateSession(long studyId, long sessionId, long memberId, UpdateSessionReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증 
		if(!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.SESSION_NOT_FOUND));
		
		if(reqDto.sessionNumber() != null && session.getSessionNumber() != reqDto.sessionNumber()) {
			if(sessionRepository.existsByStudyIdAndSessionNumber(studyId, reqDto.sessionNumber())) {
				throw new GeneralException(SessionErrorCode.DUPLICATE_SESSION_NUMBER);
			}
		}
		
		session.updateSession(reqDto);
		
		return SessionConverter.toDetailResDto(session);
	}
	
}

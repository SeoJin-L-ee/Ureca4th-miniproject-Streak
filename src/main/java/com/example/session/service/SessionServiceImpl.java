package com.example.session.service;

import org.springframework.stereotype.Service;

import com.example.global.common.CustomResponse;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.code.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.converter.SessionConverter;
import com.example.session.dto.request.CreateSessionReqDto;
import com.example.session.dto.request.UpdateSessionReqDto;
import com.example.session.dto.response.SessionDetailResDto;
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
	
	// 회차 생성 
	@Override
	public CustomResponse<SessionDetailResDto> createSession(long studyId, long memberId, CreateSessionReqDto reqDto) {
		Study study = studyRepository.findById(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		
		
		Participant participant = participantRepository.findByStudyIdAndMemberId(studyId, memberId)
				.orElseThrow(()-> new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND));
		
		if(participant.getRole() != StudyRole.LEADER) {
			throw new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_AUTHORIZED);
		}
			
		Session session = SessionConverter.toSession(reqDto, study);
		Session savedSession = sessionRepository.save(session);
		
		return CustomResponse.onSuccess(SessionConverter.toDetailResDto(savedSession));
	}

	// 회차 수정 
	@Override
	public CustomResponse<SessionDetailResDto> updateSession(long studyId, long sessionId, long memberId, UpdateSessionReqDto reqDto) {
		
		validateLeader(studyId, memberId);
		
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.SESSION_NOT_FOUND));
		
		if(reqDto.sessionNumber() != null && session.getSessionNumber() != reqDto.sessionNumber()) {
			if(sessionRepository.existsByStudyIdAndSessionNumber(studyId, reqDto.sessionNumber())) {
				throw new GeneralException(SessionErrorCode.DUPLICATE_SESSION_NUMBER);
			}
		}
		
		session.update(
	            reqDto.sessionNumber(),
	            reqDto.title(),
	            reqDto.content(),
	            reqDto.startsAt()
	    );
		
		return CustomResponse.onSuccess(SessionConverter.toDetailResDto(session));
	}
	
	
	// 리더 권한 검증 
	private void validateLeader(long studyId, long memberId) {
		Participant participant = participantRepository.findByStudyIdAndMemberId(studyId, memberId)
				.orElseThrow(()-> new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_FOUND));
		
		if(participant.getRole() != StudyRole.LEADER) {
			throw new GeneralException(ParticipantErrorCode.PARTICIPANT_NOT_AUTHORIZED);
		}
	}
}

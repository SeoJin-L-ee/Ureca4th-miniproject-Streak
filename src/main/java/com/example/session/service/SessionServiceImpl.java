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
import com.example.session.dto.response.SessionDetailResDto;
import com.example.session.entity.Session;
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


}

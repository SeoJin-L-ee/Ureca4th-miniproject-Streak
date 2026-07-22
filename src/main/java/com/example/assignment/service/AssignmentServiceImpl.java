package com.example.assignment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.assignment.converter.AssignmentConverter;
import com.example.assignment.dto.request.CreateAssignmentReqDto;
import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.assignment.dto.response.AssignmentInfoResDto;
import com.example.assignment.entity.Assignment;
import com.example.assignment.exception.AssignmentErrorCode;
import com.example.assignment.repository.AssignmentRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.repository.ParticipantRepository;
import com.example.session.entity.Session;
import com.example.session.exception.code.SessionErrorCode;
import com.example.session.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
	
	private final AssignmentRepository assignmentRepository;
	private final ParticipantRepository participantRepository;
	private final SessionRepository sessionRepository;
	
	// 스터디장만 과제 생성 가능 
	@Override
	@Transactional
	public AssignmentInfoResDto createAssignment(Long studyId, Long sessionId, Long memberId, CreateAssignmentReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}

		// 해당 스터디에 속한 회차 존재 여부 검증 
		Session session = sessionRepository.findByIdAndStudyId(sessionId, studyId)
				.orElseThrow(() -> new GeneralException(SessionErrorCode.NOT_STUDY_SESSION));
		
		Assignment assignment = AssignmentConverter.toAssignment(session, reqDto);
		Assignment savedAssignment = assignmentRepository.save(assignment);
		
		return AssignmentConverter.toAssignmentInfoResDto(savedAssignment);
	}

	// 스터디장만 과제 수정 가능 
	@Override
	@Transactional
	public AssignmentInfoResDto updateAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId, UpdateAssignmentReqDto reqDto) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
		
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		assignment.updateAssignment(reqDto);
		
		return AssignmentConverter.toAssignmentInfoResDto(assignment);
	}

	
	// 스터디장만 과제 삭제 가능 
	@Override
	@Transactional
	public void deleteAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId) {
		
		// LEADER 로 등록된 Member만 스터디 회차를 생성할 수 있도록 검증
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
				
		// 회차가 존재하고, 그 회차에 속해있는 과제인지 검증 
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		assignmentRepository.delete(assignment);
	}

	// 과제 상세 조회 - 해당 스터디 참여자만 조회 가능 
	@Override
	public AssignmentInfoResDto detailAssignment(Long studyId, Long sessionId, Long assignmentId, Long memberId) {
		
		// 해당 Study 에 참여한 Member만 스터디 회차를 조회할 수 있도록 검증 
		if (!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(CommonErrorCode.FORBIDDEN);
		}
		
		// 해당 회차가 해당 스터디 소속인지 검증 
		if(!sessionRepository.existsByIdAndStudyId(sessionId, studyId)) throw new GeneralException(SessionErrorCode.NOT_STUDY_SESSION);
			
		// 과제 존재 및 해당 회차 소속 여부 검증 
		Assignment assignment = assignmentRepository.findByIdAndSessionId(assignmentId, sessionId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		return AssignmentConverter.toAssignmentInfoResDto(assignment);
	}
	
}

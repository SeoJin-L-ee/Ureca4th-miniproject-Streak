package com.example.submission.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.assignment.entity.Assignment;
import com.example.assignment.exception.AssignmentErrorCode;
import com.example.assignment.repository.AssignmentRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.repository.ParticipantRepository;
import com.example.submission.converter.SubmissionConverter;
import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.request.UpdateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionListResDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;
import com.example.submission.entity.Submission;
import com.example.submission.exception.SubmissionErrorCode;
import com.example.submission.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
	
	private final SubmissionRepository submissionRepository;
	private final ParticipantRepository participantRepository;
	private final AssignmentRepository assignmentRepository;
	private final MemberRepository memberRepository;
	
	// 과제 제출 - 해당 스터디 참여자만 조회 가능 
	@Override
	@Transactional
	public SubmissionSummaryResDto createSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId, CreateSubmissionReqDto reqDto) {
		
		// 해당 Study에 참여한 Member인지 검증 
		validateParticipant(studyId, memberId);
		validateAssignment(studyId, sessionId, assignmentId);

		if (submissionRepository.existsByAssignmentIdAndMemberId(assignmentId, memberId)) {
	        throw new GeneralException(SubmissionErrorCode.DUPLICATE_SUBMISSION);
	    }

		// DB 조회 없이 프록시 객체 참조
		Assignment assignment = assignmentRepository.getReferenceById(assignmentId); 
	    Member member = memberRepository.getReferenceById(memberId);
		
		// 이미 제출했는지 중복 제출 검증 
	    Submission submission = SubmissionConverter.toSubmission(assignment, member, reqDto);
	    Submission saved = submissionRepository.save(submission);
		
		return SubmissionConverter.toSubmissionSummaryResDto(saved);
		
	}

	// 제출한 과제 수정 - 본인만 가능 
	@Override
	@Transactional
	public SubmissionSummaryResDto updateSubmission(Long studyId, Long sessionId, Long assignmentId, Long submissionId, Long memberId, UpdateSubmissionReqDto reqDto) {
		
		// 해당 Study에 참여한 Member인지 검증
		validateParticipant(studyId, memberId);
		
		// 과제 존재 및 해당 스터디의 과제인지 검증
		validateAssignment(studyId, sessionId, assignmentId);
		
		
		// 제출물 존재 여부 검증 
		Submission submission = getOwnedSubmission(submissionId, assignmentId, memberId);
		submission.updateContent(reqDto.content());
		
		return SubmissionConverter.toSubmissionSummaryResDto(submission);
	}

	
	// 제출한 과제 삭제 - 본인만 가능 
	@Override
	@Transactional
	public void deleteSubmission(Long studyId, Long sessionId, Long assignmentId, Long submissionId, Long memberId) {
		
		// 해당 Study에 참여한 Member인지 검증
		validateParticipant(studyId, memberId);

		// 과제 존재 및 해당 스터디의 과제인지 검증
		validateAssignment(studyId, sessionId, assignmentId);

		// 제출물 존재 여부 검증
		// 제출물이 요청된 과제의 제출물이 맞는지 검증 
		Submission submission = getOwnedSubmission(submissionId, assignmentId, memberId);
		
		submissionRepository.delete(submission);
		
	}

	// 과제별 제출 목록 조회
	// open-in-view=false 환경에서 participant/submission의 member 지연 로딩 접근을 위해 트랜잭션 범위가 필요함
	@Transactional(readOnly = true)
	@Override
	public SubmissionListResDto listSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId) {
		
		// 해당 Study에 참여한 Member인지 검증
		validateParticipant(studyId, memberId);

		// 과제 존재 및 해당 스터디의 과제인지 검증
		validateAssignment(studyId, sessionId, assignmentId);
		
		List<Participant> participants = participantRepository.findAllByStudyIdFetchJoinMember(studyId);
		List<Submission> submissions = submissionRepository.findAllByAssignmentId(assignmentId);

		return SubmissionConverter.toSubmissionListResDto(assignmentId, participants, submissions);
	}
	
	
	// 해당 Study에 참여한 Member인지 검증 
	private void validateParticipant(Long studyId, Long memberId) {
		
	    if (!participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
	        throw new GeneralException(CommonErrorCode.FORBIDDEN);
	    }
	}
	
	// 과제 존재 및 해당 스터디의 과제인지 검증
	private void validateAssignment(Long studyId, Long sessionId, Long assignmentId) {
		
		if (!assignmentRepository.existsById(assignmentId)) {
			throw new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND);
		}
		
		if (!assignmentRepository.existsByIdAndSessionIdAndSessionStudyId(assignmentId, sessionId, studyId)) {
			throw new GeneralException(AssignmentErrorCode.NOT_STUDY_ASSIGNMENT);
		}
	}

	// Submission 존재 및 소유자 검증 
	private Submission getOwnedSubmission(Long submissionId, Long assignmentId, Long memberId) {
		
	    Submission submission = submissionRepository.findById(submissionId)
	            .orElseThrow(() -> new GeneralException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

	    if (!submission.getAssignment().getId().equals(assignmentId)) {
	        throw new GeneralException(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
	    }

	    if (!submission.getMember().getId().equals(memberId)) {
	        throw new GeneralException(SubmissionErrorCode.NOT_SUBMISSION_OWNER);
	    }

	    return submission;
	}

}

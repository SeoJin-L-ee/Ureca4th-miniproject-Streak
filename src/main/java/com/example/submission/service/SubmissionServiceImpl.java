package com.example.submission.service;

import org.springframework.stereotype.Service;

import com.example.assignment.entity.Assignment;
import com.example.assignment.exception.AssignmentErrorCode;
import com.example.assignment.repository.AssignmentRepository;
import com.example.global.common.code.CommonErrorCode;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.participant.repository.ParticipantRepository;
import com.example.submission.converter.SubmissionConverter;
import com.example.submission.dto.request.CreateSubmissionReqDto;
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
	
	// 과제 제출 - 해당 스터디 참여자만 조회 가능 
	@Override
	public SubmissionSummaryResDto createSubmission(Long studyId, Long sessionId, Long assignmentId, Long memberId, CreateSubmissionReqDto reqDto) {
		
		// 해당 Study에 참여한 Member인지 검증 
		Participant participant = participantRepository.findByStudyIdAndMemberId(studyId, memberId)
				.orElseThrow(() -> new GeneralException(CommonErrorCode.FORBIDDEN));

		Member member = participant.getMember();

		// 과제 존재 및 해당 스터디의 과제인지 검증
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new GeneralException(AssignmentErrorCode.ASSIGNMENT_NOT_FOUND));
		
		if(!assignment.getSession().getStudy().getId().equals(studyId))
			throw new GeneralException(AssignmentErrorCode.NOT_STUDY_ASSIGNMENT);
		
		
		// 이미 제출했는지 중복 제출 검증 
		if(submissionRepository.existsByAssignmentIdAndMemberId(assignmentId, memberId))
			throw new GeneralException(SubmissionErrorCode.DUPLICATE_SUBMISSION);

		
		Submission submission = SubmissionConverter.toSubmission(assignment, member, reqDto);
		Submission savedSubmission = submissionRepository.save(submission);
		
		return SubmissionConverter.toSubmissionSummaryResDto(savedSubmission);
		
	}

}

package com.example.submission.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.assignment.entity.Assignment;
import com.example.member.entity.Member;
import com.example.participant.entity.Participant;
import com.example.submission.dto.request.CreateSubmissionReqDto;
import com.example.submission.dto.response.SubmissionInfoResDto;
import com.example.submission.dto.response.SubmissionListResDto;
import com.example.submission.dto.response.SubmissionSummaryResDto;
import com.example.submission.entity.Submission;

public class SubmissionConverter {
	
	// CreateSubmissionReqDto 엔티티를 Submission로 변환 
	public static Submission toSubmission(Assignment assignment, Member member, CreateSubmissionReqDto reqDto) {
		return Submission.builder()
				.assignment(assignment)
				.member(member)
				.content(reqDto.content())
				.build();
	}
	
	// Submission 엔티티를 SubmissionSummaryResDto로 변환 
	public static SubmissionSummaryResDto toSubmissionSummaryResDto(Submission submission) {
		return new SubmissionSummaryResDto(
				submission.getId(),
				submission.getMember().getId(),
				submission.getContent()
		);
	}
	
	// 제출 완료된 사용자 DTO 변환 
	public static SubmissionInfoResDto toSubmissionInfoResDto(Submission submission) {
		return new SubmissionInfoResDto(
				submission.getId(),
				submission.getMember().getId(),
				submission.getMember().getName(),
				submission.getContent(),
				submission.getCreatedAt(),
				submission.getUpdatedAt(),
				true
		);
	}
	
	// 미제출 사용자 DTO 변환 
	public static SubmissionInfoResDto toUnsubmissionInfoResDto(Member member) {
		return new SubmissionInfoResDto(
				null,
				member.getId(),
				member.getName(),
				null,
				null,
				null,
				false
		);
	}
	
	// 과제 제출 목록 조회 
	public static SubmissionListResDto toSubmissionListResDto(Long assignmentId, List<Participant> participants, List<Submission> submissions) {
		
		Map<Long, Submission> submissionMap = submissions.stream()
				.collect(Collectors.toMap(s -> s.getMember().getId(), s -> s));
		
		List<SubmissionInfoResDto> infoList = participants.stream()
					.map(participant -> {
						Member member = participant.getMember();
						Submission submission = submissionMap.get(member.getId());
						
						if(submission != null) return toSubmissionInfoResDto(submission);
						else return toUnsubmissionInfoResDto(member);
					})
					.toList();
		
		return new SubmissionListResDto(assignmentId, infoList);
	}
}

package com.example.submission.converter;

import com.example.assignment.entity.Assignment;
import com.example.member.entity.Member;
import com.example.submission.dto.request.CreateSubmissionReqDto;
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
}

package com.example.application.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.converter.ApplicationConverter;
import com.example.application.dto.request.CreateApplicationReqDto;
import com.example.application.dto.request.UpdateApplicationStatusReqDto;
import com.example.application.dto.response.ApplicationResDto;
import com.example.application.entity.Application;
import com.example.application.entity.enums.ApplicationStatus;
import com.example.application.exception.ApplicationErrorCode;
import com.example.application.repository.ApplicationRepository;
import com.example.global.common.exception.GeneralException;
import com.example.member.entity.Member;
import com.example.member.exception.code.MemberErrorCode;
import com.example.member.repository.MemberRepository;
import com.example.participant.entity.Participant;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.study.entity.Study;
import com.example.study.entity.enums.StudyStatus;
import com.example.study.exception.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationCommandServiceImpl implements ApplicationCommandService {
	
	private final ApplicationRepository applicationRepository;
	private final MemberRepository memberRepository;
	private final StudyRepository studyRepository;
	private final ParticipantRepository participantRepository;
	
	@Override
	@Transactional
	// 스터디 지원
	public ApplicationResDto createApplication(Long memberId, Long studyId, CreateApplicationReqDto request) {
		
		Study study = studyRepository.findByIdAndIsDeletedFalse(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		// 모집 중인 스터디에만 지원 가능
		if (study.getStatus() != StudyStatus.RECRUITING) throw new GeneralException(StudyErrorCode.STUDY_NOT_RECRUITING);
		
		Member applicant = memberRepository.findById(memberId)
				.orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
		
		// 이미 해당 스터디의 참여자인 경우(스터디장 포함) 지원 불가능
		if (participantRepository.existsByStudyIdAndMemberId(studyId, memberId)) {
			throw new GeneralException(ParticipantErrorCode.ALREADY_STUDY_MEMBER);
		}
		// application 테이블에서 memberId와 studyId 조합은 유니크함
		applicationRepository.findByApplicantIdAndStudyId(memberId, studyId)
							 .ifPresent(a -> {
								 if (a.getStatus() == ApplicationStatus.PENDING) {
									 throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_PENDING);
								 }
								 // PENDING 이 아니라 승인/거절 이력이어도 아무튼 안됨
								 throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_EXISTS);
							 });
		
		Application application = Application.builder()
											 .applicant(applicant)
											 .study(study)
											 .content(request.content())
											 .status(ApplicationStatus.PENDING)
											 .build();
		
		// DB의 unique 제약을 어길 경우 DataIntegrityViolationException 에러 발생한다고 함
		try {
			Application saved = applicationRepository.saveAndFlush(application);
			return ApplicationConverter.toApplicationResDto(saved);
		} catch (DataIntegrityViolationException e) {
			throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_EXISTS);
		}
	}

	@Override
	@Transactional
	// 지원 수락/거절
	public ApplicationResDto updateApplicationStatus(Long memberId, Long applicationId, UpdateApplicationStatusReqDto request) {
		ApplicationStatus requestStatus = request.status();
		if (requestStatus == ApplicationStatus.PENDING) throw new GeneralException(ApplicationErrorCode.INVALID_UPDATE_STATUS);
		
		Application application = applicationRepository.findByIdForStatusUpdate(applicationId)
				.orElseThrow(() -> new GeneralException(ApplicationErrorCode.APPLICATION_NOT_FOUND));
		
		// 지원 내역에 해당하는 스터디의 스터디장만 수락/거절 가능
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(application.getStudy().getId(), memberId, StudyRole.LEADER)) {
			throw new GeneralException(ParticipantErrorCode.NOT_STUDY_LEADER);
		}
		// PENDING 이 아닌 다른 상태는 처리할 수 없음
		if (application.getStatus() != ApplicationStatus.PENDING) {
			throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_PROCESSED);
		}
		// 거절 시 처리
		if (requestStatus == ApplicationStatus.REJECTED) {
			application.updateStatus(ApplicationStatus.REJECTED);
			return ApplicationConverter.toApplicationResDto(application);
		}
		// 수락 시 처리
		return approve(application);
	}

	@Override
	@Transactional
	// 지원 삭제(취소)
	public void deleteMyApplication(Long memberId, Long applicationId) {
		Application application = applicationRepository.findMineByIdForStatusUpdate(applicationId, memberId)
				.orElseThrow(() -> new GeneralException(ApplicationErrorCode.APPLICATION_NOT_FOUND));
		// PENDING 이 아닌 다른 상태는 삭제할 수 없음
		if (application.getStatus() != ApplicationStatus.PENDING) {
			throw new GeneralException(ApplicationErrorCode.APPLICATION_ALREADY_PROCESSED);
		}
		applicationRepository.delete(application);
	}
	
	// 지원 수락 시 처리
	private ApplicationResDto approve(Application application) {
		Long studyId = application.getStudy().getId();
		Long applicantId = application.getApplicant().getId();
		
		Study study = studyRepository.findByIdForUpdate(studyId)
				.orElseThrow(() -> new GeneralException(StudyErrorCode.STUDY_NOT_FOUND));
		// 수락하려고 보니까 지원자가 이미 해당 스터디원인 경우 (물론 지원 로직에서 걸러지겠지만 그래도)
		if (participantRepository.existsByStudyIdAndMemberId(studyId, applicantId)) {
			throw new GeneralException(ParticipantErrorCode.ALREADY_STUDY_MEMBER);
		}
		// 현재 스터디 참여자가 이미 정원을 초과했을 경우
		if (participantRepository.countByStudyId(studyId) >= study.getCapacity()) {
			throw new GeneralException(StudyErrorCode.STUDY_CAPACITY_FULL);
		}
		Participant participant = Participant.builder()
											 .study(study)
											 .member(application.getApplicant())
											 .role(StudyRole.MEMBER)
											 .build();
		
		participantRepository.save(participant);
		application.updateStatus(ApplicationStatus.APPROVED);
		return ApplicationConverter.toApplicationResDto(application);
	}
	
}

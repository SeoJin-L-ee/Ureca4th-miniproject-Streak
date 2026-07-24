package com.example.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.converter.ApplicationConverter;
import com.example.application.dto.response.ApplicationResDto;
import com.example.application.entity.Application;
import com.example.application.repository.ApplicationRepository;
import com.example.global.common.exception.GeneralException;
import com.example.participant.entity.enums.StudyRole;
import com.example.participant.exception.ParticipantErrorCode;
import com.example.participant.repository.ParticipantRepository;
import com.example.study.exception.StudyErrorCode;
import com.example.study.repository.StudyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationQueryServiceImpl implements ApplicationQueryService {
	
	private final StudyRepository studyRepository;
	private final ParticipantRepository participantRepository;
	private final ApplicationRepository applicationRepository;
	
	@Override
	@Transactional(readOnly = true)
	// 지원자 목록 조회 (스터디장 전용)
	public List<ApplicationResDto> getApplications(Long memberId, Long studyId) {
		if (!studyRepository.existsByIdAndIsDeletedFalse(studyId)) {
			throw new GeneralException(StudyErrorCode.STUDY_NOT_FOUND);
		}
		// 현재 요청자가 해당 스터디의 스터디장인지 검증 
		if (!participantRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.LEADER)) {
			throw new GeneralException(ParticipantErrorCode.NOT_STUDY_LEADER);
		}
		List<Application> applications = applicationRepository.findAllByStudyIdWithApplicant(studyId);
		return applications.stream().map(ApplicationConverter::toApplicationResDto).toList();
	}

}

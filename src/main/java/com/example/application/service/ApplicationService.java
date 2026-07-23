package com.example.application.service;

import com.example.application.dto.request.CreateApplicationReqDto;
import com.example.application.dto.request.UpdateApplicationStatusReqDto;
import com.example.application.dto.response.ApplicationResDto;

public interface ApplicationService {
	// 스터디 지원
	ApplicationResDto createApplication(Long applicantId, Long studyId, CreateApplicationReqDto request);
	// 지원 수락/거절
	ApplicationResDto updateApplicationStatus(Long requesterId, Long applicationId, UpdateApplicationStatusReqDto request);
	// 지원 삭제(취소)
	void deleteMyApplication(Long applicantId, Long applicationId);
	
}

package com.example.study.service;

import com.example.study.dto.request.CreateStudyReqDto;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.dto.response.StudyInfoResDto;
import com.example.study.dto.response.UpdateStudyLeaderResDto;
import com.example.study.entity.enums.StudyStatus;

public interface StudyService {
	// 스터디 생성
	StudyInfoResDto createStudy(Long memberId, CreateStudyReqDto reqDto);
	// 스터디 수정
	StudyInfoResDto updateStudy(Long memberId, Long studyId, UpdateStudyReqDto reqDto);
	// 스터디 상태 변경 (모집 중, 모집 완료, 종료됨)
	StudyInfoResDto updateStudyStatus(Long memberId, Long studyId, StudyStatus status);
	// 스터디장 변경 (위임)
	UpdateStudyLeaderResDto updateStudyLeader(Long memberId, Long studyId, Long newLeaderId);
	// 스터디 soft delete (DELETED)
	void softDeleteStudy(Long memberId, Long studyId);
}

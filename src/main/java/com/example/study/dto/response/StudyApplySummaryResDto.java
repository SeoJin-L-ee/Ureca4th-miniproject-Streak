package com.example.study.dto.response;

import java.time.LocalDateTime;

import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

// 스터디 찾기 화면에서 목록 조회 시 사용되는 단건 DTO (StudyAplySummaryListResDto에 리스트로 포함)
public record StudyApplySummaryResDto(
		Long studyId,
		String title,
		String leaderName,
		
		StudyCategory category,
		StudyStatus status,
		MyStudyStatus myStatus,
		
		int currentParticipantCnt,
		int capacity,
		LocalDateTime createdAt
) {
	// 목록 요청한 사용자가, 각 스터디에 대해 이미 지원했는지 참여중인지를 나타내는 enum
	// 이 지표에 따라 프론트에서 지원하기 버튼을 아예 막든 하게끔 할 예정
	public enum MyStudyStatus {
		NONE,
		PENDING,
		ACCEPTED,
		REJECTED
	}
}

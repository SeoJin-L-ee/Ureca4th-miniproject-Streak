package com.example.member.dto.response;

import com.example.participant.entity.enums.StudyRole;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

//마이페이지 - 참여 중인 스터디
public record MyStudyResDto(Long studyId, String title, StudyCategory category, StudyStatus status, StudyRole role) {
	
}